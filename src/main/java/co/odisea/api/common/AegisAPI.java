package co.odisea.api.common;

import co.odisea.api.common.utils.Env;
import com.google.gson.GsonBuilder;

import java.util.Optional;
import java.util.UUID;

public abstract class AegisAPI {
    protected static AegisAPI instance;

    public static AegisAPI get() {
        return instance;
    }

    public static <T extends AegisAPI> Optional<T> getAs(Class<T> clazz) {
        if (clazz.isInstance(instance)) {
            //noinspection unchecked
            return Optional.of((T) instance);
        }
        return Optional.empty();
    }

    public String getAppName() {
        return getClass().getSimpleName();
    }

    public UUID getAppUuid() {
        return UUID.nameUUIDFromBytes(getAppName().getBytes());
    }

    abstract public Env getEnv();

    public ClassLoader getClassLoader() {
        return null;
    }

    public abstract void shutdown();

    public GsonBuilder getPrettyPrinter() {
        return new GsonBuilder().setPrettyPrinting();
    }

    public void sync(Runnable runnable) {
        runnable.run();
    }

}