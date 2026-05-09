package com.minenash.customhud.mixin;

import com.minenash.customhud.ducks.SubtitleEntryDuck;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SubtitleOverlay.Subtitle.class)
public class SubtitleEntryMixin implements SubtitleEntryDuck {

    @Unique public Identifier soundId;

    @Override
    public Identifier customhud$getSoundID() {
        return soundId;
    }

    @Override
    public void customhud$setSoundID(Identifier id) {
        soundId = id;
    }
}
