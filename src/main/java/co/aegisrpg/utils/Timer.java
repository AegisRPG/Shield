package co.aegisrpg.utils;

import co.aegisrpg.Shield;
import lombok.Getter;

public class Timer {
    private static final int IGNORE = 1000;

    @Getter
    private final long duration;

    public Timer(String id, Runnable runnable) {
        this(id, null, runnable);
    }

    public Timer(String id, Boolean debug, Runnable runnable) {
        long startTime = System.currentTimeMillis();

        runnable.run();

        duration = System.currentTimeMillis() - startTime;

        if (debug == null ? Shield.isDebug() : debug || duration > IGNORE)
            Shield.log("[Timer] " + id + " took " + duration + "ms");
    }
}
