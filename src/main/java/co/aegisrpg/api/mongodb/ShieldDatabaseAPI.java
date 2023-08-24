package co.aegisrpg.api.mongodb;

import co.aegisrpg.api.common.ShieldAPI;
import dev.morphia.converters.TypeConverter;

import java.util.Collection;
import java.util.Collections;

import static co.aegisrpg.api.common.utils.ReflectionUtils.subTypesOf;

public abstract class ShieldDatabaseAPI extends ShieldAPI {
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
