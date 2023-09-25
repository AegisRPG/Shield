package co.aegisrpg.models.mobheads;

import co.aegisrpg.features.mobheads.common.MobHead;
import co.aegisrpg.framework.interfaces.PlayerOwnedObject;
import co.aegisrpg.framework.persistence.serializer.mongodb.MobHeadConverter;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PostLoad;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
@Entity(value = "mob_head_user", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters(UUIDConverter.class)
public class MobHeadUser implements PlayerOwnedObject {
	@Id
	@NonNull
	private UUID uuid;
	private List<MobHeadData> data = new ArrayList<>();

	private final transient Map<MobHead, MobHeadData> map = new HashMap<>();

	@PostLoad
	void postLoad() {
		for (MobHeadData data : data)
			map.put(data.getMobHead(), data);
	}

	public @NotNull MobHeadData get(MobHead mobHead) {
		return map.computeIfAbsent(mobHead, $ -> {
			MobHeadData mobHeadData = new MobHeadData(mobHead);
			data.add(mobHeadData);
			return mobHeadData;
		});
	}

	@Data
	@NoArgsConstructor
	@RequiredArgsConstructor
	@Converters(MobHeadConverter.class)
	public static class MobHeadData {
		@NonNull
		private MobHead mobHead;
		private int kills;
		private int heads;

		public void kill() {
			++kills;
		}

		public void head() {
			++heads;
		}

	}

}
