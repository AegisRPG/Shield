package co.odisea.models.warps;

import co.odisea.framework.exceptions.postconfigured.InvalidInputException;
import co.odisea.framework.interfaces.PlayerOwnedObject;
import co.odisea.framework.persistence.serializer.mongodb.LocationConverter;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import gg.projecteden.parchment.OptionalLocation;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static co.odisea.utils.Extensions.camelCase;

@Data
@Entity(value = "warps", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocationConverter.class})
public class Warps implements PlayerOwnedObject {
    @Id
    @NonNull
    private UUID uuid;
    private Map<WarpType, List<Warp>> warps = new ConcurrentHashMap<>();

    public List<Warp> getAll(WarpType type) {
        return warps.getOrDefault(type, new ArrayList<>());
    }

    public Warp get(WarpType type, String name) {
        return getAll(type).stream().filter(warp -> warp.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void add(Warp warp) {
        warps.computeIfAbsent(warp.getType(), $ -> new ArrayList<>()).add(warp);
    }

    public void delete(WarpType type, String name) {
        getAll(type).removeIf(warp -> warp.getName().equalsIgnoreCase(name));
    }

    public void delete(Warp warp) {
        getAll(warp.getType()).remove(warp);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Converters(LocationConverter.class)
    public static class Warp implements OptionalLocation {
        private String name;
        private WarpType type;
        private Location location;

        public @NotNull CompletableFuture<Boolean> teleportAsync(Player player) {
            return teleportAsync(player, PlayerTeleportEvent.TeleportCause.COMMAND);
        }

        public @NotNull CompletableFuture<Boolean> teleportAsync(Player player, PlayerTeleportEvent.TeleportCause teleportCause) {
            if (location == null)
                throw new InvalidInputException("Location of " + camelCase(type) + " warp " + name + " is null!");
            if (location.getWorld() == null)
                throw new InvalidInputException("World of " + camelCase(type) + " warp " + name + " is null!");

            return player.teleportAsync(location, teleportCause);
        }
    }

}
