package co.aegisrpg.api.common.exceptions;

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
