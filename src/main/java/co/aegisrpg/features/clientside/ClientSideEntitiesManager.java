package co.aegisrpg.features.clientside;

import co.aegisrpg.Shield;
import co.aegisrpg.api.common.utils.TimeUtils.TickTime;
import co.aegisrpg.features.listeners.events.PlayerChangingWorldEvent;
import co.aegisrpg.models.clientside.ClientSideConfig;
import co.aegisrpg.models.clientside.ClientSideUser;
import co.aegisrpg.models.clientside.ClientSideUserService;
import co.aegisrpg.utils.PlayerUtils.OnlinePlayers;
import co.aegisrpg.utils.Tasks;
import co.aegisrpg.utils.Timer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ClientSideEntitiesManager implements Listener {
    private static final ClientSideUserService userService = new ClientSideUserService();
    protected static boolean debug;

    public ClientSideEntitiesManager() {
        Shield.registerListener(this);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        userService.get(event.getPlayer()).hideAll();

        Tasks.wait(5, () -> userService.get(event.getPlayer()).showAll());
    }

    @EventHandler
    public void on(PlayerChangedWorldEvent event) {
        userService.get(event.getPlayer()).showAll();
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        userService.get(event.getPlayer()).hideAll();
    }

    @EventHandler
    public void on(PlayerChangingWorldEvent event) {
        userService.get(event.getPlayer()).hideAll();
    }

    static {
        Tasks.selfRepeatingAsync(TickTime.SECOND, () -> {
            final String id = "ClientSideEntities Radius Task";
            new Timer(id, debug, () -> {
                ClientSideConfig.getEntities().forEach((world, entities) -> {
                    if (entities.isEmpty())
                        return;

                    OnlinePlayers.where().world(world).forEach(player -> {
                        new Timer(id + " - " + world.getName() + " - " + player.getName(), debug, () -> {
                            final var user = ClientSideUser.of(player);
                            if (!user.hasMoved())
                                return;

                            user.updateVisibilityBox();

                            for (var entity : entities)
                                user.updateVisibility(entity);
                        });
                    });
                });
            });
        });

        Tasks.selfRepeating(TickTime.SECOND.x(3), () -> {
            final String id = "ClientSideEntities Radius Task 2";
            new Timer(id, debug, () -> {
                for (Player player : OnlinePlayers.where().world("survival").region("spawn").get()) {
                    final var user = ClientSideUser.of(player);
                    new Timer(id + " - " + player.getName(), debug, () -> {
                        for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                            if (entity instanceof Player || entity instanceof FallingBlock)
                                continue;

                            if (user.getVisibilityBox().contains(entity.getLocation().toVector()))
                                player.showEntity(Shield.getInstance(), entity);
                            else
                                player.hideEntity(Shield.getInstance(), entity);
                        }
                    });
                }
            });
        });
    }

}