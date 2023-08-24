package co.aegisrpg.framework.exceptions.postconfigured;

import co.aegisrpg.framework.commands.models.cooldown.CooldownService;
import org.bukkit.OfflinePlayer;

import co.aegisrpg.api.common.utils.TimeUtils.Timespan;
import java.time.LocalDateTime;
import java.util.UUID;

public class CommandCooldownException extends PostConfiguredException {

	public CommandCooldownException(OfflinePlayer player, String type) {
		this(player.getUniqueId(), type);
	}

	public CommandCooldownException(UUID uuid, String type) {
		super("You can run this command again in &e" + new CooldownService().getDiff(uuid, type));
	}

	public CommandCooldownException(LocalDateTime when) {
		super("You can run this command again in &e" + Timespan.of(when).format());
	}

}
