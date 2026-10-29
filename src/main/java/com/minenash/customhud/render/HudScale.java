package com.minenash.customhud.render;

import com.minenash.customhud.data.Profile;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudScale {
    private HudScale() {}

    public static void extract(GuiGraphicsExtractor graphics, Profile profile, Runnable extraction) {
        int target = profile == null || profile.baseTheme.hudScale == null
                ? 0 : profile.baseTheme.getTargetGuiScale();
        Window window = Minecraft.getInstance().getWindow();
        int originalScale = window.getGuiScale();
        if (target <= 0 || target == originalScale) {
            extraction.run();
            return;
        }

        graphics.pose().pushMatrix();
        try {
            window.setGuiScale(target);
            graphics.pose().scale((float) target / originalScale);
            extraction.run();
        }
        finally {
            graphics.pose().popMatrix();
            window.setGuiScale(originalScale);
        }
    }
}
