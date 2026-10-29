package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.DebugCharts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @WrapWithCondition(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debugchart/ProfilerPieChart;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private boolean customhud$renderVanillaProfiler(ProfilerPieChart chart, GuiGraphicsExtractor graphics) {
        var profile = ProfileManager.getActive();
        return Minecraft.getInstance().debugEntries.isOverlayVisible() || profile == null
                || (profile.leftChart != DebugCharts.PROFILER && profile.rightChart != DebugCharts.PROFILER);
    }
}
