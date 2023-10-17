package co.odisea.models.clientside;

import co.odisea.api.mongodb.MongoPlayerService;
import co.odisea.api.mongodb.annotations.ObjectClass;

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