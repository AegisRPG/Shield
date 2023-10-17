package co.odisea.models.emoji;

import co.odisea.api.mongodb.annotations.ObjectClass;
import co.odisea.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(EmojiUser.class)
public class EmojiUserService extends MongoPlayerService<EmojiUser> {
	private final static Map<UUID, EmojiUser> cache = new ConcurrentHashMap<>();

	public Map<UUID, EmojiUser> getCache() {
		return cache;
	}

}
