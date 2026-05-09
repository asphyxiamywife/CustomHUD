package com.minenash.customhud.complex;

import com.minenash.customhud.mixin.music.MinecraftClientAccess;
import com.minenash.customhud.mixin.music.MusicTrackerAccess;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public class MusicAndRecordTracker {

    public static boolean isMusicPlaying = false;
    public static Identifier musicId = null;
    public static String musicName = "";

    public static boolean isRecordPlaying = false;

    public static List<RecordInstance> records = new ArrayList<>();
    public static class RecordInstance {
        public SoundInstance sound = null;
        public Identifier id = null;
        public Component name = Component.literal("Unknown Music Disc");
        public int length = 0;
        public int elapsed = 0;
        public ItemStack icon = new ItemStack(Items.BARRIER);
    }

    public static RecordInstance getClosestRecord() {
        if (client.player == null) return records.get(records.size()-1);
        Vec3 pos = client.player.position();

        RecordInstance closestInstance = records.get(0);
        double closestDistance = pos.distanceToSqr(closestInstance.sound.getX(), closestInstance.sound.getY(), closestInstance.sound.getZ());


        for (int i = 1; i < records.size(); i++) {
            RecordInstance instance = records.get(i);
            double distance = pos.distanceToSqr(instance.sound.getX(), instance.sound.getY(), instance.sound.getZ());
            if (distance <= closestDistance) {
                closestDistance = distance;
                closestInstance = instance;
            }
        }

        return closestInstance;
    }

    private final static Minecraft client = Minecraft.getInstance();

    public static void tick() {
//        isRecordPlaying = recordInstance != null && client.getSoundManager().isPlaying(recordInstance);
//        if (isRecordPlaying)
//            recordElapsed++;


        Iterator<RecordInstance> iterator = records.iterator();
        while (iterator.hasNext()) {
            RecordInstance instance = iterator.next();
            if (!client.getSoundManager().isActive(instance.sound))
                iterator.remove();
            else
                instance.elapsed++;
        }
        isRecordPlaying = !records.isEmpty();


        SoundInstance music = ((MusicTrackerAccess)((MinecraftClientAccess)client).getMusicManager()).getCurrentMusic();
        isMusicPlaying =  client.getSoundManager().isActive(music);
        if (music != null) {
            musicId = music.getSound().getLocation();
            String idStr = musicId.toString();
            musicName = WordUtils.capitalize(idStr.substring(idStr.lastIndexOf('/')+1).replace("_", " ").replaceAll("(\\d+)", " $1"));
        }
    }

    public static void setRecord(Holder<JukeboxSong> song, SoundInstance instance, BlockPos jukeboxPos) {
        if (song == null)
            return;

        JukeboxSong jbs = song.value();

        RecordInstance record = new RecordInstance();
        record.sound = instance;
        record.id = song.unwrapKey().isPresent() ? song.unwrapKey().get().identifier() : null;
        record.name = jbs.description();
        record.length = jbs.lengthInTicks();

        if (client.getSingleplayerServer() != null && client.level != null) {
            BlockEntity state = client.getSingleplayerServer().getLevel(client.level.dimension()).getChunkAt(jukeboxPos).getBlockEntity(jukeboxPos, LevelChunk.EntityCreationType.IMMEDIATE);
            record.icon = state instanceof JukeboxBlockEntity jbe ? jbe.getTheItem() : ItemStack.EMPTY;
        }
        if (record.icon == ItemStack.EMPTY) {
            record.icon = new ItemStack(switch (record.id.toString()) {
                case "minecraft:13" -> Items.MUSIC_DISC_13;
                case "minecraft:cat" -> Items.MUSIC_DISC_CAT;
                case "minecraft:blocks" -> Items.MUSIC_DISC_BLOCKS;
                case "minecraft:chirp" -> Items.MUSIC_DISC_CHIRP;
                case "minecraft:far" -> Items.MUSIC_DISC_FAR;
                case "minecraft:mall" -> Items.MUSIC_DISC_MALL;
                case "minecraft:mellohi" -> Items.MUSIC_DISC_MELLOHI;
                case "minecraft:stal" -> Items.MUSIC_DISC_STAL;
                case "minecraft:strad" -> Items.MUSIC_DISC_STRAD;
                case "minecraft:ward" -> Items.MUSIC_DISC_WARD;
                case "minecraft:11" -> Items.MUSIC_DISC_11;
                case "minecraft:wait" -> Items.MUSIC_DISC_WAIT;
                case "minecraft:pigstep" -> Items.MUSIC_DISC_PIGSTEP;
                case "minecraft:otherside" -> Items.MUSIC_DISC_OTHERSIDE;
                case "minecraft:5" -> Items.MUSIC_DISC_5;
                case "minecraft:relic" -> Items.MUSIC_DISC_RELIC;
                case "minecraft:precipice" -> Items.MUSIC_DISC_PRECIPICE;
                case "minecraft:creator" -> Items.MUSIC_DISC_CREATOR;
                case "minecraft:creator_music_box" -> Items.MUSIC_DISC_CREATOR_MUSIC_BOX;
                default -> Items.AIR;
            });
        }

        records.add(record);
    }

}
