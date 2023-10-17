package co.odisea.framework.exceptions.postconfigured;

public class PlayerNotFoundException extends co.odisea.api.common.exceptions.postconfigured.PlayerNotFoundException {

	public PlayerNotFoundException(String input) {
		super("&e" + input + "&c");
	}

}
