package co.odisea.api.common.utils;

import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Map;

public class Nullables {

    @Contract("null -> true; !null -> _")
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    @Contract("null -> false; !null -> _")
    public static boolean isNotNullOrEmpty(String string) {
        return !isNullOrEmpty(string);
    }

    @Contract("null -> true; !null -> _")
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    @Contract("null -> false; !null -> _")
    public static boolean isNotNullOrEmpty(Collection<?> collection) {
        return !isNullOrEmpty(collection);
    }

    @Contract("null -> true; !null -> _")
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    @Contract("null -> false; !null -> _")
    public static boolean isNotNullOrEmpty(Map<?, ?> map) {
        return !isNullOrEmpty(map);
    }

}
