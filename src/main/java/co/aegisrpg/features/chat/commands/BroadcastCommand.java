package co.aegisrpg.features.chat.commands;

import co.aegisrpg.framework.commands.models.CustomCommand;
import co.aegisrpg.framework.commands.models.annotations.Path;
import co.aegisrpg.framework.commands.models.annotations.Permission;
import co.aegisrpg.features.chat.Chat.Broadcast;
import co.aegisrpg.framework.commands.models.annotations.Description;
import co.aegisrpg.framework.commands.models.annotations.Permission.Group;
import co.aegisrpg.framework.commands.models.events.CommandEvent;
import lombok.NonNull;

@Permission(Group.ADMIN)
public class BroadcastCommand extends CustomCommand {

	public BroadcastCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path("<message...>")
	@Description("Broadcast a message to the server")
	void run(String message) {
		Broadcast.all().message(message).send();
	}

}
