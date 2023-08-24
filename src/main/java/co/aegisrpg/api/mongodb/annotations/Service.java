package co.aegisrpg.api.mongodb.annotations;

import co.aegisrpg.api.interfaces.DatabaseObject;
import co.aegisrpg.api.mongodb.MongoService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    Class<? extends MongoService<? extends DatabaseObject>> value();

}
