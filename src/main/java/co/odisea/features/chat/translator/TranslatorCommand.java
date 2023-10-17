package co.odisea.features.chat.translator;

import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.Aliases;
import co.odisea.framework.commands.models.annotations.Description;
import co.odisea.framework.commands.models.annotations.Path;
import co.odisea.framework.commands.models.annotations.Permission;
import co.odisea.framework.commands.models.annotations.Permission.Group;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.framework.exceptions.postconfigured.InvalidInputException;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@Aliases("translate")
@Permission(Group.STAFF)
public class TranslatorCommand extends CustomCommand {
	public TranslatorCommand(CommandEvent event) {
		super(event);
	}

	@Path("stop [player]")
	@Description("Stop automatically translating messages from a player")
	void remove(Player player) {
		if (player != null) {
			ArrayList<UUID> translators = Translator.getMap().get(player.getUniqueId());
			if (translators != null && translators.contains(uuid())) {
				Translator.getMap().get(player.getUniqueId()).remove(uuid());
				send(PREFIX + "You are no longer translating " + player.getDisplayName());
			} else {
				send(PREFIX + "You are not translating that player");
			}
			return;
		}

		for (UUID uuid : Translator.getMap().keySet())
			Translator.getMap().get(uuid).remove(uuid());

		send(PREFIX + "Stopping all active translations");
	}

	@Path("<player>")
	@Description("Automatically translate messages from a player")
	void translate(Player player) {
		if (player() == player)
			throw new InvalidInputException("You cannot translate yourself");

		ArrayList<UUID> uuids = new ArrayList<>() {{
			add(uuid());
			if (Translator.getMap().containsKey(player.getUniqueId()))
				addAll(Translator.getMap().get(player.getUniqueId()));
		}};
		Translator.getMap().put(player.getUniqueId(), uuids);

		send(PREFIX + "You are now translating messages from " + player.getDisplayName());
	}

}
