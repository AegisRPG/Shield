package co.odisea.framework.exceptions.preconfigured;

import co.odisea.api.common.exceptions.AegisException;
import net.md_5.bungee.api.ChatColor;

public class PreConfiguredException extends AegisException {

	public PreConfiguredException(String message) {
		super(ChatColor.RED + message);
	}
}
