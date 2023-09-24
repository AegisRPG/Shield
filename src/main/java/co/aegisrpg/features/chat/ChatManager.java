package co.aegisrpg.features.chat;

import co.aegisrpg.api.mongodb.models.nickname.Nickname;
import co.aegisrpg.features.chat.events.PrivateChatEvent;
import co.aegisrpg.features.chat.events.PublicChatEvent;
import co.aegisrpg.framework.commands.models.cooldown.CooldownService;
import co.aegisrpg.framework.exceptions.postconfigured.InvalidInputException;
import co.aegisrpg.framework.exceptions.postconfigured.PlayerNotOnlineException;
import co.aegisrpg.models.chat.Channel;
import co.aegisrpg.api.common.utils.TimeUtils.TickTime;
import co.aegisrpg.models.chat.Chatter;
import co.aegisrpg.models.chat.PrivateChannel;
import co.aegisrpg.models.chat.PublicChannel;
import co.aegisrpg.models.nerd.Rank;
import co.aegisrpg.utils.JsonBuilder;
import co.aegisrpg.utils.PlayerUtils;
import co.aegisrpg.utils.Tasks;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.MessageType;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static co.aegisrpg.utils.PlayerUtils.canSee;
import static co.aegisrpg.utils.StringUtils.decolorize;
import static co.aegisrpg.utils.StringUtils.stripColor;

public class ChatManager {
    @Getter
    private static List<PublicChannel> channels = new ArrayList<>();

    @Getter
    @Setter
    private static PublicChannel mainChannel;

    public static PublicChannel getChannel(String id) {
        if (id == null) return null;
        Optional<PublicChannel> channel = channels.stream().filter(_channel -> _channel.getNickname().equalsIgnoreCase(id)).findFirst();
        if (!channel.isPresent())
            channel = channels.stream().filter(_channel -> _channel.getName().equalsIgnoreCase(id)).findFirst();
        if (!channel.isPresent())
            channel = channels.stream().filter(_channel -> _channel.getName().toLowerCase().startsWith(id.toLowerCase())).findFirst();

        return channel.orElseThrow(() -> new InvalidInputException("Channel '" + id + "' not found"));
    }

    public static Optional<PublicChannel> getChannelByDiscordId(String id) {
        return channels.stream().filter(_channel -> _channel.getDiscordTextChannel() != null && _channel.getDiscordTextChannel().getId().equalsIgnoreCase(id)).findFirst();
    }

    public static void addChannel(PublicChannel channel) {
        channels.add(channel);
    }

    public static void process(Chatter chatter, Channel channel, String originalMessage) {
        Tasks.async(() -> {
            String message = originalMessage;
            if (chatter == null || message == null)
                return;

            message = message.trim();

            if (!Rank.of(chatter.getOnlinePlayer()).isAdmin())
                message = decolorize(message).replaceAll("&", "&" + channel.getMessageColor());

            if (message.isEmpty())
                return;

            if (channel == null) {
                chatter.sendMessage(Chat.PREFIX + "&cYou are not speaking in a channel. &3Use &c/ch g &3to return to Global chat.");
                return;
            }

            try {
                Set<Chatter> recipients = channel.getRecipients(chatter);
                if (channel instanceof PublicChannel publicChannel) {
                    if (!chatter.canJoin(publicChannel))
                        throw new InvalidInputException(publicChannel.getJoinError());
                    if (!chatter.isInValidWorld(publicChannel))
                        throw new InvalidInputException("You cannot join that channel in this world");

                    if ("G".equals(publicChannel.getNickname())) {
                        if ("Tuniab".equals(chatter.getName())) {
                            String id = "chat-" + publicChannel.getName().toLowerCase();
                            CooldownService service = new CooldownService();
                            if (!service.check(chatter.getUuid(), id, TickTime.SECOND.x(5)))
                                throw new InvalidInputException("You are talking too fast! (&e" + service.getDiff(chatter.getUuid(), id) + " left&c)");
                        }
                    }

                    PublicChatEvent event = new PublicChatEvent(chatter, publicChannel, message, message, recipients);
                    if (event.callEvent())
                        process(event);
                } else if (channel instanceof PrivateChannel) {
                    PrivateChatEvent event = new PrivateChatEvent(chatter, (PrivateChannel) channel, message, message, recipients);
                    if (event.callEvent())
                        process(event);
                }
            } catch (InvalidInputException ex) {
                PlayerUtils.send(chatter.getOnlinePlayer(), Chat.PREFIX + "&c" + ex.getMessage());
            }
        });
    }

    public static void process(PublicChatEvent event) {
        event.checkWasSeen();

        event.getRecipients().forEach(recipient -> {
            JsonBuilder json = event.getChannel().getChatterFormat(event.getChatter(), recipient, false)
                    .color(event.getChannel().getMessageColor())
                    .group()
                    .next(event.getMessage());

            if (event.isFiltered())
                if (Rank.of(recipient).isStaff())
                    json.next(" ")
                            .group()
                            .next("&c&l*")
                            .hover("&cChat message was filtered")
                            .hover("&cClick to see original message")
                            .command("/echo &3Original message: &f" + event.getOriginalMessage());

            // TODO - 1.19.2 Chat Validation Kick
            // recipient.sendMessage(event, json, MessageType.CHAT);
            recipient.sendMessage(json);
        });

        JsonBuilder json = event.getChannel().getChatterFormat(event.getChatter(), null, false)
                .color(event.getChannel().getMessageColor())
                .group()
                .next(event.getMessage());

        if (event.isFiltered())
            json.next(" *");

        Bukkit.getConsoleSender().sendMessage(stripColor(json.toString()));
    }

    public static void process(PrivateChatEvent event) {
        JsonBuilder to = new JsonBuilder("&3&l[&bPM&3&l] &eTo &3" + event.getRecipientNames() + " &b&l> ")
                .next(event.getChannel().getMessageColor() + event.getMessage());
        JsonBuilder from = new JsonBuilder("&3&l[&bPM&3&l] &eFrom &3" + Nickname.of(event.getChatter()) + " &b&l> ")
                .next(event.getChannel().getMessageColor() + event.getMessage());

        int seen = 0;
        for (Chatter recipient : event.getRecipients()) {
            recipient.setLastPrivateMessage(event.getChannel());

            if (!recipient.equals(event.getChatter())) {
                boolean canSee = canSee(event.getChatter(), recipient);
                JsonBuilder notOnline = new JsonBuilder(Chat.PREFIX).next(new PlayerNotOnlineException(recipient).getJson());

                if (!recipient.isOnline())
                    event.getChatter().sendMessage(notOnline);
                else {
                    // TODO - 1.19.2 Chat Validation Kick
                    // recipient.sendMessage(event, from, MessageType.CHAT);
                    recipient.sendMessage(from);
                    if (canSee)
                        ++seen;
                    else
                        event.getChatter().sendMessage(notOnline);
                }
            }
        }

        if (seen > 0) {
            // TODO - 1.19.2 Chat Validation Kick
            // event.getChatter().sendMessage(event, to, MessageType.CHAT);
            event.getChatter().sendMessage(to, MessageType.SYSTEM);
        }

        Bukkit.getConsoleSender().sendMessage(Nickname.of(event.getChatter()) + " -> " + event.getRecipientNames() + ": " + event.getMessage());
    }

}
