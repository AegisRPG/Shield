package co.aegisrpg.api.common.utils;

import co.aegisrpg.api.common.ShieldAPI;

import java.util.Arrays;
import java.util.List;

public enum Env {
    DEV,
    TEST,
    PROD;

    public static boolean applies(Env... envs) {
        return applies(Arrays.asList(envs));
    }

    public static boolean applies(List<Env> envs) {
        return envs.contains(ShieldAPI.get().getEnv());
    }
}
