package co.aegisrpg.models.mobheads;

import co.aegisrpg.api.mongodb.MongoService;
import co.aegisrpg.api.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(MobHeadChanceConfig.class)
public class MobHeadChanceConfigService extends MongoService<MobHeadChanceConfig> {
	private final static Map<UUID, MobHeadChanceConfig> cache = new ConcurrentHashMap<>();

	public Map<UUID, MobHeadChanceConfig> getCache() {
		return cache;
	}

}
