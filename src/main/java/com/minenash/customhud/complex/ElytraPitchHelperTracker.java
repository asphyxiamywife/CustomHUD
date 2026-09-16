package com.minenash.customhud.complex;

import org.apache.logging.log4j.core.LogEvent;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ElytraPitchHelperTracker {

    private static final String LOGGER_NAME = "elytrapitchhelper";
    private static final String NUMBER = "([+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+))";
    private static final Pattern CYCLE = Pattern.compile(
            "^cycle dy=" + NUMBER + " \\([^)]*\\) dx=" + NUMBER + " \\(" + NUMBER + "/s\\) "
                    + "over (\\d+)t \\([^)]*\\) swing=" + NUMBER
                    + " glyphs=(?:(\\d+)/(\\d+) reached|none)(?:,|\\s|$)");
    private static final Pattern GLYPH_RESULT = Pattern.compile(
            "^glyph-result #\\d+ action=(pull-up|release-down) reached target in "
                    + "(\\d+)t \\(" + NUMBER + "s\\)(?:\\s|$)");

    private static volatile Cycle latestCycle;
    private static volatile GlyphResult latestPullUp;
    private static volatile GlyphResult latestReleaseDown;

    public static final Supplier<Number> CYCLE_DY = () -> value(Cycle::dy);
    public static final Supplier<Number> CYCLE_DX = () -> value(Cycle::dx);
    public static final Supplier<Number> CYCLE_DX_PER_SECOND = () -> value(Cycle::dxPerSecond);
    public static final Supplier<Number> CYCLE_TICKS = () -> value(Cycle::ticks);
    public static final Supplier<Number> CYCLE_SECONDS = () -> value(cycle -> cycle.ticks / 20.0);
    public static final Supplier<Number> SWING = () -> value(Cycle::swing);
    public static final Supplier<Number> GLYPHS_REACHED = () -> value(Cycle::glyphsReached);
    public static final Supplier<Number> PULL_UP_TICKS = () -> glyphValue(latestPullUp, GlyphResult::ticks);
    public static final Supplier<Number> PULL_UP_SECONDS = () -> glyphValue(latestPullUp, GlyphResult::seconds);
    public static final Supplier<Number> RELEASE_DOWN_TICKS = () -> glyphValue(latestReleaseDown, GlyphResult::ticks);
    public static final Supplier<Number> RELEASE_DOWN_SECONDS = () -> glyphValue(latestReleaseDown, GlyphResult::seconds);

    private ElytraPitchHelperTracker() {}

    public static void initialize() {
        Log4jRecordListener.register(ElytraPitchHelperTracker::onLogRecord);
    }

    public static void reset() {
        latestCycle = null;
        latestPullUp = null;
        latestReleaseDown = null;
    }

    private static Number value(java.util.function.Function<Cycle, Number> getter) {
        Cycle cycle = latestCycle;
        return cycle == null ? null : getter.apply(cycle);
    }

    private static Number glyphValue(GlyphResult result,
            java.util.function.Function<GlyphResult, Number> getter) {
        return result == null ? null : getter.apply(result);
    }

    private static void onLogRecord(LogEvent event) {
        if (!LOGGER_NAME.equals(event.getLoggerName()) || event.getMessage() == null)
            return;

        String message = event.getMessage().getFormattedMessage();
        if (message.startsWith("diagnostics enabled")) {
            reset();
            return;
        }

        Matcher matcher = CYCLE.matcher(message);
        if (matcher.find()) {
            latestCycle = new Cycle(
                    Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Double.parseDouble(matcher.group(5)),
                    matcher.group(6) == null ? 0 : Integer.parseInt(matcher.group(6)));
            return;
        }

        matcher = GLYPH_RESULT.matcher(message);
        if (matcher.find()) {
            GlyphResult result = new GlyphResult(
                    Integer.parseInt(matcher.group(2)), Double.parseDouble(matcher.group(3)));
            if (matcher.group(1).equals("pull-up"))
                latestPullUp = result;
            else
                latestReleaseDown = result;
        }
    }

    private record Cycle(double dy, double dx, double dxPerSecond, int ticks, double swing,
            int glyphsReached) {}
    private record GlyphResult(int ticks, double seconds) {}
}
