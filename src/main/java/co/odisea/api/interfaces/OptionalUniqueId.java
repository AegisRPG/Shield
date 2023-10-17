package co.odisea.api.interfaces;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface OptionalUniqueId {
    @Nullable UUID getUniqueId();
}
