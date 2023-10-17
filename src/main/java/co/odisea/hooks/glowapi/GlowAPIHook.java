package co.odisea.hooks.glowapi;

import co.odisea.hooks.IHook;
import co.odisea.utils.GlowUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class GlowAPIHook extends IHook<GlowAPIHook> {

    public void setGlowing(Collection<? extends Entity> entities, GlowUtils.GlowColor color, Collection<? extends Player> recievers) {}

}
