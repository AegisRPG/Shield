package co.aegisrpg.models.chat;

import com.github.twitch4j.helix.domain.*;
import net.md_5.bungee.api.*;

import java.util.*;

public interface Channel {

    Set<Chatter> getRecipients(Chatter chatter);

    String getAssignMessage(Chatter chatter);

    default ChatColor getMessageColor() {
        return ChatColor.WHITE;
    }
}
