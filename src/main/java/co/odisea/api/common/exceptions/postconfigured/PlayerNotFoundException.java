package co.odisea.api.common.exceptions.postconfigured;

import co.odisea.api.common.exceptions.AegisException;

public class PlayerNotFoundException extends AegisException {

    public PlayerNotFoundException(String input) {
        super("Player " + input + " not found");
    }

}
