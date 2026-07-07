package com.minenash.customhud.mixin.accessors;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DebugScreenOverlay.class)
public interface DebugHudAccessor {

    @Accessor LocalSampleLogger getFrameTimeLogger();
    @Accessor LocalSampleLogger getTickTimeLogger();

}
