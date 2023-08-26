package co.aegisrpg.framework.features;

import co.aegisrpg.utils.StringUtils;

import static org.bukkit.Bukkit.getName;

public class Feature {
    public String PREFIX = StringUtils.getPrefix(getName());
    public String DISCORD_PREFIX = StringUtils.getDiscordPrefix(getName());

    public String getName() {
        return Features.prettyName(this);
    }

    public String getPrefix() {
        return PREFIX;
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void reload() {
        onStop();
        onStart();
    }
}
