package co.aegisrpg.api.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HasUniqueId extends OptionalUniqueId {

    @NotNull UUID getUniqueId();

}
