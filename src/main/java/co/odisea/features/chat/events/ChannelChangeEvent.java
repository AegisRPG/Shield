package co.odisea.features.chat.events;

import co.odisea.models.chat.Channel;
import co.odisea.models.chat.Chatter;
import lombok.Data;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Data
public class ChannelChangeEvent extends Event {
    private final Chatter chatter;
    private final Channel previousChannel, newChannel;

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
