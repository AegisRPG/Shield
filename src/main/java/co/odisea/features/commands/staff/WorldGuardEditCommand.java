package co.odisea.features.commands.staff;

import co.odisea.framework.commands.models.annotations.*;
import co.odisea.framework.commands.models.annotations.Permission.Group;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.utils.WorldGuardFlagUtils;
import co.odisea.utils.LuckPermsUtils.PermissionChange;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.dv8tion.jda.annotations.ReplaceWith;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;

import java.util.List;
import java.util.stream.Collectors;

import static jdk.internal.org.jline.utils.Log.error;

@Aliases("wgedit")
@NoArgsConstructor
@Permission(Group.STAFF)
public class WorldGuardEditCommand extends CustomCommand implements Listener {
    @Deprecated
    @ReplaceWith("canWorldGuardEdit")
    private static final String PERMISSION = "worldguard.region.bypass.*";

    public WorldGuardEditCommand(@NonNull CommandEvent event) {
        super(event);
    }

    @Path("[enable]")
    @Description("Toggle WorldGuard edit bypass")
    void toggle(Boolean enable) {
        if (worldGroup() == WorldGroup.LEGACY && !isAdmin())
            error("You cannot enable WorldGuard editing here");

        if (enable == null) enable = !player().hasPermission(PERMISSION);

        if (enable) {
            on(player());
            send("&eWorldGuard editing &aenabled");
        } else {
            off(player());
            send("&eWorldGuard editing &cdisabled");
        }
    }

    @Path("flags registry [enable]")
    @Description("Set the WorldGuard flag registry state")
    void flags_registry(Boolean enable) {
        if (enable == null)
            enable = !WorldGuardFlagUtils.registry.isInitialized();

        WorldGuardFlagUtils.registry.setInitialized(enable);
        send(PREFIX + "Flag registry " + (enable ? "&aenabled" : "&cdisabled"));
    }

    public static void on(Player player) {
        PermissionChange.set().player(player).permissions(PERMISSION).runAsync();
    }

    public static void off(Player player) {
        PermissionChange.unset().player(player).permissions(PERMISSION).runAsync();
    }

    public static boolean canWorldGuardEdit(Permissible permissible) {
        return permissible.hasPermission(PERMISSION);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (canWorldGuardEdit(event.getPlayer()))
            off(event.getPlayer());
    }

    @EventHandler
    public void on(WorldGroupChangedEvent event) {
        if (event.getNewWorldGroup() == WorldGroup.LEGACY)
            off(event.getPlayer());
    }

    @ConverterFor(Flag.class)
    Flag<?> convertToWorldGuardFlag(String value) {
        return WorldGuard.getInstance().getFlagRegistry().get(value);
    }

    @TabCompleterFor(Flag.class)
    List<String> tabCompleteWorldGuardFlag(String filter) {
        return WorldGuard.getInstance().getFlagRegistry().getAll().stream()
                .map(Flag::getName)
                .filter(name -> name.toLowerCase().startsWith(filter.toLowerCase()))
                .collect(Collectors.toList());
    }

}
