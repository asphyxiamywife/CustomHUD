package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Crosshairs;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;render3dCrosshair:Z"))
    private boolean getDebugCrosshairEnable(boolean original) {
        return original || (ProfileManager.getActive() != null && ProfileManager.getActive().crosshair == Crosshairs.DEBUG);
    }


}
