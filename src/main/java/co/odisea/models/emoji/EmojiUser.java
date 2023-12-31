package co.odisea.models.emoji;

import co.odisea.framework.interfaces.PlayerOwnedObject;
import co.odisea.framework.persistence.serializer.mongodb.LocationConverter;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PostLoad;
import dev.morphia.converters.UUIDConverter;
import lombok.*;

import java.util.*;

@Data
@Entity(value = "emoji_user", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocationConverter.class})
public class EmojiUser implements PlayerOwnedObject {
    @Id
    @NonNull
    private UUID uuid;
    private Set<String> owned = new HashSet<>();

    public boolean owns(Emoji emoji) {
        return owned.contains(emoji.getName());
    }

    public void give(Emoji emoji) {
        owned.add(emoji.getName());
    }

    @PostLoad
    void fix() {
        final Map<String, String> converter = Map.of(
                "+1", "thumbsup",
                "-1", "thumbsdown"
        );

        converter.forEach((oldName, newName) -> {
            if (owned.contains(oldName)) {
                owned.remove(oldName);
                owned.add(newName);
            }
        });
    }

    @Data
    public static class Emoji {
        private final String name;
        private final String emoji;
        private final boolean purchasable;

        public static final List<Emoji> EMOJIS = new ArrayList<>();

        public static Emoji of(String name) {
            for (Emoji emoji : EMOJIS)
                if (emoji.getName().equals(name))
                    return emoji;
            return null;
        }

    }

}
