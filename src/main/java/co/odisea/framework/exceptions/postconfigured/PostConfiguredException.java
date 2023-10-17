package co.odisea.framework.exceptions.postconfigured;

import co.odisea.framework.exceptions.ShieldException;
import co.odisea.utils.JsonBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;

public class PostConfiguredException extends ShieldException {

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
