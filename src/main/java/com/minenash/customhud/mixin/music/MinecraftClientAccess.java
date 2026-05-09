package com.minenash.customhud.mixin.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccess {

    @Accessor MusicManager getMusicManager();

}
