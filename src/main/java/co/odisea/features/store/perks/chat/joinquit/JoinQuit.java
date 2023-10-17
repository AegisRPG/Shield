package co.odisea.features.store.perks.chat.joinquit;

import co.odisea.api.common.utils.RandomUtils;
import co.odisea.api.mongodb.models.nickname.Nickname;
import co.odisea.features.chat.bridge.RoleManager;
import co.odisea.features.vanish.Vanish;
import co.odisea.framework.commands.models.cooldown.CooldownService;
import co.odisea.framework.features.Feature;
import co.odisea.models.mutemenu.MuteMenuUser;
import co.odisea.features.commands.MuteMenuCommand.MuteMenuProvider.MuteMenuItem;
import co.odisea.models.nerd.Rank;
import co.odisea.utils.PlayerUtils.OnlinePlayers;
import co.odisea.features.chat.Chat.Broadcast;
import co.odisea.utils.AdventureUtils;
import co.odisea.utils.SoundUtils.Jingle;
import co.odisea.utils.IOUtils;
import co.odisea.utils.StringUtils;
import co.odisea.utils.Tasks;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class JoinQuit extends Feature implements Listener {
    @Getter
    private static List<String> joinMessages = new ArrayList<>();
    @Getter
    private static List<String> quitMessages = new ArrayList<>();

    @Override
    public void onStart() {
        reloadConfig();
    }

    @SneakyThrows
    public static void reloadConfig() {
        YamlConfiguration config = IOUtils.getAegisConfig("jq.yml");
        if (config.isConfigurationSection("messages")) {
            joinMessages = config.getConfigurationSection("messages").getStringList("join");
            quitMessages = config.getConfigurationSection("messages").getStringList("quit");
        }
    }

    public static void join(Player player) {
        if (isDuplicate(player, "join"))
            return;

        if (Vanish.isVanished(player)) {
            Broadcast.staffIngame().message(formatJoin(player, "[player] has joined while vanished").replaceAll("&[25]", "&7")).hideFromConsole(true).send();
            return;
        }

        String message = "&a[player] &5has joined the server";
        if (player.hasPermission("jq.custom") && joinMessages.size() > 0)
            message = RandomUtils.randomElement(joinMessages);

        final String finalMessage = message;

        if (player.isOnline()) {
            final String ingame = formatJoin(player, finalMessage);
            final Component component = AdventureUtils.fromLegacyAmpersandText(ingame);

            for (Player receiver : OnlinePlayers.getAll()) {
                if (!MuteMenuUser.hasMuted(receiver, MuteMenuItem.JOIN_QUIT))
                    // TODO - 1.19.2 Chat Validation Kick
                    // receiver.sendMessage(player, component, MessageType.CHAT);
                    receiver.sendMessage(component, MessageType.CHAT);
            }

            if (!player.hasPlayedBefore())
                Jingle.FIRST_JOIN.playAll();
            else
                Jingle.JOIN.playAll();

            Tasks.async(() -> {
                DiscordUser user = new DiscordUserService().get(player);
                RoleManager.update(user);

                final String discord = discordize(finalMessage).replaceAll("\\[player]", "**" + Nickname.discordOf(player) + "**");
                Discord.send("<:blue_arrow_right:883811353641517126> " + discord, TextChannel.BRIDGE);
            });
        }
    }

    public static void quit(Player player) {
        quit(player, PlayerQuitEvent.QuitReason.DISCONNECTED);
    }

    public static void quit(Player player, PlayerQuitEvent.QuitReason reason) {
        // Denizen Discord: https://discord.com/channels/315163488085475337/315163488085475337/929117355223695360
        if (!player.isOnline())
            return;

        if (isDuplicate(player, "quit"))
            return;

        if (vanished.contains(player)) {
            Broadcast.staffIngame().message(formatQuit(player, "[player] has left while vanished").replaceAll("&[45]", "&7")).hideFromConsole(true).send();
            return;
        }

        String message = "&c[player] &5has left the server";
        if (player.hasPermission("jq.custom") && quitMessages.size() > 0)
            message = RandomUtils.randomElement(quitMessages);

        final String reasonString;
        if (player.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.DECLINED && !new LocalResourcePackUserService().get(player).isEnabled()) {
            reason = PlayerQuitEvent.QuitReason.KICKED;
            reasonString = "Resource Pack Declined";
        } else
            reasonString = StringUtils.camelCase(reason.name());

        final String finalMessage = message;
        final String ingame = formatQuit(player, finalMessage);
        final Component component = AdventureUtils.fromLegacyAmpersandText(ingame);
        final Component staffComponent = AdventureUtils.fromLegacyAmpersandText(ingame + " (" + reasonString + ")");

        for (Player receiver : OnlinePlayers.getAll()) {
            if (MuteMenuUser.hasMuted(receiver, MuteMenuItem.JOIN_QUIT))
                continue;

            if (reason != PlayerQuitEvent.QuitReason.DISCONNECTED && Rank.of(receiver).isStaff())
                // TODO - 1.19.2 Chat Validation Kick
                // receiver.sendMessage(player, staffComponent, MessageType.CHAT);
                receiver.sendMessage(staffComponent, MessageType.CHAT);
            else
                // TODO - 1.19.2 Chat Validation Kick
                // receiver.sendMessage(player, component, MessageType.CHAT);
                receiver.sendMessage(component, MessageType.CHAT);
        }

        Jingle.QUIT.playAll();

        Tasks.async(() -> {
            DiscordUser user = new DiscordUserService().get(player);
            RoleManager.update(user);

            final String discord = discordize(finalMessage).replaceAll("\\[player]", "**" + Nickname.discordOf(player) + "**");
            Discord.send("<:red_arrow_left:331808021267218432> " + discord, TextChannel.BRIDGE);
        });
    }

    @NotNull
    public static String formatJoin(Player player, String finalMessage) {
        return "&2 &2&m &2&m &2&m &2>&5 " + finalMessage.replaceAll("\\[player]", "&a" + Nickname.of(player) + "&5");
    }

    @NotNull
    public static String formatQuit(Player player, String finalMessage) {
        return "&4 <&4&m &4&m &4&m &5 " + finalMessage.replaceAll("\\[player]", "&c" + Nickname.of(player) + "&5");
    }

    public static boolean isDuplicate(Player player, String type) {
        return !new CooldownService().check(player, type, 2);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            Koda.replyIngame("&lWelcome to Aegis, " + Nickname.of(player) + "!");
            Koda.replyDiscord("**Welcome to Aegis, " + Nickname.discordOf(player) + "!**");
        }

        join(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        Player player = event.getPlayer();
        quit(player, event.getReason());
    }

    // Can't use Utils#isVanished on player in quit event
    private static final Set<Player> vanished = new HashSet<>();

    static {
        Tasks.repeat(2, 2, JoinQuit::updateVanished);
    }

    public static void updateVanished() {
        OnlinePlayers.getAll().forEach(player -> {
            if (Vanish.isVanished(player))
                vanished.add(player);
            else
                vanished.remove(player);
        });
    }

}
