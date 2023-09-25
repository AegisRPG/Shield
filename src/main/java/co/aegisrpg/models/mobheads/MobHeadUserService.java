package co.aegisrpg.models.mobheads;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(MobHeadUser.class)
public class MobHeadUserService extends MongoPlayerService<MobHeadUser> {
	private final static Map<UUID, MobHeadUser> cache = new ConcurrentHashMap<>();

	public Map<UUID, MobHeadUser> getCache() {
		return cache;
	}

	@Override
	protected void beforeSave(MobHeadUser user) {
		user.getData().removeIf(data -> data.getKills() == 0 && data.getHeads() == 0);
	}

}
