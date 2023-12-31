package co.odisea.features.chat.commands;

import co.odisea.features.chat.Censor;
import co.odisea.features.chat.events.ChatEvent;
import co.odisea.features.chat.events.PublicChatEvent;
import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.Description;
import co.odisea.framework.commands.models.annotations.Path;
import co.odisea.framework.commands.models.annotations.Permission;
import co.odisea.framework.commands.models.annotations.Permission.Group;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.models.chat.ChatterService;
import co.odisea.models.chat.PublicChannel;
import lombok.NonNull;

import java.util.HashSet;

@Permission(Group.SENIOR_STAFF)
public class CensorCommand extends CustomCommand {

	public CensorCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path("test <channel> <message...>")
	@Description("Test the censor")
	void test(PublicChannel channel, String message) {
		ChatEvent event = new PublicChatEvent(new ChatterService().get(player()), channel, message, message, new HashSet<>());
		Censor.process(event);
		send(PREFIX + "Processed message:" + (event.isCancelled() ? " &c(Cancelled)" : ""));
		send("&eOriginal: &f" + event.getOriginalMeessage());
		send("&eResult: &f" + event.getMessage());
		send("&eChanged: " + (event.wasChanged() ? "&aYes" : "&cNo"));
	}

	@Path("reload")
	@Description("Reload the censor configuration from disk")
	void reload() {
		Censor.reloadConfig();
		send(PREFIX + Censor.getCensorItems().size() + " censor items loaded from disk");
	}

	@Path("debug")
	@Description("Print the censor configuration in chat")
	void debug() {
		send(Censor.getCensorItems().toString());
	}

}
