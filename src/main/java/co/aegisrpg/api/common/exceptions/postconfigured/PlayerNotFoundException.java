package co.aegisrpg.api.common.exceptions.postconfigured;

import co.aegisrpg.api.common.exceptions.ShieldException;

public class PlayerNotFoundException extends ShieldException {

    public PlayerNotFoundException(String input) {
        super("Player " + input + " not found");
    }

}
