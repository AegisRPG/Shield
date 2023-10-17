package co.odisea.models.emote;

import co.odisea.features.chat.Emotes;
import co.odisea.framework.interfaces.PlayerOwnedObject;
import co.odisea.utils.RandomUtils;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import net.md_5.bungee.api.ChatColor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Data
@Entity(value = "emote_user", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters(UUIDConverter.class)
public class EmoteUser implements PlayerOwnedObject {
    @Id
    @NonNull
    private UUID uuid;
    private boolean enabled = true;

    private Set<String> disabled = new HashSet<>();

    public String getKey(Emotes emote, ChatColor color) {
        String key = emote.name();
        if (color != null)
            key += "-" + color.getName().toUpperCase();
        return key;
    }

    public boolean isEnabled(Emotes emote) {
        return !disabled.contains(getKey(emote, null));
    }

    public boolean isEnabled(Emotes emote, ChatColor color) {
        return !disabled.contains(getKey(emote, color));
    }

    public List<ChatColor> getEnabledColors(Emotes emote) {
        return emote.getColors().stream().filter(color -> isEnabled(emote, color)).collect(toList());
    }

    public ChatColor getRandomColor(Emotes emote) {
        return RandomUtils.randomElement(getEnabledColors(emote));
    }

    public boolean enable(Emotes emote, ChatColor color) {
        String key = getKey(emote, color);
        if (disabled.contains(key)) {
            disabled.remove(key);
            return true;
        }

        return false;
    }

    public boolean disable(Emotes emote, ChatColor color) {
        String key = getKey(emote, color);
        if (!disabled.contains(key)) {
            disabled.add(key);
            return true;
        }

        return false;
    }

}

