package co.odisea.api.interfaces;

import gg.projecteden.api.interfaces.Named;
import org.jetbrains.annotations.NotNull;

public interface Nicknamed extends Named {
    /** @deprecated */
    @Deprecated
    @NotNull String getName();

    @NotNull String getNickname();
}
