package co.odisea.api.mongodb.annotations;

import co.odisea.api.interfaces.DatabaseObject;
import co.odisea.api.mongodb.MongoService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    Class<? extends MongoService<? extends DatabaseObject>> value();

}
