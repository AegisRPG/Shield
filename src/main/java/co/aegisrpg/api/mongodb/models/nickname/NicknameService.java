package co.aegisrpg.api.mongodb.models.nickname;

import co.aegisrpg.api.mongodb.MongoPlayerService;
import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import dev.morphia.query.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ObjectClass(Nickname.class)
public class NicknameService extends MongoPlayerService<Nickname> {
    private final static Map<UUID, Nickname> cache = new HashMap<>();

    public Map<UUID, Nickname> getCache() {
        return cache;
    }

    public Nickname getFromNickname(String nickname) {
        Query<Nickname> query = database.createQuery(Nickname.class);
        query.and(query.criteria("nickname").equalIgnoreCase(nickname));
        Nickname data = query.find().tryNext();
        cache(data);
        return data;
    }

    public Nickname getFromQueueId(String queueId) {
        Nickname data = database.createQuery(Nickname.class).filter("nicknameHistory.nicknameQueueId", queueId).find().tryNext();
        cache(data);
        return data;
    }

}
