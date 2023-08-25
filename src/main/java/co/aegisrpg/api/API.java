package co.aegisrpg.api;

import co.aegisrpg.Shield;
import co.aegisrpg.api.common.utils.Env;
import co.aegisrpg.api.mongodb.AegisDatabaseAPI;
import co.aegisrpg.api.mongodb.DatabaseConfig;
import co.aegisrpg.utils.Tasks;
import com.google.gson.GsonBuilder;
import dev.morphia.converters.TypeConverter;
import org.bukkit.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static co.aegisrpg.api.common.utils.ReflectionUtils.subTypesOf;

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