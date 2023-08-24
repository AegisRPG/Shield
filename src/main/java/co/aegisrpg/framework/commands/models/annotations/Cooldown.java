package co.aegisrpg.framework.commands.models.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import co.aegisrpg.api.common.utils.TimeUtils.TickTime;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cooldown {
	TickTime value();
	double x() default 1;
	boolean global() default false;
	String bypass() default "";

}
