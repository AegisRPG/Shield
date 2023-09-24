package co.aegisrpg.framework.exceptions;

import co.aegisrpg.api.common.exceptions.AegisException;
import co.aegisrpg.utils.JsonBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.ComponentLike;

@Data
@NoArgsConstructor
public class ShieldException extends AegisException {
    private JsonBuilder json;

    public ShieldException(JsonBuilder json) {
        super(json.toString());
        this.json = json;
    }

    public ShieldException(ComponentLike component) {
        this(new JsonBuilder(component));
    }

    public ShieldException(String message) {
        this(new JsonBuilder(message));
    }

    public ComponentLike withPrefix(String prefix) {
        return new JsonBuilder(prefix).next(getJson());
    }

}
