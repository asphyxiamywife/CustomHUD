package com.minenash.customhud.test;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.minenash.customhud.render.HudScale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

import static com.minenash.customhud.test.ProfileFixtures.check;

final class HudScaleChecks {
    private HudScaleChecks() {}

    static void run(Minecraft client, Profile profile) {
        var window = client.getWindow();
        int savedScale = window.getGuiScale();
        Profile savedProfile = ProfileManager.getActive();
        boolean savedEnabled = ProfileManager.enabled;
        Integer savedHudScale = profile.baseTheme.hudScale;
        try {
            ProfileManager.setActive(profile);
            ProfileManager.enabled = true;
            for (String theme : List.of("scale:1", "nicescale:2", "nicescale:+1")) {
                check(profile.baseTheme.parse(true, theme, profile.name, 0), "Invalid theme fixture: " + theme);
                for (int guiScale : new int[]{1, 2, 3, 4}) {
                    window.setGuiScale(guiScale);
                    Geometry normal = extract(client, profile, true);
                    Geometry deferred = extract(client, profile, false);
                    check(!normal.text().isEmpty(), "No text extracted for " + theme);
                    check(normal.equals(deferred), "Normal/deferred geometry differs: " + theme + ", GUI " + guiScale);
                    check(window.getGuiScale() == guiScale, "Window scale leaked at GUI " + guiScale);
                }
            }

            window.setGuiScale(3);
            var graphics = new GuiGraphicsExtractor(client, new GuiRenderState(), 0, 0);
            var before = new Matrix3x2f(graphics.pose());
            RuntimeException expected = new RuntimeException("deliberate extraction failure");
            try {
                HudScale.extract(graphics, profile, () -> { throw expected; });
                throw new AssertionError("Extraction exception was swallowed");
            } catch (RuntimeException actual) {
                check(actual == expected, "Wrong extraction exception");
            }
            check(window.getGuiScale() == 3 && before.equals(graphics.pose()), "Scale/matrix leaked after exception");

            profile.baseTheme.hudScale = null;
            check(extract(client, profile, true).equals(extract(client, profile, false)), "No-override geometry differs");
        } finally {
            profile.baseTheme.hudScale = savedHudScale;
            profile.baseTheme.parse(true, "scale:1", profile.name, 0);
            window.setGuiScale(savedScale);
            ProfileManager.setActive(savedProfile);
            ProfileManager.enabled = savedEnabled;
        }
    }

    private static Geometry extract(Minecraft client, Profile profile, boolean nested) {
        var state = new GuiRenderState();
        var graphics = new GuiGraphicsExtractor(client, state, 0, 0);
        var before = new Matrix3x2f(graphics.pose());
        if (nested)
            HudScale.extract(graphics, profile, () -> CustomHudRenderer3.extractRenderState(graphics, DeltaTracker.ZERO));
        else
            CustomHudRenderer3.extractRenderState(graphics, DeltaTracker.ZERO);
        check(before.equals(graphics.pose()), "Extraction leaked its pose");
        List<TextGeometry> text = new ArrayList<>();
        List<ScreenRectangle> elements = new ArrayList<>();
        state.forEachText(value -> text.add(new TextGeometry(new Matrix3x2f(value.pose), value.bounds())));
        state.forEachElement(value -> elements.add(value.bounds()), GuiRenderState.TraverseRange.ALL);
        return new Geometry(text, elements);
    }

    private record TextGeometry(Matrix3x2f pose, ScreenRectangle bounds) {}
    private record Geometry(List<TextGeometry> text, List<ScreenRectangle> elements) {}
}
