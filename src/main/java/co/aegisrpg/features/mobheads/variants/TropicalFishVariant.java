package co.aegisrpg.features.mobheads.variants;

import co.aegisrpg.features.mobheads.common.MobHeadVariant;
import co.aegisrpg.utils.RandomUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static co.aegisrpg.api.common.utils.StringUtils.camelCase;

@Getter
@RequiredArgsConstructor
public enum TropicalFishVariant implements MobHeadVariant {
	ONE("33743"),
	TWO("33742"),
	THREE("33741"),
	FOUR("33740"),
	FIVE("30881"),
	SIX("33738"),
	SEVEN("33737"),
	EIGHT("33735"),
	NINE("33736"),
	TEN("30233"),
	ELEVEN("33734"),
	TWELVE("33733"),
	THIRTEEN("30880"),
	FOURTEEN("33732"),
	;

	private final String headId;

	@Override
	public @NotNull EntityType getEntityType() {
		return EntityType.TROPICAL_FISH;
	}

	@Override
	public String getDisplayName() {
		return "&e" + camelCase(getEntityType()) + " Head";
	}

	public static TropicalFishVariant random() {
		return RandomUtils.randomElement(List.of(values()));
	}
}
