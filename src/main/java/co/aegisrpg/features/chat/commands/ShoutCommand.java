package co.aegisrpg.features.chat.commands;

import co.aegisrpg.features.chat.ChatManager;
import co.aegisrpg.framework.commands.models.CustomCommand;
import co.aegisrpg.framework.commands.models.annotations.Path;
import co.aegisrpg.framework.commands.models.events.CommandEvent;
import co.aegisrpg.models.chat.Chatter;
import co.aegisrpg.models.chat.ChatterService;

//@HideFromWiki
public class ShoutCommand extends CustomCommand {
	private final Chatter chatter;

	public ShoutCommand(CommandEvent event) {
		super(event);
		chatter = new ChatterService().get(player());
	}

	@Path("<message...>")
	void run(String message) {
		chatter.say(ChatManager.getMainChannel(), message);
	}
}
