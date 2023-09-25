package co.aegisrpg.features.chat.alerts;

import co.aegisrpg.framework.commands.models.CustomCommand;
import co.aegisrpg.framework.commands.models.annotations.Description;
import co.aegisrpg.framework.commands.models.annotations.Path;
import co.aegisrpg.framework.commands.models.annotations.Permission;
import co.aegisrpg.framework.commands.models.annotations.Permission.Group;
import co.aegisrpg.framework.commands.models.events.CommandEvent;
import co.aegisrpg.utils.SoundUtils.Jingle;
import org.bukkit.entity.Player;

@Permission(Group.STAFF)
public class AlertCommand extends CustomCommand {

    public AlertCommand(CommandEvent event) {
        super(event);
    }

    @Path("<player>")
    @Description("Play a ping sound to a player")
    void alert(Player player) {
        Jingle.PING.play(player);
    }

}
