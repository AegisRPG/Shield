package co.aegisrpg.models.emote;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(EmoteUser.class)
public class EmoteService extends MongoPlayerService<EmoteUser> {
    private final static Map<UUID, EmoteUser> cache = new ConcurrentHashMap<>();

    public Map<UUID, EmoteUser> getCache() {
        return cache;
    }

    @Override
    protected boolean deleteIf(EmoteUser user) {
        return user.getDisabled().isEmpty();
    }

}