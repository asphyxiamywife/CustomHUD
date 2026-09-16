package com.minenash.customhud.complex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Makes Log4j records produced inside the client process available to trackers.
 */
public final class Log4jRecordListener {

    private static final List<Consumer<LogEvent>> LISTENERS = new CopyOnWriteArrayList<>();
    private static boolean initialized;

    private Log4jRecordListener() {}

    public static synchronized void initialize() {
        if (initialized)
            return;

        var appender = new AbstractAppender(
                "CustomHudInProcessRecords", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                for (Consumer<LogEvent> listener : LISTENERS) {
                    try {
                        listener.accept(event);
                    } catch (RuntimeException ignored) {
                        // A malformed third-party record must not interfere with logging.
                    }
                }
            }
        };
        appender.start();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);
        initialized = true;
    }

    public static void register(Consumer<LogEvent> listener) {
        LISTENERS.add(listener);
    }
}
