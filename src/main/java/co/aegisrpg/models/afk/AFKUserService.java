package co.aegisrpg.models.afk;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

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
