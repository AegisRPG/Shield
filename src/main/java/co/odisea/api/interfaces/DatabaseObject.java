package co.odisea.api.interfaces;

import gg.projecteden.api.interfaces.HasUniqueId;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface DatabaseObject extends HasUniqueId {
    UUID getUuid();

    default @NotNull UUID getUniqueId() {
        return this.getUuid();
    }
}