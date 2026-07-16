package com.minenash.customhud.complex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Util;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL15C;

public class OpenGlGpuTimer implements AutoCloseable {

    private static final int QUERY_COUNT = 3;

    private final int[] queries = new int[QUERY_COUNT];
    private final boolean[] pending = new boolean[QUERY_COUNT];
    private final long[] cpuDurations = new long[QUERY_COUNT];

    private int nextQuery = 0;
    private int activeQuery = -1;
    private long queryStart = 0;
    private long lastFrameEnd = 0;

    public OpenGlGpuTimer() {
        GL15C.glGenQueries(queries);
    }

    public static boolean isSupported() {
        return "OpenGL".equals(RenderSystem.getDevice().getDeviceInfo().backendName());
    }

    public double beginFrame() {
        double usage = poll();

        if (activeQuery != -1 || GL15C.glGetQueryi(ARBTimerQuery.GL_TIME_ELAPSED, GL15C.GL_CURRENT_QUERY) != 0)
            return usage;

        int query = findAvailableQuery();
        if (query == -1)
            return usage;

        GL15C.glBeginQuery(ARBTimerQuery.GL_TIME_ELAPSED, queries[query]);
        activeQuery = query;
        queryStart = Util.getNanos();
        return usage;
    }

    public void endFrame() {
        long now = Util.getNanos();

        if (activeQuery != -1) {
            GL15C.glEndQuery(ARBTimerQuery.GL_TIME_ELAPSED);
            cpuDurations[activeQuery] = lastFrameEnd == 0 ? now - queryStart : now - lastFrameEnd;
            pending[activeQuery] = true;
            activeQuery = -1;
        }

        lastFrameEnd = now;
    }

    private int findAvailableQuery() {
        for (int i = 0; i < QUERY_COUNT; i++) {
            int query = (nextQuery + i) % QUERY_COUNT;
            if (!pending[query]) {
                nextQuery = (query + 1) % QUERY_COUNT;
                return query;
            }
        }
        return -1;
    }

    private double poll() {
        double usage = 0;
        int samples = 0;

        for (int i = 0; i < QUERY_COUNT; i++) {
            if (!pending[i] || GL15C.glGetQueryObjecti(queries[i], GL15C.GL_QUERY_RESULT_AVAILABLE) == 0)
                continue;

            long gpuDuration = ARBTimerQuery.glGetQueryObjecti64(queries[i], GL15C.GL_QUERY_RESULT);
            if (gpuDuration > 0 && cpuDurations[i] > 0) {
                usage += gpuDuration * 100D / cpuDurations[i];
                samples++;
            }
            pending[i] = false;
        }

        return samples == 0 ? Double.NaN : usage / samples;
    }

    @Override
    public void close() {
        if (activeQuery != -1) {
            GL15C.glEndQuery(ARBTimerQuery.GL_TIME_ELAPSED);
            activeQuery = -1;
        }
        GL15C.glDeleteQueries(queries);
    }
}
