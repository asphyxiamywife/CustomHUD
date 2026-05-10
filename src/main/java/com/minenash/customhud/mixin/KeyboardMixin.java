package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.DebugCharts;
import com.minenash.customhud.data.Profile;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showProfilerChart()Z"))
    public boolean shouldShowProfiler(DebugScreenOverlay instance, Operation<Boolean> original) {
        Profile p = ProfileManager.getActive();
        return original.call(instance) ||
                (!minecraft.gui.hud.isHidden() && !minecraft.getDebugOverlay().showDebugScreen() && minecraft.level != null
                        && p != null && (p.leftChart == DebugCharts.PROFILER || p.rightChart == DebugCharts.PROFILER) );
    }

}
