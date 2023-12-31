package co.odisea.features.chat.commands;

import co.odisea.features.chat.Chat;
import co.odisea.features.chat.ChatManager;
import co.odisea.features.chat.events.ChannelChangeEvent;
import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.*;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.models.chat.Channel;
import co.odisea.models.chat.Chatter;
import co.odisea.models.chat.ChatterService;
import co.odisea.models.chat.PublicChannel;
import co.odisea.framework.commands.models.annotations.Redirects.Redirect;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static co.odisea.api.common.utils.Nullables.isNullOrEmpty;

@Aliases({"ch", "channel"})
@Redirect(from = "/qm", to = "/ch qm")
public class ChatCommand extends CustomCommand {
	private final Chatter chatter;

	public ChatCommand(@NonNull CommandEvent event) {
		super(event);
		PREFIX = Chat.PREFIX;
		chatter = new ChatterService().get(player());
	}

	@Path("<channel> [message...]")
	@Description("Switch to a channel or send a message without switching")
	void changeChannel(PublicChannel channel, String message) {
		if (!isNullOrEmpty(message)) {
			quickMessage(channel, message);
			return;
		}

		final Channel currentChannel = chatter.getActiveChannel();
		if (channel.equals(currentChannel))
			error("You are already in that channel");

		if (!chatter.isInValidWorld(channel))
			error("You cannot join that channel in this world");

		chatter.setActiveChannel(channel);
		new ChannelChangeEvent(chatter, currentChannel, channel).callEvent();
	}

	@Path("list [filter]")
	@Description("List available channels")
	void list(String filter) {
		ChatManager.getChannels().forEach(channel -> {
			if (!isNullOrEmpty(filter) && !channel.getName().toLowerCase().startsWith(filter))
				return;

			if (chatter.canJoin(channel))
				send(channel.getColor() + "[" + channel.getNickname().toUpperCase() + "] " + channel.getName() + " " +
						(chatter.hasJoined(channel) ? chatter.getActiveChannel().equals(channel) ? "&a(Active)" : "&7(Joined)" : "&c(Left)"));
		});
	}

//	@HideFromWiki
	@HideFromHelp
	@TabCompleteIgnore
	@Path("qm <channel> <message...>")
	@Description("Send a message to a channel")
	void quickMessage(PublicChannel channel, String message) {
		chatter.say(channel, message);
	}

	@Path("join <channel>")
	@Description("Join a channel")
	void join(PublicChannel channel) {
		final Channel currentChannel = chatter.getActiveChannel();
		if (channel.equals(currentChannel))
			error("You are already in that channel");
		if (!chatter.isInValidWorld(channel))
			error("You cannot join that channel in this world");
		chatter.join(channel);
	}

	@Path("leave <channel>")
	@Description("Leave a channel")
	void leave(PublicChannel channel) {
		chatter.leave(channel);
	}

	@ConverterFor({Channel.class, PublicChannel.class})
	PublicChannel convertToChannel(String value) {
		return ChatManager.getChannel(value);
	}

	@TabCompleterFor({Channel.class, PublicChannel.class})
	List<String> tabCompleteChannel(String filter) {
		return ChatManager.getChannels().stream()
				.filter(channel -> {
					if (!new ChatterService().get(player()).canJoin(channel))
						return false;
					return channel.getNickname().toLowerCase().startsWith(filter.toLowerCase()) ||
							channel.getName().toLowerCase().startsWith(filter.toLowerCase());
				})
				.map(PublicChannel::getNickname)
				.collect(Collectors.toList());
	}

	@ConverterFor(Chatter.class)
	Chatter convertToChatter(String value) {
		return new ChatterService().get(convertToPlayer(value));
	}

}
