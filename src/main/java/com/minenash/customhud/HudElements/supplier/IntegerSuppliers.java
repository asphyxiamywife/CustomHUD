package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.errors.Errors;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import oshi.hardware.CentralProcessor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Supplier;

import static com.minenash.customhud.CustomHud.CLIENT;
import static com.minenash.customhud.HudElements.supplier.EntryNumberSuppliers.*;
import static com.minenash.customhud.complex.ComplexData.targetBlock;
import static com.minenash.customhud.complex.ComplexData.targetBlockPos;
import static java.time.temporal.ChronoField.MICRO_OF_SECOND;

public class IntegerSuppliers {

    private static final Minecraft client = Minecraft.getInstance();
    private static LevelRenderer worldRender() {
        return client.levelRenderer;
    }
    private static SectionRenderDispatcher chunkBuilder() {
        return worldRender().sectionRenderDispatcher();
    }
    private static BlockPos blockPos() { return client.getCameraEntity().blockPosition(); }
    private static LevelLightEngine serverLighting() { return ComplexData.world.getChunkSource().getLightEngine(); }

    private static Integer chunk(LevelChunk chunk, Heightmap.Types type) {
        if (chunk == null) return null;
        BlockPos pos = client.getCameraEntity().blockPosition();
        return chunk.getHeight(type, pos.getX(), pos.getZ());
    }

    private static Integer spawnGroup(MobCategory group) {
        NaturalSpawner.SpawnState info = ComplexData.serverWorld.getChunkSource().getLastSpawnState();
        return info == null ? null : info.getMobCategoryCounts().getInt(group);
    }

    private static double biome(DensityFunction function, Climate.Parameter[] range) {
        double d = (double)Climate.quantizeCoord((float) EntryNumberSuppliers.sample(function));
        for(int i = 0; i < range.length; ++i)
            if (d < (double)range[i].max())
                return i;
        return Double.NaN;
    }

    public static final Supplier<Number> PROFILE_ERRORS = () -> ProfileManager.getActive() == null ? 0 : Errors.getErrors(ProfileManager.getActive().name).size();

    public static final Supplier<Number> FPS = client::getFps;
    public static final Supplier<Number> BIOME_BLEND = () -> client.options.biomeBlendRadius().get();
    public static final Supplier<Number> SIMULATION_DISTANCE = () -> client.options.simulationDistance().get();

    public static final Supplier<Number> PACKETS_SENT = () -> (int)client.getConnection().getConnection().getAverageSentPackets();
    public static final Supplier<Number> PACKETS_RECEIVED = () -> (int)client.getConnection().getConnection().getAverageReceivedPackets();
    public static final Supplier<Number> CHUNKS_RENDERED = () -> worldRender().visibleSections().size();
    public static final Supplier<Number> CHUNKS_LOADED = () -> worldRender().viewArea().sectionCount();
    @SuppressWarnings("Convert2MethodRef" )
    public static final Supplier<Number> RENDER_DISTANCE = () -> client.options.getEffectiveRenderDistance();
    public static final Supplier<Number> QUEUED_TASKS = () -> chunkBuilder().getCompileQueueSize();
    public static final Supplier<Number> UPLOAD_QUEUE = () -> 0;
    public static final Supplier<Number> BUFFER_COUNT = () -> chunkBuilder().getFreeBufferCount();
    public static final Supplier<Number> ENTITIES_RENDERED = () -> 0;
    public static final Supplier<Number> ENTITIES_LOADED = () -> client.level.getEntityCount();

    public static final Supplier<Number> FORCED_LOADED_CHUNKS = () -> ComplexData.world instanceof ServerLevel ? ((ServerLevel)ComplexData.world).getForceLoadedChunks().size() : null;

    public static final Supplier<Number> BLOCK_X = () -> blockPos().getX();
    public static final Supplier<Number> BLOCK_Y = () -> blockPos().getY();
    public static final Supplier<Number> BLOCK_Z = () -> blockPos().getZ();
    public static final Supplier<Number> TARGET_BLOCK_X = () -> targetBlockPos == null ? null : targetBlockPos.getX();
    public static final Supplier<Number> TARGET_BLOCK_Y = () -> targetBlockPos == null ? null : targetBlockPos.getY();
    public static final Supplier<Number> TARGET_BLOCK_Z = () -> targetBlockPos == null ? null : targetBlockPos.getZ();
    public static final Supplier<Number> TARGET_BLOCK_DISTANCE = () -> targetBlockPos == null ? null : targetBlockPos.distManhattan(client.player.blockPosition());
    public static final Supplier<Number> TARGET_BLOCK_COLOR = () -> targetBlock == null || targetBlock.isAir() ? null : targetBlock.getMapColor(client.level, targetBlockPos).col;
    public static final Supplier<Number> TARGET_BLOCK_LUMINANCE = () -> targetBlock == null || targetBlock.isAir() ? null : ComplexData.world.getLightEmission(targetBlockPos);
    public static final Supplier<Number> TARGET_FLUID_X = () -> ComplexData.targetFluidPos == null ? null : ComplexData.targetFluidPos.getX();
    public static final Supplier<Number> TARGET_FLUID_Y = () -> ComplexData.targetFluidPos == null ? null : ComplexData.targetFluidPos.getY();
    public static final Supplier<Number> TARGET_FLUID_Z = () -> ComplexData.targetFluidPos == null ? null : ComplexData.targetFluidPos.getZ();
    public static final Supplier<Number> TARGET_FLUID_DISTANCE = () -> ComplexData.targetFluidPos == null ? null : ComplexData.targetFluidPos.distManhattan(client.player.blockPosition());
    public static final Supplier<Number> TARGET_FLUID_COLOR = () -> ComplexData.targetFluid == null || ComplexData.targetFluid.isEmpty()? null : ComplexData.targetFluid.createLegacyBlock().getMapColor(client.level, ComplexData.targetFluidPos).col;

    public static final Supplier<Number> TARGET_BLOCK_POWERED = () -> targetBlockPos == null ? null : client.level.getBestNeighborSignal(targetBlockPos);
    public static final Supplier<Number> TARGET_BLOCK_POWERED_NORTH = () -> targetBlockPos == null ? null : client.level.getSignal(targetBlockPos.relative(Direction.NORTH), Direction.NORTH);
    public static final Supplier<Number> TARGET_BLOCK_POWERED_SOUTH = () -> targetBlockPos == null ? null : client.level.getSignal(targetBlockPos.relative(Direction.SOUTH), Direction.SOUTH);
    public static final Supplier<Number> TARGET_BLOCK_POWERED_EAST = () -> targetBlockPos == null ? null :  client.level.getSignal(targetBlockPos.relative(Direction.EAST), Direction.EAST);
    public static final Supplier<Number> TARGET_BLOCK_POWERED_WEST = () -> targetBlockPos == null ? null :  client.level.getSignal(targetBlockPos.relative(Direction.WEST), Direction.WEST);
    public static final Supplier<Number> TARGET_BLOCK_POWERED_UP = () -> targetBlockPos == null ? null :    client.level.getSignal(targetBlockPos.relative(Direction.UP), Direction.UP);
    public static final Supplier<Number> TARGET_BLOCK_POWERED_DOWN = () -> targetBlockPos == null ? null :  client.level.getSignal(targetBlockPos.relative(Direction.DOWN), Direction.DOWN);

    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED = () -> targetBlockPos == null ? null : client.level.getDirectSignalTo(targetBlockPos);
    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED_NORTH = () -> targetBlockPos == null ? null : client.level.getDirectSignal(targetBlockPos.relative(Direction.NORTH), Direction.NORTH);
    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED_SOUTH = () -> targetBlockPos == null ? null : client.level.getDirectSignal(targetBlockPos.relative(Direction.SOUTH), Direction.SOUTH);
    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED_EAST = () -> targetBlockPos == null ? null :  client.level.getDirectSignal(targetBlockPos.relative(Direction.EAST), Direction.EAST);
    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED_WEST = () -> targetBlockPos == null ? null :  client.level.getDirectSignal(targetBlockPos.relative(Direction.WEST), Direction.WEST);
    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED_UP = () -> targetBlockPos == null ? null :    client.level.getDirectSignal(targetBlockPos.relative(Direction.UP), Direction.UP);
    public static final Supplier<Number> TARGET_BLOCK_STRONG_POWERED_DOWN = () -> targetBlockPos == null ? null :  client.level.getDirectSignal(targetBlockPos.relative(Direction.DOWN), Direction.DOWN);

    public static final Supplier<Number> IN_CHUNK_X = () -> blockPos().getX() & 15;
    public static final Supplier<Number> IN_CHUNK_Y = () -> blockPos().getY() & 15;
    public static final Supplier<Number> IN_CHUNK_Z = () -> blockPos().getZ() & 15;
    public static final Supplier<Number> CHUNK_X = () -> blockPos().getX() >> 4;
    public static final Supplier<Number> CHUNK_Y = () -> blockPos().getY() >> 4;
    public static final Supplier<Number> CHUNK_Z = () -> blockPos().getZ() >> 4;
    public static final Supplier<Number> REGION_X = () -> blockPos().getX() >> 9;
    public static final Supplier<Number> REGION_Z = () -> blockPos().getZ() >> 9;
    public static final Supplier<Number> REGION_RELATIVE_X = () -> blockPos().getX() >> 4 & 0x1F;
    public static final Supplier<Number> REGION_RELATIVE_Z = () -> blockPos().getZ() >> 4 & 0x1F;

    public static final Supplier<Number> CHUNK_CLIENT_CACHED = () -> client.level.getChunkSource().storage.chunks.length();
    public static final Supplier<Number> CHUNK_CLIENT_LOADED = () -> client.level.getChunkSource().getLoadedChunksCount();
    public static final Supplier<Number> CHUNK_CLIENT_ENTITIES_LOADED = () -> client.level.getEntityCount();
    public static final Supplier<Number> CHUNK_CLIENT_ENTITIES_CACHED_SECTIONS = () -> client.level.entityStorage.sectionStorage.count();
    public static final Supplier<Number> CHUNK_CLIENT_ENTITIES_TICKING_CHUNKS = () -> client.level.entityStorage.tickingChunks.size();

    public static final Supplier<Number> CHUNK_SERVER_LOADED = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.getChunkSource().getLoadedChunksCount();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_REGISTERED = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.knownUuids.size();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_LOADED = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.visibleEntityStorage.count();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_CACHED_SECTIONS = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.sectionStorage.count();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_MANAGED = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.chunkLoadStatuses.size();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_TRACKED = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.chunkVisibility.size();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_LOADING = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.loadingInbox.size();
    public static final Supplier<Number> CHUNK_SERVER_ENTITIES_UNLOADING = () -> ComplexData.serverWorld == null ? null : ComplexData.serverWorld.entityManager.chunksToUnload.size();

    public static final Supplier<Number> CLIENT_LIGHT = () -> {
        if (ComplexData.clientChunk.isEmpty()) return null;
        client.level.updateSkyBrightness();
        return Math.max(0, client.level.getChunkSource().getLightEngine().getRawBrightness(blockPos(), client.level.getSkyDarken()));
    };
    public static final Supplier<Number> CLIENT_LIGHT_SKY = () -> ComplexData.clientChunk.isEmpty() ? null : client.level.getBrightness(LightLayer.SKY, blockPos());
    public static final Supplier<Number> CLIENT_LIGHT_SUN = () -> {
        if (ComplexData.clientChunk.isEmpty()) return null;
        client.level.updateSkyBrightness();
        return Math.max(0, client.level.getBrightness(LightLayer.SKY, blockPos()) - client.level.getSkyDarken());
    };
    public static final Supplier<Number> CLIENT_LIGHT_BLOCK = () -> ComplexData.clientChunk.isEmpty() ? null : client.level.getBrightness(LightLayer.BLOCK, blockPos());
    @Deprecated public static final Supplier<Number> SERVER_LIGHT_SKY = () -> ComplexData.serverChunk == null ? null : serverLighting().getLayerListener(LightLayer.SKY).getLightValue(blockPos());
    @Deprecated public static final Supplier<Number> SERVER_LIGHT_BLOCK = () -> ComplexData.serverChunk == null ? null : serverLighting().getLayerListener(LightLayer.BLOCK).getLightValue(blockPos());

    public static final Supplier<Number> CLIENT_HEIGHT_MAP_SURFACE = () -> chunk(ComplexData.clientChunk, Heightmap.Types.WORLD_SURFACE);
    public static final Supplier<Number> CLIENT_HEIGHT_MAP_MOTION_BLOCKING = () -> chunk(ComplexData.clientChunk, Heightmap.Types.MOTION_BLOCKING);
    public static final Supplier<Number> SERVER_HEIGHT_MAP_SURFACE = () -> chunk(ComplexData.serverChunk, Heightmap.Types.WORLD_SURFACE);
    public static final Supplier<Number> SERVER_HEIGHT_MAP_OCEAN_FLOOR = () -> chunk(ComplexData.serverChunk, Heightmap.Types.OCEAN_FLOOR);
    public static final Supplier<Number> SERVER_HEIGHT_MAP_MOTION_BLOCKING = () -> chunk(ComplexData.serverChunk, Heightmap.Types.MOTION_BLOCKING);
    public static final Supplier<Number> SERVER_HEIGHT_MAP_MOTION_BLOCKING_NO_LEAVES = () -> chunk(ComplexData.serverChunk, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);

    public static final Supplier<Number> WORLD_MIN_Y = () -> ComplexData.world.getMinY();
    public static final Supplier<Number> WORLD_MAX_Y = () -> ComplexData.world.getMaxY();
    public static final Supplier<Number> WORLD_HEIGHT = () -> ComplexData.world.getHeight();
    public static final Supplier<Number> WORLD_COORD_SCALE = () -> ComplexData.world.dimensionType().coordinateScale();;

    public static final Supplier<Number> MOON_PHASE = () -> ComplexData.clientChunk.isEmpty() ? null : (int)(client.level.getOverworldClockTime() / 24000L % 8L) + 1;

    public static final Supplier<Number> SPAWN_CHUNKS = () -> {
        NaturalSpawner.SpawnState info = ComplexData.serverWorld.getChunkSource().getLastSpawnState();
        return info == null ? null : info.getSpawnableChunkCount();
    };
    public static final Supplier<Number> MONSTERS = () -> spawnGroup(MobCategory.MONSTER);
    public static final Supplier<Number> CREATURES = () -> spawnGroup(MobCategory.CREATURE);
    public static final Supplier<Number> AMBIENT_MOBS = () -> spawnGroup(MobCategory.AMBIENT);
    public static final Supplier<Number> WATER_CREATURES = () -> spawnGroup(MobCategory.WATER_CREATURE);
    public static final Supplier<Number> WATER_AMBIENT_MOBS = () -> spawnGroup(MobCategory.WATER_AMBIENT);
    public static final Supplier<Number> UNDERGROUND_WATER_CREATURE = () -> spawnGroup(MobCategory.UNDERGROUND_WATER_CREATURE);
    public static final Supplier<Number> AXOLOTLS = () -> spawnGroup(MobCategory.AXOLOTLS);
    public static final Supplier<Number> MISC_MOBS = () -> spawnGroup(MobCategory.MISC);

    public static final Supplier<Number> JAVA_BIT = () -> 64;
    public static final Supplier<Number> CPU_CORES = () -> ComplexData.cpu == null ? null : ((CentralProcessor)ComplexData.cpu).getPhysicalProcessorCount();
    public static final Supplier<Number> CPU_THREADS = () -> ComplexData.cpu == null ? null : ((CentralProcessor)ComplexData.cpu).getLogicalProcessorCount();

    public static final Supplier<Number> DISPLAY_WIDTH = () -> client.getWindow().getWidth();
    public static final Supplier<Number> DISPLAY_HEIGHT = () -> client.getWindow().getHeight();
    public static final Supplier<Number> DISPLAY_REFRESH_RATE = () -> client.getWindow().getActiveVideoMode().getRefreshRate();
    public static final Supplier<Number> PING = () -> Math.round(ComplexData.pingMetrics[0]);
    public static final Supplier<Number> LATENCY = () -> client.player.connection.getPlayerInfo(client.player.getUUID()).getLatency();
    public static final Supplier<Number> SOLAR_TIME = () -> client.level.getOverworldClockTime() % 24000;
    public static final Supplier<Number> LUNAR_TIME = () -> client.level.getOverworldClockTime();

    public static final Supplier<Number> PARTICLES = () -> client.particleEngine.particles.values().stream().mapToInt(ParticleGroup::size).sum();
    public static final Supplier<Number> STREAMING_SOUNDS = () -> CLIENT.getSoundManager().soundEngine.library.staticChannels.getUsedCount();
    public static final Supplier<Number> MAX_STREAMING_SOUNDS = () -> CLIENT.getSoundManager().soundEngine.library.staticChannels.getMaxCount();
    public static final Supplier<Number> STATIC_SOUNDS = () -> CLIENT.getSoundManager().soundEngine.library.streamingChannels.getUsedCount();
    public static final Supplier<Number> MAX_STATIC_SOUNDS = () -> CLIENT.getSoundManager().soundEngine.library.streamingChannels.getMaxCount();

    public static final Supplier<Number> SLOTS_USED = () -> ComplexData.slots_used;
    public static final Supplier<Number> SLOTS_EMPTY = () -> ComplexData.slots_empty;

    public static final Supplier<Number> FOOD_LEVEL = () -> client.player.getFoodData().getFoodLevel();
    public static final Supplier<Number> SATURATION_LEVEL = () -> client.player.getFoodData().getSaturationLevel();
    public static final Supplier<Number> ARMOR_LEVEL = () -> client.player.getArmorValue();
    public static final Supplier<Number> AIR_LEVEL = () -> Math.round(20F * client.player.getAirSupply() / client.player.getMaxAirSupply());
    public static final Supplier<Number> SCORE = () -> client.player.getScore();
    public static final Supplier<Number> XP_LEVEL = () -> client.player.experienceLevel;
    public static final Supplier<Number> XP_POINTS = () -> client.player.experienceProgress * client.player.getXpNeededForNextLevel();
    public static final Supplier<Number> XP_POINTS_NEEDED = () -> client.player.getXpNeededForNextLevel();
    public static final Supplier<Number> HEALTH = () -> client.player.getHealth() + client.player.getAbsorptionAmount();
    public static final Supplier<Number> HEALTH_MAX = () -> client.player.getMaxHealth();

    public static final Supplier<Number> FOOD_LEVEL_PERCENTAGE = () -> client.player.getFoodData().getFoodLevel() * 5;
    public static final Supplier<Number> SATURATION_LEVEL_PERCENTAGE = () -> client.player.getFoodData().getSaturationLevel() * 5;
    public static final Supplier<Number> ARMOR_LEVEL_PERCENTAGE = () -> client.player.getArmorValue() * 5;

    public static final Supplier<Number> BIOME_BUILDER_EROSION = () -> isNoise() ? biome(sampler().erosion(), par.getErosionThresholds()) : Double.NaN;
    public static final Supplier<Number> BIOME_BUILDER_TEMPERATURE = () -> isNoise() ? biome(sampler().temperature(), par.getTemperatureThresholds()) : Double.NaN;
    public static final Supplier<Number> BIOME_BUILDER_VEGETATION = () -> isNoise() ? biome(sampler().vegetation(), par.getHumidityThresholds()) : Double.NaN;

    public static final Supplier<Number> HOTBAR_SLOT = () -> client.player.getInventory().getSelectedSlot() + 1;
    public static final Supplier<Number> HOTBAR_INDEX = () -> client.player.getInventory().getSelectedSlot();
    @Deprecated public static final Supplier<Number> ITEM_DURABILITY = () -> client.player.getMainHandItem().getMaxDamage() - client.player.getMainHandItem().getDamageValue();
    @Deprecated public static final Supplier<Number> ITEM_MAX_DURABILITY = () -> client.player.getMainHandItem().getMaxDamage();
    @Deprecated public static final Supplier<Number> OFFHAND_ITEM_DURABILITY = () -> client.player.getOffhandItem().getMaxDamage() - client.player.getOffhandItem().getDamageValue();
    @Deprecated public static final Supplier<Number> OFFHAND_ITEM_MAX_DURABILITY = () -> client.player.getOffhandItem().getMaxDamage();

    public static final Supplier<Number> LCPS = () -> ComplexData.clicksPerSeconds[0];
    public static final Supplier<Number> RCPS = () -> ComplexData.clicksPerSeconds[1];

    public static final Supplier<Number> TIME_HOUR_12 = () -> {
        int hour = ComplexData.timeOfDay / 1000 % 12;
        return hour == 0 ? 12 : hour;
    };

    public static final Supplier<Number> UNIX_TIME = System::currentTimeMillis;
    public static final Supplier<Number> REAL_YEAR = () -> LocalDate.now().getYear();
    public static final Supplier<Number> REAL_MONTH = () -> LocalDate.now().getMonthValue();
    public static final Supplier<Number> REAL_DAY = () -> LocalDate.now().getDayOfMonth();
    public static final Supplier<Number> REAL_DAY_OF_WEEK = () -> LocalDate.now().getDayOfWeek().getValue();
    public static final Supplier<Number> REAL_DAY_OF_YEAR = () -> LocalDate.now().getDayOfYear();

    public static final Supplier<Number> REAL_HOUR_24 = () -> LocalTime.now().getHour();
    public static final Supplier<Number> REAL_HOUR_12 = () -> {
        int hour = LocalTime.now().getHour();
        return hour == 0 ? 12 : hour;
    };
    public static final Supplier<Number> REAL_MINUTE = () -> LocalTime.now().getMinute();
    public static final Supplier<Number> REAL_SECOND = () -> LocalTime.now().getSecond();
    public static final Supplier<Number> REAL_MICROSECOND = () -> LocalTime.now().get(MICRO_OF_SECOND);


    public static final Supplier<Number> RESOURCE_PACK_VERSION_MAJOR = () -> SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major();
    public static final Supplier<Number> RESOURCE_PACK_VERSION_MINOR = () -> SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).minor();
    public static final Supplier<Number> DATA_PACK_VERSION_MAJOR = () -> SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major();
    public static final Supplier<Number> DATA_PACK_VERSION_MINOR = () -> SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minor();

    public static final Supplier<Number> MAINHAND_SLOT = () -> CLIENT.player.getInventory().getSelectedSlot();

    public static final Supplier<Number> VILLAGER_LEVEL = () -> ComplexData.targetEntity instanceof Villager ve ? ve.getVillagerData().level() : null;
    public static final Supplier<Number> VILLAGER_XP = () -> ComplexData.targetEntity instanceof Villager ve ? ComplexData.villagerXP - VillagerData.getMinXpPerLevel(ve.getVillagerData().level()) : null;
    public static final Supplier<Number> VILLAGER_XP_NEEDED = () -> ComplexData.targetEntity instanceof Villager ve ? VillagerData.getMaxXpPerLevel(ve.getVillagerData().level()) : null;


}
