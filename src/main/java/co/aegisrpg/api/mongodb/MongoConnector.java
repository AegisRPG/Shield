package co.aegisrpg.api.mongodb;

import co.aegisrpg.api.common.AegisAPI;
import co.aegisrpg.utils.Nullables;
import com.mongodb.*;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.annotations.Entity;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MapperOptions;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static co.aegisrpg.api.common.utils.ReflectionUtils.typesAnnotatedWith;

public class MongoConnector {
    protected static final Morphia morphia = new Morphia();
    @Getter
    private static Datastore datastore;

    public static Datastore connect() {
        if (datastore != null)
            return datastore;

        AegisAPI.getAs(AegisDatabaseAPI.class).ifPresent(api -> {
            // Properly merge deleted hashmaps and null vars
            MapperOptions.Builder options = MapperOptions.builder().storeEmpties(true).storeNulls(true);
            if (api.getClassLoader() != null)
                options.classLoader(api.getClassLoader());

            morphia.getMapper().setOptions(options.build());

            DatabaseConfig config = api.getDatabaseConfig();
            // Load classes into memory once
            if (!Nullables.isNullOrEmpty(config.getModelPath()))
                typesAnnotatedWith(Entity.class, config.getModelPath());

            MongoCredential root = MongoCredential.createScramSha1Credential(config.getUsername(), "admin", config.getPassword().toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(), root, MongoClientOptions.builder().build());
            String database = (config.getPrefix() == null ? "" : config.getPrefix() + "_") + "aegisrpg";
            datastore = morphia.createDatastore(mongoClient, database);
            datastore.ensureIndexes();

            List<Class<? extends TypeConverter>> classes = new ArrayList<>();
            classes.addAll(api.getDefaultMongoConverters());
            classes.addAll(api.getMongoConverters());

            for (Class<? extends TypeConverter> clazz : classes) {
                try {
                    Constructor<? extends TypeConverter> constructor = clazz.getDeclaredConstructor(Mapper.class);
                    TypeConverter instance = constructor.newInstance(morphia.getMapper());
                    morphia.getMapper().getConverters().addConverter(instance);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return datastore;
    }

    //TODO: uncomment to new connection method if old one fails

//    public static Datastore connect() {
//        if (datastore != null)
//            return datastore;
//
//        AegisAPI.getOptional().ifPresent(api -> {
//            // Properly merge deleted hashmaps and null vars
//            MapperOptions.Builder options = MapperOptions.builder().storeEmpties(true).storeNulls(true);
//            if (api.getClassLoader() != null)
//                options.classLoader(api.getClassLoader());
//
//            morphia.getMapper().setOptions(options.build());
//
//            DatabaseConfig config = api.getDatabaseConfig();
//            // Load classes into memory once
//            if (!Nullables.isNullOrEmpty(config.getModelPath()))
//                typesAnnotatedWith(Entity.class, config.getModelPath());
//
//            MongoClientURI uri = new MongoClientURI(config.getLink());
//            MongoClient mongoClient = new MongoClient(uri);
//
//            String database = (config.getPrefix() == null ? "" : config.getPrefix() + "_") + "aegisserver";
//            datastore = morphia.createDatastore(mongoClient, database);
//            datastore.ensureIndexes();
//
//            List<Class<? extends TypeConverter>> classes = new ArrayList<>();
//            classes.addAll(api.getDefaultMongoConverters());
//            classes.addAll(api.getMongoConverters());
//
//            for (Class<? extends TypeConverter> clazz : classes) {
//                try {
//                    Constructor<? extends TypeConverter> constructor = clazz.getDeclaredConstructor(Mapper.class);
//                    TypeConverter instance = constructor.newInstance(morphia.getMapper());
//                    morphia.getMapper().getConverters().addConverter(instance);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        return datastore;
//    }

    public static void shutdown() {
        try {
            if (datastore != null) {
                datastore.getMongo().close();
                datastore = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
