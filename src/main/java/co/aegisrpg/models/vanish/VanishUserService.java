package co.aegisrpg.models.vanish;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(VanishUser.class)
public class VanishUserService extends MongoPlayerService<VanishUser> {
    private final static Map<UUID, VanishUser> cache = new ConcurrentHashMap<>();

    public Map<UUID, VanishUser> getCache() {
        return cache;
    }
}
