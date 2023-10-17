package co.odisea.hooks.viaversion;

import co.odisea.hooks.IHook;
import org.bukkit.entity.Player;

public class ViaVersionHook extends IHook<ViaVersionHook> {

    public String getPlayerVersion(Player player) {
        return "Unknown (ViaVersion not loaded)";
    }

}
