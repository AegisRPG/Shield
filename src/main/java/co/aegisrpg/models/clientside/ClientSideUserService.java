package co.aegisrpg.models.clientside;

import co.aegisrpg.api.mongodb.MongoPlayerService;
import co.aegisrpg.api.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(ClientSideUser.class)
public class ClientSideUserService extends MongoPlayerService<ClientSideUser> {
    private final static Map<UUID, ClientSideUser> cache = new ConcurrentHashMap<>();

    public Map<UUID, ClientSideUser> getCache() {
        return cache;
    }

}