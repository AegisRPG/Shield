package co.odisea.hooks;

import co.odisea.hooks.citizens.CitizensHook;
import co.odisea.hooks.citizens.CitizensHookImpl;
import co.odisea.hooks.glowapi.GlowAPIHook;
import co.odisea.hooks.glowapi.GlowAPIHookImpl;
import co.odisea.hooks.viaversion.ViaVersionHook;
import co.odisea.hooks.viaversion.ViaVersionHookImpl;
import co.odisea.utils.Utils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import static co.odisea.Shield.singletonOf;

@Getter
public class Hook {
    public static final ViaVersionHook VIAVERSION = hook("ViaVersion", ViaVersionHook.class, ViaVersionHookImpl.class);
    public static final GlowAPIHook GLOWAPI = hook("GlowAPI", GlowAPIHook.class, GlowAPIHookImpl.class);
    public static final CitizensHook CITIZENS = hook("Citizens", CitizensHook.class, CitizensHookImpl.class);

    @SneakyThrows
    private static <T extends IHook<?>> T hook(String plugin, Class<? extends IHook<T>> defaultImpl, Class<? extends IHook<T>> workingImpl) {
        final IHook<T> hook;

        if (isEnabled(plugin))
            hook = singletonOf(workingImpl);
        else
            hook = singletonOf(defaultImpl);

        Utils.tryRegisterListener(hook);
        return (T) hook;
    }

    public static boolean isEnabled(String plugin) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(plugin);
    }

}

