package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.CustomHud;
import com.minenash.customhud.complex.OpenGlGpuTimer;
import com.minenash.customhud.data.DebugCharts;
import com.minenash.customhud.data.Profile;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Unique private OpenGlGpuTimer customhud$openGlGpuTimer;

    @Shadow @Final public Options options;
    @Shadow public abstract double getGpuUtilization();

    @Shadow @Final public Gui gui;
    @Shadow public abstract DebugScreenOverlay getDebugOverlay();

    @Shadow @Nullable public ClientLevel level;

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z"))
    public boolean readClick(KeyMapping instance, Operation<Boolean> original) {
        boolean p = original.call(instance);

        if (p && instance == options.keyAttack)
            ComplexData.clicksSoFar[0]++;
        if (p && instance == options.keyUse)
            ComplexData.clicksSoFar[1]++;

        return p;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(GameConfig args, CallbackInfo ci) {
        CustomHud.delayedInitialize();
    }

    @Inject(method = "renderFrame", at = @At("HEAD"))
    public void beginGpuUsageQuery(boolean tick, CallbackInfo ci) {
        Profile profile = ProfileManager.getActive();
        if (profile == null || !profile.enabled.gpuMetrics || !OpenGlGpuTimer.isSupported())
            return;

        if (customhud$openGlGpuTimer == null)
            customhud$openGlGpuTimer = new OpenGlGpuTimer();

        double usage = customhud$openGlGpuTimer.beginFrame();
        if (Double.isFinite(usage))
            ComplexData.updateGpuUsage(usage);
    }

    @Inject(method = "renderFrame", at = @At("RETURN"))
    public void getGpuUsage(boolean tick, CallbackInfo ci) {
        if (customhud$openGlGpuTimer != null)
            customhud$openGlGpuTimer.endFrame();

        Profile profile = ProfileManager.getActive();
        if (profile == null || !profile.enabled.gpuMetrics) {
            ComplexData.resetGpuUsage();
            return;
        }

        if (!OpenGlGpuTimer.isSupported())
            ComplexData.updateGpuUsage(getGpuUtilization());
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isCurrentlyEnabled(Lnet/minecraft/resources/Identifier;)Z"))
    public boolean getGpuUsageAndOtherPerformanceMetrics(DebugScreenEntryList instance, Identifier entryId, Operation<Boolean> original) {
        Profile profile = ProfileManager.getActive();
        return original.call(instance, entryId)
                || (DebugScreenEntries.GPU_UTILIZATION.equals(entryId)
                    && profile != null
                    && profile.enabled.gpuMetrics
                    && !OpenGlGpuTimer.isSupported());
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void closeGpuUsageQuery(CallbackInfo ci) {
        if (customhud$openGlGpuTimer != null) {
            customhud$openGlGpuTimer.close();
            customhud$openGlGpuTimer = null;
        }
    }


    @Unique private static boolean isFirst = true;
    @Inject(method = "onResourceLoadFinished", at = @At("RETURN"))
    public void reloadProfiles(GameLoadCookie loadingContext, CallbackInfo ci) {
        if (isFirst) {
            isFirst = false;
            return;
        }

        CustomHud.resourceTriggeredReload();
    }

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showProfilerChart()Z"))
    public boolean activateProfiler(DebugScreenOverlay instance, Operation<Boolean> original) {
        if (ComplexData.refreshTimings) {
            ComplexData.refreshTimings = false;
            return false;
        }

        Profile p = ProfileManager.getActive();
        return original.call(instance) ||
                (!gui.hud.isHidden() && !getDebugOverlay().showDebugScreen() && level != null
                        && p != null && (p.enabled.profilerTimings || p.leftChart == DebugCharts.PROFILER || p.rightChart == DebugCharts.PROFILER) );
    }
}
