package co.aegisrpg.features.afk;

import co.aegisrpg.api.common.utils.TimeUtils.Timespan;
import co.aegisrpg.framework.commands.models.CustomCommand;
import co.aegisrpg.framework.commands.models.annotations.Aliases;
import co.aegisrpg.framework.commands.models.annotations.Arg;
import co.aegisrpg.framework.commands.models.annotations.Description;
import co.aegisrpg.framework.commands.models.annotations.Path;
import co.aegisrpg.framework.commands.models.events.CommandEvent;
import org.bukkit.entity.Player;

import static co.aegisrpg.utils.PlayerUtils.send;

@Aliases("timeafk")
public class AFKTimeCommand extends CustomCommand {

    public AFKTimeCommand(CommandEvent event) {
        super(event);
    }

    @Path("[player]")
    @Description("View how long a player has been AFK")
    void timeAfk(@Arg("self") Player player) {
        String timespan = Timespan.of(AFK.get(player).getTime()).format();
        send(PREFIX + "&3" + nickname(player) + " has been AFK for &e" + timespan);
    }

}
