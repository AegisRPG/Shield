package co.odisea.features.afk;

import co.odisea.api.common.annotations.Async;
import co.odisea.api.common.utils.TimeUtils.TickTime;
import co.odisea.api.mongodb.models.nickname.Nickname;
import co.odisea.features.chat.events.MinecraftChatEvent;
import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.Aliases;
import co.odisea.framework.commands.models.annotations.Cooldown;
import co.odisea.framework.commands.models.annotations.Description;
import co.odisea.framework.commands.models.annotations.Path;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.models.afk.AFKUser;
import co.odisea.models.afk.AFKUserService;
import co.odisea.models.afk.AFKUser.AFKSetting;
import co.odisea.models.chat.Chatter;
import co.odisea.models.chat.PrivateChannel;
import co.odisea.utils.JsonBuilder;
import co.odisea.utils.PlayerUtils;
import co.odisea.utils.Tasks;
import lombok.NoArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;

import static co.odisea.api.common.utils.Nullables.isNullOrEmpty;

@Aliases("away")
@NoArgsConstructor
public class AFKCommand extends CustomCommand implements Listener {
    private static final AFKUserService service = new AFKUserService();

    public AFKCommand(CommandEvent event) {
        super(event);
    }

    @Async
    @Path("[autoreply...]")
    @Cooldown(value = TickTime.SECOND, x = 5)
    @Description("Toggle AFK mode")
    void afk(String autoreply) {
        AFKUser user = AFK.get(player());

        if (!isNullOrEmpty(autoreply))
            user.setMessage(autoreply);

        if (user.isAfk())
            if (isNullOrEmpty(autoreply))
                user.notAfk();
            else
                user.forceAfk(user::message);
        else
            user.forceAfk(user::afk);
    }

    @Path("settings")
    @Description("View available AFK settings")
    void settings() {
        send(PREFIX + "Available settings:");
        final AFKUser user = AFK.get(player());
        for (AFKSetting setting : AFKSetting.values()) {
            final boolean value = user.getSetting(setting);
            final JsonBuilder json = json((value ? "&a" : "&c") + " " + camelCase(setting))
                    .hover(value ? "&a&lEnabled" : "&c&lDisabled", setting.getMessage().apply(value))
                    .next(" &7- " + setting.getDescription());

            final String extra = setting.getDescriptionExtra();
            if (!isNullOrEmpty(extra))
                json.hover("", "&7" + extra);
            send(json.suggest("/afk settings " + setting.name().toLowerCase() + " "));
        }
    }

    @Path("settings <setting> [value]")
    @Description("Modify an AFK setting")
    void settings(AFKSetting setting, Boolean value) {
        final AFKUser user = AFK.get(player());
        if (value == null)
            value = !user.getSetting(setting);

        user.setSetting(setting, value);
        service.save(user);
        send(PREFIX + setting.getMessage().apply(value));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(MinecraftChatEvent event) {
        final AFKUser user = AFK.get(event.getChatter().getOnlinePlayer());
        if (user.isAfk())
            user.notAfk();
        else
            user.update();

        if (event.getChannel() instanceof PrivateChannel) {
            for (Chatter recipient : event.getRecipients()) {
                if (!recipient.isOnline()) continue;
                if (!PlayerUtils.canSee(user.getPlayer(), recipient.getOnlinePlayer())) return;
                AFKUser to = AFK.get(recipient.getOnlinePlayer());
                if (to.isAfk()) {
                    Tasks.wait(3, () -> {
                        if (!(event.getChatter().getOnlinePlayer().isOnline() && to.getOnlinePlayer().isOnline())) return;

                        String message = "&e* " + Nickname.of(to.getOnlinePlayer()) + " is AFK";
                        if (to.getMessage() != null)
                            message += ": &3" + to.getMessage();
                        send(event.getChatter(), message);
                    });
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Tasks.waitAsync(3, () -> {
            if (!event.getPlayer().isOnline()) return;

            AFKUser user = AFK.get(event.getPlayer());
            if (user.isAfk() && !user.isForceAfk())
                user.notAfk();
            else
                user.update();
        });
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        service.edit(event.getUniqueId(), AFKUser::reset);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        AFK.get(event.getPlayer()).unlimbo();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.edit(event.getPlayer(), AFKUser::reset);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (!"server".equals(player.getWorld().getName()))
            return;

        final AFKUser user = AFK.get(player);
        if (user.isAfk())
            user.unlimbo();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTarget(final EntityTargetLivingEntityEvent event) {
        if (event.getEntity().getType() == EntityType.EXPERIENCE_ORB)
            return;

        if (event.getTarget() instanceof Player player) {
            if (AFK.get(player).isTimeAfk()) {
                AFKUser user = service.get(player);
                if (!user.getSetting(AFKUser.AFKSetting.MOB_TARGETING))
                    event.setCancelled(true);
            }
        }
    }

}
