package co.aegisrpg.features.store.perks.chat.joinquit;

import co.aegisrpg.framework.commands.models.*;
import co.aegisrpg.framework.commands.models.annotations.*;
import co.aegisrpg.framework.commands.models.annotations.Permission.Group;
import co.aegisrpg.framework.commands.models.events.*;
import co.aegisrpg.utils.*;
import lombok.*;

@Aliases("jq")
@Permission(Group.SENIOR_STAFF)
//@WikiConfig(rank = "Store", feature = "Chat")
public class JoinQuitCommand extends CustomCommand {

    public JoinQuitCommand(@NonNull CommandEvent event) {
        super(event);
        PREFIX = StringUtils.getPrefix("JQ");
    }

    @Path("reload")
    @Description("Reload join/quit messages from disk")
    void reload() {
        JoinQuit.reloadConfig();
        send(PREFIX + "Successfulyl loaded " + JoinQuit.getQuitMessages().size() + " join and " + JoinQuit.getQuitMessages().size() + " quit messages");
    }

}
