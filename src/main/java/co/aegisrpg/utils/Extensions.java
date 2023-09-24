package co.aegisrpg.utils;

import co.aegisrpg.features.afk.AFK;
import co.aegisrpg.models.nerd.Rank;
import org.bukkit.entity.Player;

public class Extensions {

	// Players

	public static boolean isVanished(Player player) {
		return Vanish.isVanished(player);
	}

	public static boolean isAFK(Player player) {
		return AFK.get(player).isAfk();
	}

	public static Rank getRank(Player player) {
		return Rank.of(player);
	}

	public static boolean isStaff(Player player) {
		return Rank.of(player).isStaff();
	}

	public static boolean isSeniorStaff(Player player) {
		return Rank.of(player).isSeniorStaff();
	}

	// Strings

	public static boolean isNullOrEmpty(String string) {
		return Nullables.isNullOrEmpty(string);
	}

	public static String camelCase(String string) {
		return StringUtils.camelCase(string);
	}

	public static String camelCase(Enum<?> enumeration) {
		return StringUtils.camelCase(enumeration);
	}

}
