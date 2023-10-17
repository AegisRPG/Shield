package co.odisea.models.chat;

import co.odisea.api.mongodb.annotations.ObjectClass;
import co.odisea.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Chatter.class)
public class ChatterService extends MongoPlayerService<Chatter> {
    private final static Map<UUID, Chatter> cache = new ConcurrentHashMap<>();

    public Map<UUID, Chatter> getCache() {
        return cache;
    }

}
