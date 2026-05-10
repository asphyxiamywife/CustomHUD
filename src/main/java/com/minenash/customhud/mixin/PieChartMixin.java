package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.DebugCharts;
import com.minenash.customhud.data.Profile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.customhud.CustomHud.CLIENT;

@Mixin(ProfilerPieChart.class)
public class PieChartMixin {

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiWidth()I"))
    public int moveProfilerToLeft(GuiGraphicsExtractor instance, Operation<Integer> original) {
        Profile p = ProfileManager.getActive();
        return p != null && p.leftChart == DebugCharts.PROFILER ? 360 : original.call(instance);
    }

    @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderTheActualProfiler(GuiGraphicsExtractor context, CallbackInfo ci) {
        Profile p = ProfileManager.getActive();
        if (CLIENT.getDebugOverlay().showDebugScreen() ||
                (!CLIENT.gui.hud.isHidden() && !CLIENT.getDebugOverlay().showDebugScreen() && CLIENT.level != null
                        && p != null && (p.leftChart == DebugCharts.PROFILER || p.rightChart == DebugCharts.PROFILER)) )
            return;
        ci.cancel();
    }

}
