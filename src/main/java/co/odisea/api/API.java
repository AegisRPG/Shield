package co.odisea.api;

import co.odisea.Shield;
import co.odisea.api.common.utils.Env;
import co.odisea.api.mongodb.AegisDatabaseAPI;
import co.odisea.api.mongodb.DatabaseConfig;
import co.odisea.framework.persistence.serializer.mongodb.ItemStackConverter;
import co.odisea.utils.SerializationUtils.Json.LocationGsonSerializer;
import co.odisea.utils.SerializationUtils.Json.LocalDateGsonSerializer;
import co.odisea.utils.SerializationUtils.Json.LocalDateTimeGsonSerializer;
import co.odisea.utils.Tasks;
import com.google.gson.GsonBuilder;
import dev.morphia.converters.TypeConverter;
import org.bukkit.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import static co.odisea.api.common.utils.ReflectionUtils.subTypesOf;

public class API extends AegisDatabaseAPI {

    public API() {
        instance = this;
    }

    @Override
    public String getAppName() {
        return Shield.class.getSimpleName();
    }

    @Override
    public Env getEnv() {
        return Shield.getEnv();
    }

    @Override
    public DatabaseConfig getDatabaseConfig() {
        return DatabaseConfig.builder()
                .password(Shield.getInstance().getConfig().getString("databases.mongodb.password"))
                .modelPath("co.aegisrpg.shield.models")
                .env(getEnv())
                .build();
    }

    @Override
    public ClassLoader getClassLoader() {
        return Shield.class.getClassLoader();
    }

    @Override
    public Collection<? extends Class<? extends TypeConverter>> getMongoConverters() {
        return subTypesOf(TypeConverter.class, ItemStackConverter.class.getPackageName());
    }

    @Override
    public GsonBuilder getPrettyPrinter() {
        return super.getPrettyPrinter()
                .registerTypeAdapter(Location.class, new LocationGsonSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateGsonSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeGsonSerializer());
    }

    @Override
    public void sync(Runnable runnable) {
        Tasks.sync(runnable);
    }

}