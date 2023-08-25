package co.aegisrpg.api.common.exceptions.postconfigured;

import co.aegisrpg.api.common.exceptions.AegisException;

public class PlayerNotFoundException extends AegisException {

    public PlayerNotFoundException(String input) {
        super("Player " + input + " not found");
    }

}
