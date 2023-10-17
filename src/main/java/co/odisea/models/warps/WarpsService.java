package co.odisea.models.warps;

import co.odisea.api.mongodb.annotations.ObjectClass;
import co.odisea.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Warps.class)
public class WarpsService extends MongoPlayerService<Warps> {
    private final static Map<UUID, Warps> cache = new ConcurrentHashMap<>();

    public Map<UUID, Warps> getCache() {
        return cache;
    }

}

