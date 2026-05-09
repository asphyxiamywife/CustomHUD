package com.minenash.customhud.mixin.disable;

import com.minenash.customhud.CustomHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.customhud.data.DisableElement.*;

@Mixin(JumpableVehicleBarRenderer.class)
public class JumpBarMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    public void customhud$disableHorseJump(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(HORSE) || CustomHud.isDisabled(HORSE_JUMP))
            ci.cancel();
    }
}
