package co.aegisrpg.features.clientside;

import co.aegisrpg.api.common.utils.TimeUtils.TickTime;
import co.aegisrpg.framework.features.Feature;
import co.aegisrpg.models.clientside.ClientSideConfig;
import co.aegisrpg.models.clientside.ClientSideUser;
import co.aegisrpg.models.clientside.ClientSideUserService;
import co.aegisrpg.utils.PlayerUtils.OnlinePlayers;
import co.aegisrpg.utils.Tasks;
import org.bukkit.entity.Player;

public class ClientSideFeature extends Feature {

    static {
        final var userService = new ClientSideUserService();
        Tasks.repeatAsync(TickTime.SECOND, TickTime.SECOND.x(10), userService::saveOnlineSync);
    }

    @Override
    public void onStart() {
        new ClientSideEntitiesManager();

        for (Player player : OnlinePlayers.getAll()) {
            ClientSideUser user = ClientSideUser.of(player);
            for (var entity : ClientSideConfig.getEntities(player.getWorld()))
                user.show(entity);
        }
    }

    @Override
    public void onStop() {
        ClientSideConfig.getEntities().forEach((world, entities) -> {
            for (Player player : OnlinePlayers.where().world(world).get())
                for (var entity : entities)
                    ClientSideUser.of(player).hide(entity);
        });

        new ClientSideUserService().saveOnlineSync();
    }

}
