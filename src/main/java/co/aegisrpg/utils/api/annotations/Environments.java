package co.aegisrpg.utils.api.annotations;

import co.aegisrpg.utils.api.utils.Env;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Environments {
    Env[] value();
}
