package com.minenash.customhud.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;

@Mixin(Options.class)
public interface GameOptionsAccessor {

    @Invoker("processOptions") void invokeAccept(Options.FieldAccess visitor);

    @Accessor Map<SoundSource, OptionInstance<Double>> getSoundSourceVolumes();

    @Accessor("cloudStatus")
    OptionInstance<?> getCloudRenderMode();

}
