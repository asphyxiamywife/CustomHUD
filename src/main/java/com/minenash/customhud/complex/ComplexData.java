package com.minenash.customhud.complex;

import com.minenash.customhud.data.Profile;
import com.minenash.customhud.mixin.accessors.DebugHudAccessor;
import com.minenash.customhud.registry.CustomHudRegistry;
import com.mojang.datafixers.DataFixUtils;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ComplexData {

    public static LevelChunk clientChunk = null;
    public static LevelChunk serverChunk = null;
    public static ServerLevel serverWorld = null;
    public static DifficultyInstance localDifficulty = null;
    public static Level world = null;
    public static BlockPos targetBlockPos = null;
    public static BlockState targetBlock = null;
    public static BlockPos targetFluidPos = null;
    public static FluidState targetFluid = null;
    public static Entity targetEntity = null;
    public static Vec3 targetEntityHitPos = null;
    public static Entity lastHitEntity = null;
    public static double lastHitEntityDist = Double.NaN;
    public static long lastHitEntityTime = -1;
    public static String[] sounds = null;
    public static String[] clientChunkCache = null;
    public static int timeOfDay = -1;
    public static double x1 = 0, y1 = 0, z1 = 0, velocityXZ = 0, velocityY = 0, velocityXYZ = 0;

    private static final Minecraft client = Minecraft.getInstance();
    private static final BlockState AIR_BLOCK_STATE = Blocks.AIR.defaultBlockState();

    //Chunk Data.
    private static ChunkPos pos = null;
    private static CompletableFuture<LevelChunk> chunkFuture;

    public static Object cpu;
    private static long[] prevTicks = new long[CentralProcessor.TickType.values().length];
    public static double cpuLoad = 0;
    public static double gpuUsage = 0;
    private static double gpuUsageSampleSum = 0;
    private static int gpuUsageSampleCount = 0;
    private static long gpuUsageLastUpdate = 0;

    public static int[] clicksSoFar = new int[]{0,0};
    public static int[] clicksPerSeconds = new int[]{0,0};
    public static ArrayDeque<Integer>[] clicks = null;

    public static double[] frameTimeMetrics = new double[4];
    public static double[] tickTimeMetrics = new double[4];
    public static double[] pingMetrics = new double[4];
    public static double[] packetSizeMetrics = new double[4];
    public static double[] tpsMetrics = new double[4];

    public static int slots_used = 0;
    public static int slots_empty = 0;

    private static long lastStatUpdate = 0;

    public static void updateGpuUsage(double usage) {
        if (!Double.isFinite(usage))
            return;

        usage = Math.max(0, Math.min(100, usage));
        gpuUsageSampleSum += usage;
        gpuUsageSampleCount++;

        long now = System.currentTimeMillis();
        if (gpuUsageLastUpdate == 0 || now - gpuUsageLastUpdate >= 250) {
            gpuUsage = gpuUsageSampleSum / gpuUsageSampleCount;
            gpuUsageSampleSum = 0;
            gpuUsageSampleCount = 0;
            gpuUsageLastUpdate = now;
        }
    }

    public static void resetGpuUsage() {
        gpuUsage = 0;
        gpuUsageSampleSum = 0;
        gpuUsageSampleCount = 0;
        gpuUsageLastUpdate = 0;
    }

    public static final Map<UUID, BossEvent> bossbars = new HashMap<>();

    public static MerchantOffers villagerOffers = new MerchantOffers();
    public static int villagerXP = 0;
    public static UUID villagerUUID = null;
    public static int fakeVillagerInteract = 0;
    public static long villagerLastRequested = Long.MAX_VALUE;

    public static boolean refreshTimings = false;
    public record ProfilerTimingWithPath(String path, String name, double parent, double total, int color, List<ProfilerTimingWithPath> entries) {}
    public static List<ProfilerTimingWithPath> rootEntries = Collections.EMPTY_LIST;
    public static Map<String,ProfilerTimingWithPath> allEntries = Collections.EMPTY_MAP;

    @SuppressWarnings("ConstantConditions")
    public static void update(Profile profile) {

        Profiler.get().push("custom_hud_complex_data");
        if (profile.enabled.serverWorld) {
            Profiler.get().push("serverWorld");
            IntegratedServer integratedServer = client.getSingleplayerServer();
            serverWorld = integratedServer != null ? integratedServer.getLevel(client.level.dimension()) : null;
            Profiler.get().pop();
        }

        if (profile.enabled.clientChunk) {
            Profiler.get().push("clientChunk");
            ChunkPos newPos = ChunkPos.containing(client.getCameraEntity().blockPosition());
            if (!Objects.equals(ComplexData.pos,newPos)) {
                pos = newPos;
                chunkFuture = null;
                clientChunk = null;
            }
            if (clientChunk == null)
                clientChunk = client.level.getChunk(pos.x(), pos.z());
            Profiler.get().pop();
        }

        if (profile.enabled.serverChunk) {
            Profiler.get().push("serverChunk");
            if (chunkFuture == null) {
                if (serverWorld != null)
                    chunkFuture = serverWorld.getChunkSource().getChunkFuture(pos.x(), pos.z(), ChunkStatus.FULL, false).thenApply((either) -> (LevelChunk) either.orElse(null));

                if (chunkFuture == null)
                    chunkFuture = CompletableFuture.completedFuture(clientChunk);
            }
            serverChunk = chunkFuture.getNow(null);
            Profiler.get().pop();
        }

        if (profile.enabled.world) {
            Profiler.get().push("world");
            world = DataFixUtils.orElse(Optional.ofNullable(client.getSingleplayerServer()).flatMap((integratedServer) -> Optional.ofNullable(integratedServer.getLevel(client.level.dimension()))), client.level);
            Profiler.get().pop();
        }

        if (profile.enabled.targetBlock) {
            Profiler.get().push("targetBlock");
            HitResult hit =  client.getCameraEntity().pick(profile.targetDistance, 0.0F, false);

            if (hit.getType() == HitResult.Type.BLOCK) {
                targetBlockPos = ((BlockHitResult)hit).getBlockPos();
                targetBlock = world.getBlockState(targetBlockPos);
            }
            else {
                targetBlockPos = null;
                targetBlock = AIR_BLOCK_STATE;
            }
            Profiler.get().pop();
        }

        if (profile.enabled.targetFluid) {
            Profiler.get().push("targetFluid");
            HitResult hit =  client.getCameraEntity().pick(profile.targetDistance, 0.0F, true);

            if (hit.getType() == HitResult.Type.BLOCK) {
                targetFluidPos = ((BlockHitResult)hit).getBlockPos();
                targetFluid = world.getFluidState(targetFluidPos);
            }
            else {
                targetFluidPos = null;
                targetFluid = Fluids.EMPTY.defaultFluidState();
            }

            Profiler.get().pop();
        }

        if (profile.enabled.targetEntity) {
            Profiler.get().push("targetEntity");
            double dist = profile.targetDistance;

            Vec3 min = client.getCameraEntity().getEyePosition(0);
            Vec3 rot = client.getCameraEntity().getViewVector(1.0F);
            Vec3 max = min.add(rot.x * dist, rot.y * dist, rot.z * dist);
            AABB box = client.getCameraEntity().getBoundingBox().expandTowards(rot.scale(dist)).inflate(1.0, 1.0, 1.0);

            HitResult block = client.getCameraEntity().pick(dist, 0, false);
            double dist2 = block == null ? dist*dist : block.getLocation().distanceToSqr(min);

            EntityHitResult result = ProjectileUtil.getEntityHitResult(client.getCameraEntity(), min, max, box, (en) -> !en.isSpectator(), dist2);
            targetEntity = result == null ? null : result.getEntity();
            targetEntityHitPos = result == null ? null : result.getLocation();
            Profiler.get().pop();
        }

        if (profile.enabled.localDifficulty) {
            Profiler.get().push("localDifficulty");
            localDifficulty = new DifficultyInstance(world.getDifficulty(), world.getOverworldClockTime(),
                    serverChunk == null ? 0 : serverChunk.getInhabitedTime(), world.getOverworldClockTime());
            Profiler.get().pop();
        }

        if (profile.enabled.sound) {
            Profiler.get().push("sound");
            sounds = new String[] {"0", "0"};
            Profiler.get().pop();
        }

        if (profile.enabled.time) {
            Profiler.get().push("time");
            timeOfDay = (int) ((client.level.getOverworldClockTime() + 6000) % 24000);
            Profiler.get().pop();
        }

        if (!profile.enabled.velocityTrackers.isEmpty()) {
            Profiler.get().push("velocities");
            for (var v : profile.enabled.velocityTrackers)
                v.tick();
            VelocityTracker.recordCords();
            Profiler.get().pop();
        }

        if (profile.enabled.cpu) {
            if (cpu == null)
                cpu = new SystemInfo().getHardware().getProcessor();
        }
        if (profile.enabled.cpuUsage) {
            Profiler.get().push("cpu");
            var c = (CentralProcessor) cpu;
            double load = c.getSystemCpuLoadBetweenTicks( prevTicks ) * 100;
            if (load > 0)
                cpuLoad = load;
            prevTicks = c.getSystemCpuLoadTicks();
            Profiler.get().pop();
        }

        if (profile.enabled.updateStats) {
            Profiler.get().push("updateStats");
            if (System.currentTimeMillis() - lastStatUpdate >= 500) {
                client.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
                lastStatUpdate = System.currentTimeMillis();
            }
            Profiler.get().pop();
        }


        if (profile.enabled.clicksPerSeconds) {
            Profiler.get().push("clicksPerSeconds");
            if (clicks == null) {
                clicks = new ArrayDeque[]{new ArrayDeque<Integer>(20), new ArrayDeque<Integer>(20)};
                for (int i = 0; i < 20; i++) {
                    clicks[0].add(0);
                    clicks[1].add(0);
                }
            }

            clicks[0].remove();
            clicks[1].remove();
            clicks[0].add(clicksSoFar[0]);
            clicks[1].add(clicksSoFar[1]);
            clicksSoFar[0] = 0;
            clicksSoFar[1] = 0;
            clicksPerSeconds[0] = clicks[0].stream().reduce(0, Integer::sum);
            clicksPerSeconds[1] = clicks[1].stream().reduce(0, Integer::sum);
            Profiler.get().pop();
        }

        if (profile.enabled.frameMetrics) {
            Profiler.get().push("frameMetrics");
            processLog(((DebugHudAccessor)client.getDebugOverlay()).getFrameTimeLogger(), 0.000001, 240, frameTimeMetrics);
            Profiler.get().pop();
        }
        if (profile.enabled.tickMetrics) {
            Profiler.get().push("tickMetrics");
            processLog(((DebugHudAccessor)client.getDebugOverlay()).getTickTimeLogger(), 0.000001, 120, tickTimeMetrics);
            Profiler.get().pop();
        }
        if (profile.enabled.pingMetrics) {
            Profiler.get().push("pingMetrics");
            processLog(client.getDebugOverlay().getPingLogger(), 1, 120, pingMetrics);
            Profiler.get().pop();
        }
        if (profile.enabled.packetMetrics) {
            Profiler.get().push("packetMetrics");
            processLog(client.getDebugOverlay().getBandwidthLogger(), 20/1024D, 120, packetSizeMetrics);
            Profiler.get().pop();
        }
        if (profile.enabled.tpsMetrics) {
            Profiler.get().push("tpsMetrics");
            processTPSLog(((DebugHudAccessor)client.getDebugOverlay()).getTickTimeLogger(), tpsMetrics);
            Profiler.get().pop();
        }

        if (profile.enabled.slots) {
            Profiler.get().push("slots");
            slots_used = slots_empty = 0;
            NonNullList<ItemStack> inv = client.player.getInventory().getNonEquipmentItems();
            for (ItemStack itemStack : inv) {
                if (itemStack == ItemStack.EMPTY)
                    slots_empty++;
                else
                    slots_used++;
            }
            Profiler.get().pop();
        }

        if (profile.enabled.music) {
            Profiler.get().push("music");
            MusicAndRecordTracker.tick();
            Profiler.get().pop();
        }

        if (profile.enabled.targetVillager) {
            Profiler.get().push("targetVillager");
            if ( !(targetEntity instanceof Villager) && villagerUUID != null) {
                villagerOffers.clear();
                villagerUUID = null;
                villagerLastRequested = Long.MAX_VALUE;
            }
            else if (targetEntity instanceof Villager && (villagerUUID == null ||
                    !targetEntity.getUUID().equals(villagerUUID) || System.currentTimeMillis() - villagerLastRequested > 30_000)) {
                villagerUUID = targetEntity.getUUID();
                fakeVillagerInteract = 2;
                CLIENT.getConnection().send(new ServerboundInteractPacket(targetEntity.getId(), InteractionHand.OFF_HAND, Vec3.ZERO, false));
                villagerLastRequested = System.currentTimeMillis();
            }
            Profiler.get().pop();
        }

        if (profile.enabled.profilerTimings) {
            Profiler.get().push("profilerTimings");
            ProfileResults profileResult = CLIENT.getDebugOverlay().getProfilerPieChart().profilerPieChartResults;
            if (profileResult == null) {
                rootEntries = Collections.EMPTY_LIST;
                allEntries = Collections.EMPTY_MAP;
            }
            else {
                rootEntries = new ArrayList<>();
                allEntries = new HashMap<>();
                List<ResultField> timings = profileResult.getTimes("root");
                timings.remove(0);
                for (var entry : timings)
                    rootEntries.add( getEntries(profileResult, entry, "root\u001e" + entry.name) );
            }
            Profiler.get().pop();
        }

        SubtitleTracker.INSTANCE.setEnable(profile.enabled.subtitles);
        Profiler.get().push("registry");
        CustomHudRegistry.runComplexData(profile.enabled);
        Profiler.get().pop();
        Profiler.get().pop();
    }

    public static ProfilerTimingWithPath getEntries(ProfileResults profileResult, ResultField timing, String path) {
        List<ProfilerTimingWithPath> entries = new ArrayList<>();
        List<ResultField> timings = profileResult.getTimes(path);
        timings.remove(0);
        for (var entry : timings)
            entries.add(getEntries(profileResult, entry, path + "\u001e" + entry.name));

        ProfilerTimingWithPath entry = new ProfilerTimingWithPath(path, timing.name, timing.percentage, timing.globalPercentage, timing.getColor(), entries);
        allEntries.put(path, entry);
        return entry;
    }

    public static void processLog(LocalSampleLogger log, double multiplier, int samples, double[] metrics) {
        if (log.size() == 0) {
            metrics[0] = metrics[1] = metrics[2] = metrics[3] = Double.NaN;
            return;
        }

        metrics[0] = 0; //AVG
        metrics[1] = Integer.MAX_VALUE; //MIN
        metrics[2] = Integer.MIN_VALUE; //MAX
        metrics[3] = Math.min(samples, log.size()-1); //SAMPLES

        double avg = 0L;
        for (int r = 0; r <  metrics[3]; ++r) {
            double s = log.get(r) * multiplier;
            metrics[1] = Math.min(metrics[1], s);
            metrics[2] = Math.max(metrics[2], s);
            avg += s;
        }
        metrics[0] = avg / metrics[3];
    }

    public static void processTPSLog(LocalSampleLogger log, double[] metrics) {
        if (log.size() == 0) {
            metrics[0] = metrics[1] = metrics[2] = metrics[3] = Double.NaN;
            return;
        }

        metrics[0] = 0; //AVG
        metrics[1] = Integer.MAX_VALUE; //MIN
        metrics[2] = Integer.MIN_VALUE; //MAX
        metrics[3] = Math.min(120, log.size()-1); //SAMPLES

        double avg = 0L;
        for (int r = 0; r <  metrics[3]; ++r) {
            double s = Math.min(20, 1000F / (log.get(r) * 0.000001));
            metrics[1] = Math.min(metrics[1], s);
            metrics[2] = Math.max(metrics[2], s);
            avg += s;
        }
        metrics[0] = avg / metrics[3];
    }

    public static void reset() {
        clientChunk = null;
        serverChunk = null;
        serverWorld = null;
        localDifficulty = null;
        world = null;
        sounds = null;
        clientChunkCache = null;
        clicks = null;
        frameTimeMetrics = new double[4];
        tickTimeMetrics = new double[4];
        pingMetrics = new double[4];
        packetSizeMetrics = new double[4];
        x1 = y1 = z1 = velocityXZ = velocityY = velocityXYZ = 0;
        slots_used = slots_empty = 0;
        clicksSoFar[0] = clicksSoFar[1] = 0;
        clicksPerSeconds[0] = clicksPerSeconds[1] = 0;
    }

    public static class Enabled {
        public static final Enabled DISABLED = new Enabled();
        public final Map<String,Boolean> custom = new HashMap<>();

        public final List<VelocityTracker> velocityTrackers = new ArrayList<>();

        public boolean clientChunk = false;
        public boolean serverChunk = false;
        public boolean serverWorld = false;
        public boolean localDifficulty = false;
        public boolean world = false;
        public boolean sound = false;
        public boolean targetBlock = false;
        public boolean targetFluid = false;
        public boolean targetEntity = false;
        public boolean time = false;
        public boolean velocity = false;
        public boolean cpu = false;
        public boolean cpuUsage = false;
        public boolean updateStats = false;
        public boolean clicksPerSeconds = false;
        public boolean music = false;
        public boolean subtitles = false;

        public boolean gpuMetrics = false;
        public boolean frameMetrics = false;
        public boolean tickMetrics = false;
        public boolean tpsMetrics = false;
        public boolean pingMetrics = false;
        public boolean packetMetrics = false;
        public boolean profilerTimings = false;

        public boolean slots = false;
        public boolean targetVillager = false;

        public void merge(Enabled enabled) {
            for (Field field : this.getClass().getFields()) {
                if (field.getType() != Boolean.TYPE) continue;
                try { field.setBoolean(this, field.getBoolean(this) || field.getBoolean(enabled)); }
                catch (Exception ignored) {}
            }
            this.custom.putAll(enabled.custom);


            this.velocityTrackers.addAll(enabled.velocityTrackers);
        }

        public boolean get(String name) {
            return custom.getOrDefault(name, false);
        }
        public void set(String name) {
            custom.put(name, true);
        }
        public void set(String name, boolean value) {
            custom.put(name, value);
        }
    }



}
