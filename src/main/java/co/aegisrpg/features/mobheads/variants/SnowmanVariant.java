package co.aegisrpg.features.mobheads.variants;

import co.aegisrpg.features.mobheads.common.MobHeadVariant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowman;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public enum SnowmanVariant implements MobHeadVariant {
	NONE("30000"),
	DERP("4378"),
	;

	private final String headId;

	@Override
	public @NotNull EntityType getEntityType() {
		return EntityType.SNOWMAN;
	}

	public static SnowmanVariant of(Snowman snowman) {
		return snowman.isDerp() ? DERP : NONE;
	}
}
