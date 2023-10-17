package co.odisea.models.afk;

import co.odisea.api.mongodb.annotations.ObjectClass;
import co.odisea.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(AFKUser.class)
public class AFKUserService extends MongoPlayerService<AFKUser> {
    private final static Map<UUID, AFKUser> cache = new ConcurrentHashMap<>();

    public Map<UUID, AFKUser> getCache() {
        return cache;
    }

}
