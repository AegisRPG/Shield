package co.odisea.features.mobheads;

import co.odisea.features.mobheads.common.MobHead;
import co.odisea.features.mobheads.common.MobHeadVariant;
import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.*;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.framework.commands.models.annotations.Permission.Group;
import co.odisea.framework.persistence.serializer.mongodb.MobHeadConverter;
import co.odisea.models.mobheads.MobHeadChanceConfigService;
import co.odisea.models.mobheads.MobHeadUser;
import co.odisea.models.mobheads.MobHeadUser.MobHeadData;
import co.odisea.models.mobheads.MobHeadUserService;
import co.odisea.utils.JsonBuilder;
import co.odisea.utils.StringUtils;
import co.odisea.utils.Utils;
import kotlin.Pair;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@NoArgsConstructor
@Aliases("mobhead")
public class MobHeadsCommand extends CustomCommand implements Listener {
	private final MobHeadUserService service = new MobHeadUserService();

	public MobHeadsCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path
	@Description("Open the mob head menu")
	void menu() {
		new MobHeadUserMenu().open(player());
	}

	@Permission(Group.ADMIN)
	@Path("get <type>")
	@Description("Spawn a mob head")
	void mobHead(MobHead mobHead) {
		giveItem(mobHead.getNamedSkull());
	}

	@Path("debug [enable]")
	@Permission(Group.ADMIN)
	@Description("Toggle debug mode")
	void debug(Boolean enable) {
		if (enable == null)
			enable = !MobHeads.isDebug();

		MobHeads.setDebug(enable);
		send(PREFIX + "Debug " + (enable ? "&aenabled" : "&cdisabled"));
	}

	@Path("validate types")
	@Permission(Group.ADMIN)
	@Description("Validate whether all entity types have configured mob heads")
	void validateTypes() {
		final List<EntityType> missingTypes = MobHeadType.getMissingTypes();

		if (missingTypes.isEmpty()) {
			send(PREFIX + "All entity types have defined mob heads");
			return;
		}

		send(PREFIX + "Missing entity types:");
		for (EntityType entityType : missingTypes)
			send(" &e" + camelCase(entityType));
	}

	@Path("chances validate")
	@Permission(Group.ADMIN)
	@Description("Validate whether all entity types have a configured drop chance")
	void chances_validate() {
		List<MobHeadType> zeroChance = new ArrayList<>();
		for (MobHeadType type : MobHeadType.values())
			if (type.getChance() == 0)
				zeroChance.add(type);

		if (zeroChance.isEmpty()) {
			send(PREFIX + "All mobs have a defined chance greater than 0");
			return;
		}

		send(PREFIX + "Mobs with 0% chance to drop head:");
		for (MobHeadType type : zeroChance)
			send(" &e" + camelCase(type));
	}

	@Permission(Group.ADMIN)
	@Path("chances get <type>")
	@Description("View a mob head's drop chance")
	void chances_get(MobHeadType type) {
		send(PREFIX + "Chance of drop for &e" + camelCase(type) + "&3: &e" + StringUtils.getDf().format(type.getChance()));
	}

	@Permission(Group.ADMIN)
	@Path("chances set <type> <chance>")
	@Description("Set a mob head's drop chance")
	void chances_set(MobHeadType type, double chance) {
		new MobHeadChanceConfigService().edit0(config -> config.getChances().put(type, chance));
		send(PREFIX + "Set chance of drop for &e" + camelCase(type) + " &3to &e" + StringUtils.getDf().format(chance));
	}

	@Path("top kills [page]")
	@Description("View the mob kill leaderboard")
	void topKills(@Arg("1") int page) {
		var top = getTop(MobHeadData::getKills);
		paginate(Utils.sortByValueReverse(top).keySet(), getTopFormatter(top), "mobheads top kills", page);
	}

	@Path("top heads [page]")
	@Description("View the mob head leaderboard")
	void topHeads(@Arg("1") int page) {
		var top = getTop(MobHeadData::getHeads);
		paginate(Utils.sortByValueReverse(top).keySet(), getTopFormatter(top), "mobheads top heads", page);
	}

	@NotNull
	private Map<Pair<MobHeadUser, MobHead>, Integer> getTop(Function<MobHeadData, Integer> getter) {
		return new HashMap<>() {{
			for (MobHeadUser user : service.getAll())
				for (MobHeadData data : user.getData())
					if (getter.apply(data) > 0)
						put(new Pair<>(user, data.getMobHead()), getter.apply(data));
		}};
	}

	@NotNull
	private BiFunction<Pair<MobHeadUser, MobHead>, String, JsonBuilder> getTopFormatter(Map<Pair<MobHeadUser, MobHead>, Integer> top) {
		return (pair, index) -> json(index + " " + pair.getFirst().getNerd().getColoredName()
			+ " &7- &e" + pair.getSecond().getDisplayName() + " &7- " + top.get(pair));
	}

	@ConverterFor(MobHead.class)
	MobHead convertToMobHead(String value) {
		return MobHeadConverter.decode(value);
	}

	@TabCompleterFor(MobHead.class)
	List<String> tabCompleteMobHead(String filter) {
		return new ArrayList<>() {{
			for (MobHeadType mobHeadType : MobHeadType.values())
				if (mobHeadType.hasVariants()) {
					for (MobHeadVariant variant : mobHeadType.getVariants())
						add((mobHeadType.name() + "." + variant.name()).toLowerCase());
				} else
					add(mobHeadType.name().toLowerCase());
		}};
	}

}
