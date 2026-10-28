package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Crosshairs;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public abstract class InGameHudMixin {
    @ModifyExpressionValue(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isCurrentlyEnabled(Lnet/minecraft/resources/ResourceKey;)Z"))
    private boolean getDebugCrosshairEnable(boolean original) {
        var profile = ProfileManager.getActive();
        return original && (profile == null || profile.crosshair == Crosshairs.VANILLA);
    }
}
