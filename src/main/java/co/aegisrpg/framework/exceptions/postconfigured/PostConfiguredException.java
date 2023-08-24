package co.aegisrpg.framework.exceptions.postconfigured;

import co.aegisrpg.framework.exceptions.AegisException;
import co.aegisrpg.utils.JsonBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;

public class PostConfiguredException extends AegisException {

	public PostConfiguredException(JsonBuilder json) {
		super(new JsonBuilder(NamedTextColor.RED).next(json));
	}

	public PostConfiguredException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public PostConfiguredException(String message) {
		this(new JsonBuilder(message));
	}

}
