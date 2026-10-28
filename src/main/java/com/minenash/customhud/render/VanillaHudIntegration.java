package com.minenash.customhud.render;

import com.minenash.customhud.CustomHud;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Crosshairs;
import com.minenash.customhud.data.DisableElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

import static com.minenash.customhud.data.DisableElement.*;

public final class VanillaHudIntegration {
    private VanillaHudIntegration() {}

    public static void register() {
        hideWhenDisabled(VanillaHudElements.HOTBAR, HOTBAR);
        hideWhenDisabled(VanillaHudElements.HELD_ITEM_TOOLTIP, ITEM_TOOLTIP);
        hideWhenDisabled(VanillaHudElements.ARMOR_BAR, ARMOR, STATUS_BARS);
        hideWhenDisabled(VanillaHudElements.HEALTH_BAR, HEALTH, STATUS_BARS);
        hideWhenDisabled(VanillaHudElements.FOOD_BAR, HUNGER, STATUS_BARS);
        hideWhenDisabled(VanillaHudElements.AIR_BAR, AIR, STATUS_BARS);
        hideWhenDisabled(VanillaHudElements.MOUNT_HEALTH, HORSE, HORSE_HEALTH, STATUS_BARS);
        hideWhenDisabled(VanillaHudElements.EXPERIENCE_LEVEL, XP);
        hideWhenDisabled(VanillaHudElements.MOB_EFFECTS, STATUS_EFFECTS);
        hideWhenDisabled(VanillaHudElements.BOSS_BAR, BOSSBARS);
        hideWhenDisabled(VanillaHudElements.SUBTITLES, SUBTITLES);
        hideWhenDisabled(VanillaHudElements.SCOREBOARD, SCOREBOARD);
        hideWhenDisabled(VanillaHudElements.CHAT, CHAT);
        hideWhenDisabled(VanillaHudElements.TITLE_AND_SUBTITLE, TITLES);
        hideWhenDisabled(VanillaHudElements.OVERLAY_MESSAGE, ACTIONBAR);

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> graphics -> {
            var profile = ProfileManager.getActive();
            if (profile == null || profile.crosshair != Crosshairs.NONE)
                original.extractRenderState(graphics);
        });
    }

    private static void hideWhenDisabled(Identifier layer, DisableElement... elements) {
        HudElementRegistry.replaceElement(layer, original -> graphics -> {
            for (DisableElement element : elements)
                if (CustomHud.isDisabled(element))
                    return;
            original.extractRenderState(graphics);
        });
    }
}
