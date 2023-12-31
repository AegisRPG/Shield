package co.odisea.api.mongodb;

import co.odisea.api.common.AegisAPI;
import dev.morphia.converters.TypeConverter;

import java.util.Collection;
import java.util.Collections;

import static co.odisea.api.common.utils.ReflectionUtils.subTypesOf;

public abstract class AegisDatabaseAPI extends AegisAPI {
    @Override
    public void shutdown() {
        MongoConnector.shutdown();
    }

    abstract public DatabaseConfig getDatabaseConfig();

    public Collection<? extends Class<? extends TypeConverter>> getDefaultMongoConverters() {
        return subTypesOf(TypeConverter.class, MongoConnector.class.getPackageName() + ".serializers");
    }

    public Collection<? extends Class<? extends TypeConverter>> getMongoConverters() {
        return Collections.emptyList();
    }

}
