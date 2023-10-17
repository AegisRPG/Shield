package co.odisea.features.chat.events;

import co.odisea.api.mongodb.models.nickname.Nickname;
import co.odisea.features.chat.Chat;
import co.odisea.models.freeze.FreezeService;
import co.odisea.utils.Tasks;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class MinecraftChatEvent extends ChatEvent implements Identified {

    public abstract boolean wasSeen();

    @Override
    public String getOrigin() {
        return Nickname.of(getChatter());
    }

    @Override
    public @NonNull Identity identity() {
        return getChatter().identity();
    }

    public void checkWasSeen() {
        if (!wasSeen() && !new FreezeService().get(getChatter()).isFrozen())
            Tasks.wait(1, () -> getChatter().sendMessage(Chat.PREFIX + "No one can hear you! Type &c/ch g &3to talk globally"));
    }

}
