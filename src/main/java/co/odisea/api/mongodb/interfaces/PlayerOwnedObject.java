package co.odisea.api.mongodb.interfaces;

import co.odisea.api.common.utils.Nullables;
import co.odisea.api.common.utils.StringUtils;
import co.odisea.api.mongodb.models.nerd.Nerd;
import co.odisea.api.mongodb.models.nickname.Nickname;
import co.odisea.api.mongodb.models.nickname.NicknameService;
import gg.projecteden.api.interfaces.DatabaseObject;
import gg.projecteden.api.interfaces.HasUniqueId;
import gg.projecteden.api.interfaces.Nicknamed;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerOwnedObject extends DatabaseObject, Nicknamed, HasUniqueId {

    @NotNull UUID getUuid();

    /**
     * Gets the unique ID of this object. Alias for {@link #getUuid()}, for compatibility with {@link HasUniqueId}.
     *
     * @return this object's unique ID
     */
    @Override
    @NotNull
    default UUID getUniqueId() {
        return getUuid();
    }

    default @NotNull Nerd getNerd() {
        return Nerd.of(getUuid());
    }

    default @NotNull String getName() {
        return getNerd().getName();
    }

    default @NotNull String getNickname() {
        return Nickname.of(getUuid());
    }

    default Nickname getNicknameData() {
        return new NicknameService().get(getUuid());
    }

    default boolean hasNickname() {
        return !Nullables.isNullOrEmpty(getNicknameData().getNicknameRaw());
    }

    default String toPrettyString() {
        return StringUtils.toPrettyString(this);
    }

/* fuck
	default boolean equals(Object obj) {
		if (!this.getClass().equals(obj.getClass())) return false;
		return getUuid().equals(((PlayerOwnedObject) obj).getUuid());
	}
*/

}
