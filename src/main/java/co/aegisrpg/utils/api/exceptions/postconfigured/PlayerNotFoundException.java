package co.aegisrpg.utils.api.exceptions.postconfigured;

import co.aegisrpg.utils.api.exceptions.ShieldException;

public class PlayerNotFoundException extends ShieldException {

    public PlayerNotFoundException(String input) {
        super("Player " + input + " not found");
    }

}
