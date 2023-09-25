package co.aegisrpg.features.chat;

import co.aegisrpg.features.chat.events.ChatEvent;
import co.aegisrpg.features.chat.events.DiscordChatEvent;
import co.aegisrpg.features.chat.events.PublicChatEvent;
import co.aegisrpg.framework.commands.Commands;
import co.aegisrpg.models.chat.Chatter;
import co.aegisrpg.models.chat.ChatterService;
import co.aegisrpg.utils.AdventureUtils;
import co.aegisrpg.utils.Tasks;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.regex.Pattern;

import static co.aegisrpg.api.common.utils.Nullables.isNullOrEmpty;
import static co.aegisrpg.api.common.utils.StringUtils.right;
import static co.aegisrpg.utils.PlayerUtils.runCommand;

@NoArgsConstructor
public class ChatListener implements Listener {

    /*
	static {
		// TODO 1.19 Fixes chat kick
		final PacketType type = PacketType.fromClass(ClientboundPlayerChatHeaderPacket.class);
		Nexus.getProtocolManager().addPacketListener(new PacketAdapter(Nexus.getInstance(), type) {
			@Override
			public void onPacketSending(PacketEvent event) {
				event.setCancelled(true);
			}
		});
	}
	*/

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void on(AsyncChatEvent event) {
        Chatter chatter = new ChatterService().get(event.getPlayer());
        Tasks.sync(() -> {
            // Prevents "t/command"
            final String msg = AdventureUtils.asLegacyText(event.message());
            if (Pattern.compile("^[tT]" + Commands.getPattern() + ".*").matcher(msg).matches())
                runCommand(event.getPlayer(), right(msg, msg.length() - 2));
            else
                chatter.say(msg);
        });
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(ChatEvent event) {
        Censor.process(event);
        // CommandHighlighter.process(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEmptyChat(ChatEvent event) {
        if (!isNullOrEmpty(event.getMessage()))
            return;
        if (event instanceof DiscordChatEvent discordChatEvent && discordChatEvent.hasAttachments())
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPublicChat(PublicChatEvent event) {
        Koda.process(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDiscordChat(DiscordChatEvent event) {
        Koda.process(event);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Chatter chatter = new ChatterService().get(event.getPlayer());
        chatter.updateChannels();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Chatter chatter = new ChatterService().get(event.getPlayer());
        if (chatter.getActiveChannel() == null)
            chatter.setActiveChannel(ChatManager.getMainChannel());
        chatter.updateChannels();
    }

}
