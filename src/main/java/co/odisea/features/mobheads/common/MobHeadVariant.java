package co.odisea.features.mobheads.common;

import co.odisea.Shield;
import co.odisea.features.mobheads.MobHeadType;
import co.odisea.utils.ItemBuilder;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static co.odisea.utils.Nullables.isNullOrAir;
import static co.odisea.utils.StringUtils.camelCase;

public interface MobHeadVariant extends MobHead {

	String getHeadId();

	@Override
	default @NotNull MobHeadType getType() {
		return Objects.requireNonNull(MobHeadType.of(getEntityType()));
	}

	@Override
	default @NotNull MobHeadVariant getVariant() {
		return this;
	}

	default @NotNull ItemStack getItemStack() {
		return Shield.getHeadAPI().getItemHead(getHeadId());
	}

	default @NonNull ItemStack getNamedItemStack() {
		return new ItemBuilder(getItemStack()).name("&e" + getDisplayName() + " Head").lore("&3Mob Head").build();
	}

	default @Nullable ItemStack getBaseSkull() {
		ItemStack skull = getItemStack();
		return isNullOrAir(skull) ? getType().getBaseSkull() : skull.clone();
	}

	default @Nullable ItemStack getNamedSkull() {
		ItemStack skull = getNamedItemStack();
		return isNullOrAir(skull) ? getType().getNamedSkull() : skull.clone();
	}

	@Override
	default String getDisplayName() {
		final String type = camelCase(getEntityType());
		final String variant = camelCase((Enum<?>) this);

		if (variant.equalsIgnoreCase("none"))
			return type;
		else
			return variant + " " + type;
	}

}
