package co.aegisrpg.models.afk;

import co.aegisrpg.Shield;
import co.aegisrpg.api.common.utils.TimeUtils;
import co.aegisrpg.features.afk.AFK;
import co.aegisrpg.features.warps.Warps;
import co.aegisrpg.framework.interfaces.PlayerOwnedObject;
import co.aegisrpg.models.warps.WarpType;
import co.aegisrpg.utils.PotionEffectBuilder;
import co.aegisrpg.utils.Tasks;
import co.aegisrpg.utils.WorldGuardUtils;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static co.aegisrpg.utils.StringUtils.stripColor;

@Data
@Entity(value = "afk_user", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters(UUIDConverter.class)
public class AFKUser implements PlayerOwnedObject {
    @Id
    @NonNull
    private UUID uuid;

    private boolean afk;
    private String message;
    private LocalDateTime time;
    private Location location;
    private boolean forceAfk;

    private Map<AFKSetting, Boolean> settings = new ConcurrentHashMap<>();

    private void save() {
        new AFKUserService().save(this);
    }

    private static WorldGuardUtils worldGuardUtils;

    public boolean isLimbo() {
        if (!isOnline())
            return false;

        if (worldGuardUtils == null)
            worldGuardUtils = new WorldGuardUtils("server");

        if (!worldGuardUtils.isInRegion(getOnlinePlayer().getLocation(), "limbo"))
            return false;

        return true;
    }

    public void limbo() {
        if (!isOnline() || isLimbo())
            return;

        Shield.log("[AFK] Sending " + getNickname() + " to limbo");

        final Player player = getOnlinePlayer();
        forceAfk = true;
        Tasks.sync(() -> {
            WarpType.STAFF.get("limbo").teleportAsync(player).thenRun(() -> {
                update();
                Nameplates.get().getPushService().edit(uuid, user -> user.setEnabled(false));
                player.addPotionEffect(new PotionEffectBuilder(PotionEffectType.INVISIBILITY).infinite().build());
                forceAfk = false;
                save();
            });

            sendMessage(AFK.PREFIX + "You have been sent to AFK limbo. Move around to exit");
        });
    }

    private CompletableFuture<Boolean> teleport;

    public void unlimbo() {
        if (!isLimbo() || teleport != null)
            return;

        Shield.log("[AFK] Returning " + getNickname() + " from limbo");

        final Player player = getOnlinePlayer();
        final BackService backService = new BackService();
        final Back back = backService.get(player);
        final Location location = back.getLocations().isEmpty() ? null : back.getLocations().remove(0);

        if (location == null) {
            Shield.severe("[AFK] Back location for " + getNickname() + " is null");
            teleport = Warps.survival(getOnlinePlayer());
        } else {
            teleport = player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            backService.save(back);
        }

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        teleport.thenRunAsync(() -> {
            afk = false;
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            notAfk();
            teleport = null;
        }, Tasks::async);
    }

    @Getter
    @AllArgsConstructor
    public enum AFKSetting {
        MOB_TARGETING(
                false,
                "Disable mobs targeting you while you are AFK",
                "Must be AFK for longer than 4 minutes",
                value -> "&3Mobs " + (value ? "&awill" : "&cwill not") + " &3target you while you are AFK"),
        MOB_SPAWNING(
                false,
                "Disable mobs spawning near you while you are AFK. Helps with server lag and spawn rates for active players",
                "Must be AFK for longer than 4 minutes",
                value -> "&3Mobs " + (value ? "&awill" : "&cwill not") + " &3spawn near you while you are AFK"),
        BROADCASTS(
                true,
                "Hides your AFK broadcasts from other players",
                null,
                value -> "&3Your own AFK broadcasts are now " + (value ? "&ashown" : "&chidden") + " &3from other players"),
        ;

        private final boolean defaultValue;
        private final String description;
        private final String descriptionExtra;
        private final Function<Boolean, String> message;
    }

    public boolean getSetting(AFKSetting setting) {
        return settings.getOrDefault(setting, setting.defaultValue);
    }

    public void setSetting(AFKSetting setting, boolean value) {
        if (value == setting.defaultValue)
            settings.remove(setting);
        else
            settings.put(setting, value);
    }

    public void setMessage(String message) {
        this.message = stripColor(message);
    }

    public void setTime() {
        this.time = LocalDateTime.now();
    }

    public boolean isNotAfk() {
        return !afk;
    }

    public boolean isTimeAfk() {
        return time != null && time.until(LocalDateTime.now(), ChronoUnit.SECONDS) > 240;
    }

    public boolean isNotTimeAfk() {
        return !isTimeAfk();
    }

    public boolean hasBeenAfkFor(long ticks) {
        return time != null && time.isBefore(LocalDateTime.now().minusSeconds(ticks / 20));
    }

    public void setLocation() {
        Player player = getPlayer();
        if (player != null)
            this.location = player.getLocation().clone();
    }

    public void forceAfk(Runnable action) {
        setForceAfk(true);
        action.run();
        Tasks.wait(TimeUtils.TickTime.SECOND.x(10), () -> {
            setLocation();
            setForceAfk(false);
        });
    }

    public void update() {
        setTime();
        setLocation();
    }

    public void afk() {
        setAfk(true);

        new NowAFKEvent(this).callEvent();

        message();
        broadcast();
    }

    public void notAfk() {
        if (isAfk())
            unlimbo();
        setAfk(false);
        setMessage(null);
        update();

        new NotAFKEvent(this).callEvent();

        message();
        broadcast();
    }

    public void message() {
        if (afk)
            sendMessage("&f" + presenceEmoji() + " &7You are now AFK" + (message == null ? "" : ". Your auto-reply message is set to:\n &e" + message));
        else
            sendMessage("&f" + presenceEmoji() + " &7You are no longer AFK");
    }

    private void broadcast() {
        if (!isOnline() || !getSetting(AFKSetting.BROADCASTS))
            return;

        Broadcast.ingame()
                .sender(this)
                .exclude(uuid)
                .checkCanSeeSender()
                .message("&f" + presenceEmoji() + " &e" + getNickname() + " &7is " + (afk ? "now" : "no longer") + " AFK")
                .muteMenuItem(MuteMenuItem.AFK)
                .hideFromConsole(true)
                .send();
    }

    public void reset() {
        afk = false;
        message = null;
        time = null;
        location = null;
        forceAfk = false;
    }

}
