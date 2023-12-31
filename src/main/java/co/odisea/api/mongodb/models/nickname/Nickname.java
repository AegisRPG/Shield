package co.odisea.api.mongodb.models.nickname;

import co.odisea.api.common.utils.UUIDUtils;
import co.odisea.api.mongodb.interfaces.PlayerOwnedObject;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import gg.projecteden.api.interfaces.HasUniqueId;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static co.odisea.api.common.utils.Nullables.isNullOrEmpty;

@Getter
@Builder
@Entity(value = "nickname", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters(UUIDConverter.class)
public class Nickname implements PlayerOwnedObject {
    @Id
    @NonNull
    protected UUID uuid;

    protected String nickname;

    public static String of(String name) {
        return new NicknameService().get(name).getNickname();
    }

    public static String of(HasUniqueId player) {
        return new NicknameService().get(player).getNickname();
    }

    public static String of(UUID uuid) {
        return new NicknameService().get(uuid).getNickname();
    }

    public @NotNull String getNickname() {
        if (UUIDUtils.isUUID0(uuid))
            return "Console";
        if (isNullOrEmpty(nickname))
            return getName();
        return nickname;
    }

    public String getNicknameRaw() {
        return nickname;
    }

    public boolean hasNickname() {
        return !isNullOrEmpty(nickname);
    }

}