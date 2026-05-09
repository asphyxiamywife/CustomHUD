package com.minenash.customhud.mixin.music;

import com.minenash.customhud.complex.MusicAndRecordTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.JukeboxSong;

@Mixin(LevelEventHandler.class)
public class WorldEventHandlerMixin {

    @Shadow @Final private Map<BlockPos, SoundInstance> playingJukeboxSongs;

    @Inject(method = "playJukeboxSong", at = @At("TAIL"))
    private void getRecord(Holder<JukeboxSong> song, BlockPos jukeboxPos, CallbackInfo ci) {
        MusicAndRecordTracker.setRecord(song, playingJukeboxSongs.get(jukeboxPos), jukeboxPos);
    }

}
