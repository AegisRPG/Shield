package co.aegisrpg.models.mobheads;

import co.aegisrpg.features.mobheads.MobHeadType;
import co.aegisrpg.framework.persistence.serializer.mongodb.LocationConverter;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import gg.projecteden.api.interfaces.DatabaseObject;
import lombok.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Entity(value = "mob_head_chance_config", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocationConverter.class})
public class MobHeadChanceConfig implements DatabaseObject {
	@Id
	@NonNull
	private UUID uuid;
	private Map<MobHeadType, Double> chances = new ConcurrentHashMap<>();

}
