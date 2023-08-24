package co.aegisrpg.api.common;

import co.aegisrpg.api.common.utils.Env;
import com.google.gson.GsonBuilder;

import java.util.Optional;
import java.util.UUID;

public abstract class ShieldAPI {
    protected static ShieldAPI instance;

    public ShieldAPI() {
    }

    public static ShieldAPI get() {
        return instance;
    }

    public static <T extends ShieldAPI> Optional<T> getAs(Class<T> clazz) {
        if (clazz.isInstance(instance)) {
            //noinspection unchecked
            return Optional.of((T) instance);
        }
        return Optional.empty();
    }

    public String getAppName() {
        return this.getClass().getSimpleName();
    }

    public UUID getAppUuid() {
        return UUID.nameUUIDFromBytes(this.getAppName().getBytes());
    }

    public abstract Env getEnv();

    public ClassLoader getClassLoader() {
        return null;
    }

    public abstract void shutdown();

    public GsonBuilder getPrettyPrinter() {
        return (new GsonBuilder()).setPrettyPrinting();
    }

    public void sync(Runnable runnable) {
        runnable.run();
    }
}
