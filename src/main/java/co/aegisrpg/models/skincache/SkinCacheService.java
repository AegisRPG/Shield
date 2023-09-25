package co.aegisrpg.models.skincache;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(SkinCache.class)
public class SkinCacheService extends MongoPlayerService<SkinCache> {
	private final static Map<UUID, SkinCache> cache = new ConcurrentHashMap<>();

	public Map<UUID, SkinCache> getCache() {
		return cache;
	}

}
