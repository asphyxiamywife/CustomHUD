package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import org.apache.commons.lang3.text.WordUtils;
import oshi.hardware.CentralProcessor;

import java.util.function.Supplier;

import static com.minenash.customhud.HudElements.supplier.EntryNumberSuppliers.*;

public class StringSupplierElement implements HudElement {

    private static final Minecraft client = Minecraft.getInstance();
    private static Entity cameraEntity() { return client.getCameraEntity(); }
    private static BlockPos blockPos() { return client.getCameraEntity().blockPosition(); }

    public static final Supplier<String> PROFILE_NAME = () -> ProfileManager.getActive() == null ? null : ProfileManager.getActive().name;

    public static final Supplier<String> VERSION = () -> SharedConstants.getCurrentVersion().name();
    public static final Supplier<String> CLIENT_VERSION = client::getLaunchedVersion;
    public static final Supplier<String> MODDED_NAME = ClientBrandRetriever::getClientModName;
    public static final Supplier<String> USERNAME = () -> client.player.getGameProfile().name() == null ? null : client.player.getGameProfile().name();
    public static final Supplier<String> UUID = () -> client.player.getGameProfile().id().toString();

    public static final Supplier<String> SERVER_BRAND = () -> client.player.connection.serverBrand();
    public static final Supplier<String> SERVER_NAME = () -> client.getCurrentServer().name;
    public static final Supplier<String> SERVER_ADDRESS = () -> client.getCurrentServer().ip;
    public static final Supplier<String> WORLD_NAME = () -> !client.hasSingleplayerServer() ? null : client.getSingleplayerServer().getWorldData().getLevelName();

    public static final Supplier<String> DIMENSION = () -> WordUtils.capitalize(client.level.dimension().identifier().getPath().replace("_"," "));
    public static final Supplier<String> BIOME = () -> I18n.get("biome." + client.level.getBiome(blockPos()).unwrapKey().get().identifier().toString().replace(':', '.'));

    private static final String[] moon_phases = new String[]{"full moon", "waning gibbous", "last quarter", "waning crescent", "new moon", "waxing crescent", "first quarter", "waxing gibbous"};
    public static final Supplier<String> MOON_PHASE_WORD = () -> ComplexData.clientChunk.isEmpty() ? null : moon_phases[(int) (client.level.getOverworldClockTime() / 24000L % 8L)];

    public static final Supplier<String> TIME_AM_PM = () -> ComplexData.timeOfDay < 12000 ? "am" : "pm";

    public static final Supplier<String> FACING4 = () -> cameraEntity().getDirection().getName();
    public static final Supplier<String> FACING4_SHORT = () -> cameraEntity().getDirection().getName().substring(0, 1).toUpperCase();
    public static final Supplier<String> FACING_TOWARDS_XZ = () ->
            cameraEntity().getDirection() == Direction.EAST || cameraEntity().getDirection() == Direction.WEST ? "X" : "Z";

    public static final Supplier<String> FACING8 = () -> {
        float yaw = Mth.wrapDegrees(cameraEntity().getYRot());
        if (yaw > 157.5 || yaw < -157.5) return "north";
        if (yaw > 112.5) return "northwest";
        if (yaw > 67.5)  return "west";
        if (yaw > 22.5)  return "southwest";
        if (yaw < -112.5) return "northeast";
        if (yaw < -67.5)  return "east";
        if (yaw < -22.5)  return "southeast";
        return "south";
    };
    public static final Supplier<String> FACING8_SHORT = () -> {
        float yaw = Mth.wrapDegrees(cameraEntity().getYRot());
        if (yaw > 157.5 || yaw < -157.5) return "N";
        if (yaw > 112.5) return "NW";
        if (yaw > 67.5)  return "W";
        if (yaw > 22.5)  return "SW";
        if (yaw < -112.5) return "NE";
        if (yaw < -67.5)  return "E";
        if (yaw < -22.5)  return "SE";
        return "S";
    };

    public static final Supplier<String> JAVA_VERSION = () -> System.getProperty("java.version");
    public static final Supplier<String> CPU_NAME = () -> ComplexData.cpu == null ? null : ((CentralProcessor)ComplexData.cpu).getProcessorIdentifier().getName().trim();
    public static final Supplier<String> GPU_NAME = () -> RenderSystem.getDevice().getDeviceInfo().name();
    public static final Supplier<String> GPU_VENDOR = () -> RenderSystem.getDevice().getDeviceInfo().vendorName();
    public static final Supplier<String> GL_VERSION = () -> RenderSystem.getDevice().getDeviceInfo().backendName();
    public static final Supplier<String> GPU_DRIVER = () -> RenderSystem.getDevice().getDeviceInfo().driverInfo();

    public static final Supplier<String> MUSIC_NAME = () -> MusicAndRecordTracker.isMusicPlaying ? MusicAndRecordTracker.musicName : null;

    public static final Supplier<String> BIOME_BUILDER_PEAKS = () -> isNoise() ? OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(NoiseRouterData.peaksAndValleys((float)sample(sampler().ridges()))) : null;
    public static final Supplier<String> BIOME_BUILDER_CONTINENTS = () -> isNoise() ? par.getDebugStringForContinentalness(sample(sampler().continents())) : null;

    public static final Supplier<String> VILLAGER_BIOME = () -> ComplexData.targetEntity instanceof Villager ve ? WordUtils.capitalize(ve.getVillagerData().type().toString()) : null;
    public static final Supplier<String> VILLAGER_LEVEL_WORD = () -> ComplexData.targetEntity instanceof Villager ve ? I18n.get("merchant.level." + ve.getVillagerData().level()) : null;


    public static final Supplier<String> RESOURCE_PACK_VERSION = () -> SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).toString();
    public static final Supplier<String> DATA_PACK_VERSION = () -> SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).toString();

    private final Supplier<String> supplier;

    public StringSupplierElement(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String getString() {
        return sanitize(supplier, "-");
    }

    @Override
    public Number getNumber() {
        try {
            return supplier.get().length();
        }
        catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean getBoolean() {
        return getNumber().intValue() > 0;
    }
}
