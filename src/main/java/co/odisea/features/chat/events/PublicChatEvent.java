package co.odisea.features.chat.events;

import co.odisea.models.chat.Chatter;
import co.odisea.models.chat.PublicChannel;
import co.odisea.features.chat.Chat.Broadcast;
import co.odisea.utils.PlayerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class PublicChatEvent extends MinecraftChatEvent {
    private final Chatter chatter;
    private PublicChannel channel;
    private final String originalMessage;
    private String message;
    private boolean filtered;
    private boolean bad;

    public PublicChatEvent(Chatter chatter, PublicChannel channel, String message) {
        this(chatter, channel, message, message, channel.getRecipients(chatter));
    }

    public PublicChatEvent(Chatter chatter, PublicChannel channel, String originalMessage, String message, Set<Chatter> recipients) {
        this.chatter = chatter;
        this.channel = channel;
        this.originalMessage = originalMessage;
        this.message = message;
        this.recipients = recipients;
    }

    private Set<Chatter> recipients;

    public boolean wasSeen() {
        if (channel.getDiscordTextChannel() != null) return true;
        return recipients.stream().anyMatch(recipient -> !chatter.getUniqueId().equals(recipient.getUniqueId()) &&
                PlayerUtils.canSee(chatter, recipient));
    }

    @Override
    public void respond(String response) {
        super.respond(response);
        Broadcast.discord().channel(channel).message(response).send();
    }

}
