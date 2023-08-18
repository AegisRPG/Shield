package co.aegisrpg;

import co.aegisrpg.utils.api.ShieldAPI;
import dev.morphia.converters.TypeConverter;

import java.util.Collection;
import java.util.Collections;

public class ShieldDatabaseAPI extends ShieldAPI {
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
