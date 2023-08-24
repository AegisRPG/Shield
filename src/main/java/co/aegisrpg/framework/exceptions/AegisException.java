package co.aegisrpg.framework.exceptions;

import co.aegisrpg.api.common.exceptions.ShieldException;
import co.aegisrpg.utils.JsonBuilder;
import net.kyori.adventure.text.ComponentLike;

public class AegisException extends ShieldException {
    private JsonBuilder json;

    public AegisException(JsonBuilder json) {
        super(json.toString());
        this.json = json;
    }

    public AegisException(ComponentLike component) {
        this(new JsonBuilder(component));
    }

    public AegisException(String message) {
        this(new JsonBuilder(message));
    }

    public ComponentLike withPrefix(String prefix) {
        return new JsonBuilder(prefix).next(getJson());
    }
}
