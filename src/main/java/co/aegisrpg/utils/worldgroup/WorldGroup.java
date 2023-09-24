package co.aegisrpg.utils.worldgroup;

import co.aegisrpg.framework.annotations.Icon;
import co.aegisrpg.models.warps.WarpType;
import co.aegisrpg.utils.LuckPermsUtils;
import co.aegisrpg.utils.StringUtils;
import co.aegisrpg.models.emoji.EmojiUser.Emoji;
import gg.projecteden.parchment.OptionalLocation;
import lombok.Getter;
import lombok.SneakyThrows;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.*;

public enum WorldGroup implements IWorldGroup {
    @Icon("globe")
    @Spawn(SpawnType.HUB)
    SERVER("server"),

    @Icon("diamond_pickaxe")
    @Spawn(SpawnType.SURVIVAL)
    SURVIVAL(List.of("safepvp", "events"), List.of(SubWorldGroup.SURVIVAL)),

    @Icon("wooden_axe")
    @Spawn(SpawnType.CREATIVE)
    CREATIVE("buildcontest"),

//    @Icon("compass")
//    ADVENTURE("map1", "map2"),

//    @Icon("star")
//    EVENTS("event1", "event2"),

    @Icon("lock")
    STAFF("buildworld"),

    @Icon("question")
    UNKNOWN;

    @Getter
    private final @NotNull List<String> worldNames = new ArrayList<>();

    WorldGroup() {
        this(new String[0]);
    }

    WorldGroup(String... worldNames) {
        this.worldNames.addAll(Arrays.asList(worldNames));
    }

    WorldGroup(SubWorldGroup... subWorldGroups) {
        for (SubWorldGroup subWorldGroup : subWorldGroups)
            this.worldNames.addAll(subWorldGroup.getWorldNames());
    }

    WorldGroup(List<String> worldNames, List<SubWorldGroup> subWorldGroups) {
        for (SubWorldGroup subWorldGroup : subWorldGroups)
            this.worldNames.addAll(subWorldGroup.getWorldNames());

        this.worldNames.addAll(worldNames);
    }

    @Override
    public String toString() {
        return StringUtils.camelCase(name());
    }

    public static WorldGroup of(@Nullable Entity entity) {
        return entity == null ? UNKNOWN : of(entity.getWorld());
    }

    public static WorldGroup of(@Nullable OptionalLocation location) {
        if (location == null)
            return UNKNOWN;
        Location loc = location.getLocation();
        return loc == null ? UNKNOWN : of(loc.getWorld());
    }

    public static WorldGroup of(@Nullable Location location) {
        return location == null ? UNKNOWN : of(location.getWorld());
    }

    public static WorldGroup of(@Nullable World world) {
        return world == null ? UNKNOWN : of(world.getName());
    }

    private static final Map<String, WorldGroup> CACHE = new HashMap<>();

    public static WorldGroup of(String world) {
        return CACHE.computeIfAbsent(world, $ -> rawOf(world));
    }

    private static WorldGroup rawOf(String world) {
        for (WorldGroup group : values())
            if (group.contains(world))
                return group;

        if (world.toLowerCase().startsWith("build"))
            return CREATIVE;

        return UNKNOWN;
    }

    @SneakyThrows
    public Field getField() {
        return getClass().getField(name());
    }

    public SpawnType getSpawnType() {
        final Spawn annotation = getField().getAnnotation(Spawn.class);
        return annotation == null ? null : annotation.value();
    }

    public String getIcon() {
        final Icon annotation = getField().getAnnotation(Icon.class);
        final Emoji emoji = Emoji.of(annotation.value());
        if (emoji != null)
            return emoji.getEmoji();
        return "❌";
    }

    public boolean isSurvivalMode() {
        return this == SURVIVAL;
    }

    static {
        LuckPermsUtils.registerContext(new WorldGroupCalculator());
    }

    public static class WorldGroupCalculator implements ContextCalculator<Player> {

        @Override
        public void calculate(@NotNull Player target, ContextConsumer contextConsumer) {
            contextConsumer.accept("worldgroup", WorldGroup.of(target).name());
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
            for (WorldGroup worldGroup : WorldGroup.values())
                builder.add("worldgroup", worldGroup.name().toLowerCase());
            return builder.build();
        }

    }

    public enum SpawnType {
        HUB,
        SURVIVAL,
        MINIGAMES,
        CREATIVE,
        ;

        public void teleport(Player player) {
            WarpType.NORMAL.get(this.name()).teleportAsync(player);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Spawn {
        SpawnType value();
    }

}

