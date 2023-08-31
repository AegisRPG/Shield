package co.aegisrpg;

import co.aegisrpg.api.API;
import co.aegisrpg.api.common.utils.EnumUtils;
import co.aegisrpg.api.common.utils.Env;
import co.aegisrpg.api.common.utils.ReflectionUtils;
import co.aegisrpg.api.common.utils.Utils;
import co.aegisrpg.utils.WorldGuardFlagUtils.CustomFlags;
import lombok.Getter;
import lombok.Setter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class Shield extends JavaPlugin {

    // TODO: Uncomment when features/commands area is created

//    @Getter
//    private Commands commands;
//    @Getter
//    private Features features;
    private static Shield instance;
    @Getter
    private static Thread thread;
    public static final LocalDateTime EPOCH = LocalDateTime.now();
    @Getter
    private final static HeadDatabaseAPI headAPI = new HeadDatabaseAPI();
    private static API api;
    public static final String DOMAIN = "play.aegisrpg.co";

    public static Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public static <T> T singletonOf(Class<T> clazz) {
        return (T) singletons.computeIfAbsent(clazz, $ -> {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
                Shield.log(Level.FINE, "Failed to create singleton of " + clazz.getName() + ", falling back to Objenesis", ex);
                try {
                    return new ObjenesisStd().newInstance(clazz);
                } catch (Throwable t) {
                    throw new IllegalStateException("Failed to create singleton of " + clazz.getName() + " using Objenesis", t);
                }
            }
        });
    }

    static {
        Locale.setDefault(Locale.US);
    }

    public Shield() {
        if (instance == null) {
            instance = this;
            thread = Thread.currentThread();
        } else
            Bukkit.getServer().getLogger().info("Shield could not be initialized: Instance is not null but is: " + instance.getClass().getName());

        api = new API();
    }

    public static Shield getInstance() {
        if (instance == null)
            Bukkit.getServer().getLogger().info("Shield could not be initialized");
        return instance;
    }

    public static Env getEnv() {
        String env = getInstance().getConfig().getString("env", Env.DEV.name()).toUpperCase();
        try {
            return Env.valueOf(env);
        } catch (IllegalArgumentException ex) {
            Shield.severe("Could not parse environment variable " + env + ", options are: " + EnumUtils.valueNamesPretty(Env.class));
            Shield.severe("Defaulting to " + Env.DEV.name() + " environment");
            return Env.DEV;
        }
    }

    @Getter
    @Setter
    private static boolean debug = false;

    public static void debug(String message) {
        if (debug)
            getInstance().getLogger().info("[DEBUG] " + ChatColor.stripColor(message));
    }

    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static void log(String message, Throwable ex) {
        log(Level.INFO, message, ex);
    }

    public static void warn(String message) {
        log(Level.WARNING, message);
    }

    public static void warn(String message, Throwable ex) {
        log(Level.WARNING, message, ex);
    }

    public static void severe(String message) {
        log(Level.SEVERE, message);
    }

    public static void severe(String message, Throwable ex) {
        log(Level.SEVERE, message, ex);
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    public static void log(Level level, String message, Throwable ex) {
        getInstance().getLogger().log(level, ChatColor.stripColor(message), ex);
    }

    @Getter
    private static final List<Listener> listeners = new ArrayList<>();
    @Getter
    private static final List<TemporaryListener> temporaryListeners = new ArrayList<>();
    @Getter
    private static final List<Class<? extends Event>> eventHandlers = new ArrayList<>();

    public static void registerTemporaryListener(TemporaryListener listener) {
        registerListener(listener);
        temporaryListeners.add(listener);
    }

    public static void unregisterTemporaryListener(TemporaryListener listener) {
        listener.unregister();
        unregisterListener(listener);
        temporaryListeners.remove(listener);
    }

    public static void registerListener(Listener listener) {
        if (!Utils.canEnable(listener.getClass()))
            return;

        final boolean isTemporary = listener instanceof TemporaryListener;
        if (listeners.contains(listener) && !isTemporary) {
            Shield.debug("Ignoring duplicate listener registration for class " + listener.getClass().getSimpleName());
            return;
        }

        Shield.debug("Registering listener: " + listener.getClass().getName());
        if (getInstance().isEnabled()) {
            getInstance().getServer().getPluginManager().registerEvents(listener, getInstance());
            listeners.add(listener);
            if (!isTemporary)
                for (Method method : ReflectionUtils.methodsAnnotatedWith(listener.getClass(), EventHandler.class))
                    eventHandlers.add((Class<? extends Event>) method.getParameters()[0].getType());
        } else log("Could not register listener " + listener.getClass().getName() + "!");
    }

    public static void unregisterListener(Listener listener) {
        try {
            HandlerList.unregisterAll(listener);
            listeners.remove(listener);
        } catch (Exception ex) {
            log("Could not unregister listener " + listener.toString() + "!");
            ex.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        CustomFlags.register();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
