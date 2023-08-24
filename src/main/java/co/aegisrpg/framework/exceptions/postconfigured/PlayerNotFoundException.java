package co.aegisrpg.framework.exceptions.postconfigured;

public class PlayerNotFoundException extends co.aegisrpg.api.common.exceptions.postconfigured.PlayerNotFoundException {

	public PlayerNotFoundException(String input) {
		super("&e" + input + "&c");
	}

}
