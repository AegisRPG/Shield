package co.aegisrpg.hooks.viaversion;

import co.aegisrpg.hooks.IHook;
import org.bukkit.entity.Player;

public class ViaVersionHook extends IHook<ViaVersionHook> {

    public String getPlayerVersion(Player player) {
        return "Unknown (ViaVersion not loaded)";
    }

}
