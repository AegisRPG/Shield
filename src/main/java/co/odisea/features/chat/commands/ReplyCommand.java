package co.odisea.features.chat.commands;

import co.odisea.features.chat.Chat;
import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.*;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.models.chat.Chatter;
import co.odisea.models.chat.ChatterService;
import lombok.NonNull;

import static co.odisea.api.common.utils.Nullables.isNullOrEmpty;

@Aliases("r")
public class ReplyCommand extends CustomCommand {
	private final Chatter chatter;

	public ReplyCommand(@NonNull CommandEvent event) {
		super(event);
		PREFIX = Chat.PREFIX;
		chatter = new ChatterService().get(player());
	}

	@Path("[message...]")
	@Description("Reply to your last private message")
	void reply(String message) {
		if (chatter.getLastPrivateMessage() == null)
			error("No one has messaged you");

		if (isNullOrEmpty(message))
			chatter.setActiveChannel(chatter.getLastPrivateMessage());
		else
			chatter.say(chatter.getLastPrivateMessage(), message);
	}

	@HideFromHelp
//	@HideFromWiki
	@TabCompleteIgnore
	@Override
	@Path("help")
	public void help() {
		reply(arg(1));
	}

	@HideFromHelp
//	@HideFromWiki
	@TabCompleteIgnore
	@Path("help [message...]")
	public void help(String message) {
		reply(arg(1) + " " + arg(2));
	}
}
