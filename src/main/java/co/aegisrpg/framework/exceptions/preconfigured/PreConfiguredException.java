package co.aegisrpg.framework.exceptions.preconfigured;

import co.aegisrpg.api.common.exceptions.AegisException;
import net.md_5.bungee.api.ChatColor;

public class PreConfiguredException extends AegisException {

	public PreConfiguredException(String message) {
		super(ChatColor.RED + message);
	}
}
