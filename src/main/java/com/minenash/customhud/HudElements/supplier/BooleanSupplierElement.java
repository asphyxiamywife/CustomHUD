package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.minenash.customhud.mixin.accessors.PlayerListHudAccess;
import java.time.LocalTime;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class BooleanSupplierElement implements HudElement {

    private static final Minecraft client = Minecraft.getInstance();
    private static boolean isInDim(Identifier id) { return client.level.dimension().identifier().equals(id); }
    protected static BlockPos blockPos() { return client.getCameraEntity().blockPosition(); }


//    public static final Supplier<Boolean> NEW_RENDERER = () -> CustomHud.useNewRenderer;


    public static final Supplier<Boolean> PROFILE_IN_CYCLE = () -> ProfileManager.getActive() != null && ProfileManager.getActive().cycle;

    public static final Supplier<Boolean> VSYNC = () -> client.options.enableVsync().get();

    public static final Supplier<Boolean> SINGLEPLAYER = client::isLocalServer;
    public static final Supplier<Boolean> MULTIPLAYER = () -> !client.isLocalServer();

    public static final Supplier<Boolean> SURVIVAL = () -> client.gameMode.getPlayerMode() == GameType.SURVIVAL;
    public static final Supplier<Boolean> CREATIVE = () -> client.gameMode.getPlayerMode() == GameType.CREATIVE;
    public static final Supplier<Boolean> ADVENTURE = () -> client.gameMode.getPlayerMode() == GameType.ADVENTURE;
    public static final Supplier<Boolean> SPECTATOR = () -> client.gameMode.getPlayerMode() == GameType.SPECTATOR;

    public static final Supplier<Boolean> CHUNK_CULLING = () -> client.smartCull;
    public static final Supplier<Boolean> IN_OVERWORLD = () -> isInDim(Level.OVERWORLD.identifier());
    public static final Supplier<Boolean> IN_NETHER = () -> isInDim(Level.NETHER.identifier());
    public static final Supplier<Boolean> IN_END = () -> isInDim(Level.END.identifier());

    public static final Supplier<Boolean> IS_RAINING = () -> ComplexData.world.isRaining();
    public static final Supplier<Boolean> IS_THUNDERING = () -> ComplexData.world.isThundering();
    public static final Supplier<Boolean> IS_SNOWING = () -> ComplexData.world.isRaining() && ComplexData.world.getBiome(client.player.blockPosition()).value().getPrecipitationAt(client.player.blockPosition(), client.level.getSeaLevel()) == Biome.Precipitation.SNOW;
    public static final Supplier<Boolean> IS_SLIME_CHUNK = () -> WorldgenRandom.seedSlimeChunk(blockPos().getX() >> 4, blockPos().getZ() >> 4, ((WorldGenLevel)ComplexData.world).getSeed(), 987234911L).nextInt(10) == 0;

    public static final Supplier<Boolean> SPRINTING = () -> client.player.isSprinting() && !client.player.isSwimming();
    public static final Supplier<Boolean> SNEAKING = () -> client.player.isShiftKeyDown();
    public static final Supplier<Boolean> SWIMMING = () -> client.player.isSwimming();
    public static final Supplier<Boolean> FLYING = () -> client.player.getAbilities().flying;
    public static final Supplier<Boolean> FALLING_WITH_STYLE = () -> client.player.isFallFlying();
    public static final Supplier<Boolean> ON_GROUND = () -> client.player.onGround();
    public static final Supplier<Boolean> SPRINT_HELD = () -> client.options.keySprint.isDown();

    public static final Supplier<Boolean> IS_FROZEN = () -> client.player.isFullyFrozen();
    public static final Supplier<Boolean> IS_FREEZING = () -> client.player.getTicksFrozen() > 0;
    public static final Supplier<Boolean> IS_ON_FIRE = () -> client.player.isOnFire();

    // ADD: onFire et al

    public static final Supplier<Boolean> HUD_HIDDEN = () -> client.options.hideGui;
    public static final Supplier<Boolean> SCREEN_OPEN = () -> client.screen != null;
    public static final Supplier<Boolean> CHAT_OPEN = () -> client.screen instanceof ChatScreen;
    public static final Supplier<Boolean> PLAYER_LIST_OPEN = () -> ((PlayerListHudAccess)client.gui.getTabList()).getVisible();

    public static final Supplier<Boolean> WINDOW_FOCUSED = client::isWindowActive;

    public static final Supplier<Boolean> RECORD_PLAYING = () -> MusicAndRecordTracker.isRecordPlaying;
    public static final Supplier<Boolean> MUSIC_PLAYING = () -> MusicAndRecordTracker.isMusicPlaying;

    public static final Supplier<Boolean> FISHING_IS_CAST = () -> client.player.fishing != null;
    public static final Supplier<Boolean> FISHING_IS_HOOKED = () -> client.player.fishing != null && client.player.fishing.getHookedIn() != null;
    public static final Supplier<Boolean> FISHING_HAS_CAUGHT = () -> client.player.fishing != null && client.player.fishing.getEntityData().get(FishingHook.DATA_BITING);
    public static final Supplier<Boolean> FISHING_IN_OPEN_WATER = () -> client.player.fishing != null && client.player.fishing.calculateOpenWater(client.player.fishing.blockPosition());

    public static final Supplier<Boolean> HAS_NOISE = () -> ComplexData.serverWorld.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator;
    public static final Supplier<Boolean> IS_TICK_SPRINTING = () -> client.getSingleplayerServer() != null ? client.getSingleplayerServer().tickRateManager().isSprinting() : null;
    public static final Supplier<Boolean> IS_TICK_FROZEN = () -> client.getSingleplayerServer() != null ? client.getSingleplayerServer().tickRateManager().isFrozen() : client.level.tickRateManager().isFrozen();
    public static final Supplier<Boolean> IS_TICK_STEPPING = () -> client.getSingleplayerServer() != null ? client.getSingleplayerServer().tickRateManager().isSteppingForward() : client.level.tickRateManager().isSteppingForward();

    public static final Supplier<Boolean> ON_LOAD = () -> ProfileManager.getActive().boolEvents.contains("load");
    public static final Supplier<Boolean> ON_JOIN = () -> ProfileManager.getActive().boolEvents.contains("join");

    public static final Supplier<Boolean> REAL_AM = () -> LocalTime.now().getHour() < 12;
    public static final Supplier<Boolean> REAL_PM = () -> LocalTime.now().getHour() >= 12;


    @Deprecated public static final Supplier<Boolean> ITEM_HAS_DURABILITY = () -> client.player.getMainHandItem().getMaxDamage() > 0;
    @Deprecated public static final Supplier<Boolean> OFFHAND_ITEM_HAS_DURABILITY = () -> client.player.getOffhandItem().getMaxDamage() > 0;

    private final Supplier<Boolean> supplier;

    public BooleanSupplierElement(Supplier<Boolean> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String getString() {
        return sanitize(supplier, false) ? "true" : "false";
    }

    @Override
    public Number getNumber() {
        return sanitize(supplier, false) ? 1 : 0;
    }

    @Override
    public boolean getBoolean() {
        return sanitize(supplier, false);
    }
}
