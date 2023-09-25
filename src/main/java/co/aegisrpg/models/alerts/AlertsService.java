package co.aegisrpg.models.alerts;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Alerts.class)
public class AlertsService extends MongoPlayerService<Alerts> {
    private final static Map<UUID, Alerts> cache = new ConcurrentHashMap<>();

    public Map<UUID, Alerts> getCache() {
        return cache;
    }

}
