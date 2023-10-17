package co.odisea.api.common.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AegisException extends RuntimeException {

    public AegisException(String message) {
        super(message);
    }

    public AegisException(String message, Throwable cause) {
        super(message, cause);
    }

}
