package co.aegisrpg.models.clientside;

import co.aegisrpg.api.mongodb.MongoPlayerService;
import co.aegisrpg.api.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(ClientSideConfig.class)
public class ClientSideConfigService extends MongoPlayerService<ClientSideConfig> {
    private final static Map<UUID, ClientSideConfig> cache = new ConcurrentHashMap<>();

    public Map<UUID, ClientSideConfig> getCache() {
        return cache;
    }
}
