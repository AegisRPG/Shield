package co.odisea.features.chat.commands;

import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.Description;
import co.odisea.framework.commands.models.annotations.Path;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.models.chat.Chatter;
import co.odisea.models.chat.ChatterService;

public class ShrugCommand extends CustomCommand {
	private final Chatter chatter;

	public ShrugCommand(CommandEvent event) {
		super(event);
		chatter = new ChatterService().get(player());
	}

	@Path("[message...]")
	@Description("Insert a shrug emote at the end of your message")
	void run(String message) {
		if (message == null)
			message = "";

		chatter.say(message + " ¯\\_(ツ)_/¯");
	}

}
