package co.odisea.features.chat.events;

import co.odisea.models.chat.Chatter;
import co.odisea.models.chat.ChatterService;
import co.odisea.models.chat.PublicChannel;
import co.odisea.utils.PlayerUtils.OnlinePlayers;
import co.odisea.utils.PlayerUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DiscordChatEvent extends ChatEvent {
    private Member member;
    private PublicChannel channel;
    private final String originalMessage;
    private String message;
    private String permission;
    @Accessors(fluent = true)
    private boolean hasAttachments;
    private boolean filtered;
    private boolean bad;

    public DiscordChatEvent(Member member, PublicChannel channel, String message) {
        this(member, channel, message, message, false, null);
    }

    public DiscordChatEvent(Member member, PublicChannel channel, String originalMessage, String message, boolean hasAttachments, String permission) {
        this.member = member;
        this.channel = channel;
        this.originalMessage = originalMessage;
        this.message = message;
        this.hasAttachments = hasAttachments;
        this.permission = permission;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public Chatter getChatter() {
        if (member != null) {
            DiscordUser user = new DiscordUserService().getFromUserId(member.getUser().getId());
            if (user != null)
                return new ChatterService().get(PlayerUtils.getPlayer(user.getUuid()));
        }
        return null;
    }

    @Override
    public String getOrigin() {
        if (getChatter() != null)
            return getChatter().getNickname();
        return Discord.getName(member);
    }

    public GuildMessageChannel getDiscordTextChannel() {
        return channel.getDiscordTextChannel().get(Bot.RELAY.jda());
    }


    @Override
    public Set<Chatter> getRecipients() {
        return OnlinePlayers.getAll().stream()
                .filter(player -> permission == null || player.hasPermission(permission))
                .map(player -> new ChatterService().get(player))
                .collect(Collectors.toSet());
    }

}
