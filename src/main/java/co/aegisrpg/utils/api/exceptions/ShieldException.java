package co.aegisrpg.utils.api.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ShieldException extends RuntimeException {

    public ShieldException(String message) {
        super(message);
    }

    public ShieldException(String message, Throwable cause) {
        super(message, cause);
    }

}
