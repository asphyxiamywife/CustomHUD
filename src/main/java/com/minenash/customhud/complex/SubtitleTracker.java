package com.minenash.customhud.complex;

import com.google.common.collect.Lists;
import com.minenash.customhud.HudElements.list.AttributeFunctions;
import com.minenash.customhud.ducks.SubtitleEntryDuck;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import java.util.List;

import static com.minenash.customhud.CustomHud.CLIENT;

public class SubtitleTracker implements SoundEventListener {

    public static final SubtitleTracker INSTANCE = new SubtitleTracker();

    public final List<SubtitleOverlay.Subtitle> entries = Lists.newArrayList();

    private boolean enabled = false;

    public void setEnable(boolean enable) {
        if (enable) tick();
        if (this.enabled == enable) return;
        this.enabled = enable;
        if (enable) CLIENT.getSoundManager().addListener(this);
        else CLIENT.getSoundManager().removeListener(this);
    }

    @Override
    public void onPlaySound(SoundInstance sound, WeighedSoundEvents soundSet, float range) {
        if (soundSet.getSubtitle() != null) {
            Component text = soundSet.getSubtitle();
            if (!this.entries.isEmpty()) {
                for (var entry : entries) {
                    if (entry.getText().equals(text)) {
                        entry.refresh(new Vec3(sound.getX(), sound.getY(), sound.getZ()));
                        return;
                    }
                }
            }
            SubtitleOverlay.Subtitle entry = new SubtitleOverlay.Subtitle(text, range, new Vec3(sound.getX(), sound.getY(), sound.getZ()));
            ((SubtitleEntryDuck)entry).customhud$setSoundID(sound.getIdentifier());
            this.entries.add(entry);
        }
    }

    public void tick() {
        double d = CLIENT.options.notificationDisplayTime().get();
        this.entries.removeIf( e -> AttributeFunctions.sound(e).time + 3000.0 * d <= Util.getMillis());
    }
}