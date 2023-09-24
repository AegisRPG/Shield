package co.aegisrpg.features.vanish.events;

import co.aegisrpg.models.vanish.VanishUser;
import co.aegisrpg.models.vanish.VanishUserService;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractVanishEvent extends PlayerEvent {

    public AbstractVanishEvent(@NotNull Player who) {
        super(who);
    }

    public VanishUser getUser() {
        return new VanishUserService().get(player);
    }
}
