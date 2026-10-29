package com.minenash.customhud.test.mixin;

import com.minenash.customhud.test.FrameProbe;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiProfilerChartRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiFrameProbeMixin {
    @Shadow @Final private GuiRenderState renderState;

    @Inject(method = "render", at = @At("HEAD"))
    private void captureCompleteFrame(CallbackInfo ci) {
        int[] charts = {0};
        renderState.forEachPictureInPicture(state -> {
            if (state instanceof GuiProfilerChartRenderState) charts[0]++;
        });
        FrameProbe.profilerCharts = charts[0];
    }
}
