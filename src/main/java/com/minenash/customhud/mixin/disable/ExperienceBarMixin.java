package com.minenash.customhud.mixin.disable;

import com.minenash.customhud.CustomHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.customhud.data.DisableElement.XP;

@Mixin(ExperienceBarRenderer.class)
public class ExperienceBarMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true, require = 0)
    public void customhud$disableXPBar(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(XP))
            ci.cancel();
    }
}
