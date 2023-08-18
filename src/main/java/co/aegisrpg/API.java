package co.aegisrpg;

import co.aegisrpg.utils.api.utils.Env;
import com.google.gson.GsonBuilder;
import dev.morphia.converters.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

public class API extends ShieldDatabaseAPI {

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
