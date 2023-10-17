package co.odisea.api.interfaces;

import gg.projecteden.api.interfaces.OptionalUniqueId;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface HasUniqueId extends OptionalUniqueId {
    @NotNull UUID getUniqueId();
}
