package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.minenash.customhud.CustomHud.CLIENT;

@Mixin(Gui.class)
public class GuiMixin {

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    public void changeHudGuiScale(Hud instance, GuiGraphicsExtractor context, DeltaTracker deltaTracker, Operation<Void> original) {
        Profile p = ProfileManager.getActive();
        int target = p == null || p.baseTheme.hudScale == null ? 0 : p.baseTheme.getTargetGuiScale();
        if (target <= 0) {
            original.call(instance, context, deltaTracker);
            return;
        }

        Window window = CLIENT.getWindow();
        int originalScale = window.getGuiScale();
        float scale = (float) target / originalScale;
        window.setGuiScale(target);

        context.pose().pushMatrix();
        try {
            context.pose().scale(scale, scale);
            original.call(instance, context, deltaTracker);
        }
        finally {
            context.pose().popMatrix();
            window.setGuiScale(originalScale);
        }
    }

}
