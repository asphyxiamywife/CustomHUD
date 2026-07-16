package com.minenash.customhud.mixin;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.DebugCharts;
import com.minenash.customhud.data.Profile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.customhud.CustomHud.CLIENT;

@Mixin(ProfilerPieChart.class)
public class PieChartMixin {

    @ModifyVariable(method = "extractRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int moveProfilerToLeft(int guiWidth) {
        Profile p = ProfileManager.getActive();
        return p != null && p.leftChart == DebugCharts.PROFILER ? 360 : guiWidth;
    }

    @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderTheActualProfiler(GuiGraphicsExtractor context, int guiWidth, int guiHeight, CallbackInfo ci) {
        Profile p = ProfileManager.getActive();
        if (CLIENT.getDebugOverlay().showDebugScreen() ||
                (!CLIENT.gui.hud.isHidden() && !CLIENT.getDebugOverlay().showDebugScreen() && CLIENT.level != null
                        && p != null && (p.leftChart == DebugCharts.PROFILER || p.rightChart == DebugCharts.PROFILER)) )
            return;
        ci.cancel();
    }

}
