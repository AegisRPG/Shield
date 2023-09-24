package co.aegisrpg.features.vanish;

import co.aegisrpg.Shield;
import co.aegisrpg.features.vanish.events.PreVanishToggleEvent;
import co.aegisrpg.features.vanish.events.VanishToggleEvent;
import co.aegisrpg.framework.features.Feature;
import co.aegisrpg.models.nerd.Nerd;
import co.aegisrpg.models.vanish.VanishUser;
import co.aegisrpg.models.vanish.VanishUserService;
import co.aegisrpg.utils.PlayerUtils.ShowPlayer;
import co.aegisrpg.utils.PlayerUtils.HidePlayer;
import co.aegisrpg.api.common.utils.TimeUtils.TickTime;
import co.aegisrpg.models.vanish.VanishUser.Setting;
import co.aegisrpg.utils.*;
import co.aegisrpg.utils.PlayerUtils.OnlinePlayers;
import gg.projecteden.api.interfaces.HasUniqueId;
import gg.projecteden.parchment.OptionalPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

public class Vanish extends Feature {
    public static final String PREFIX = StringUtils.getPrefix(Vanish.class);
    private static final VanishUserService service = new VanishUserService();
    private static final PotionEffectBuilder NIGHT_VISION = new PotionEffectBuilder(PotionEffectType.NIGHT_VISION).infinite();

    @Override
    public void onStart() {
        new VanishListener();

        Tasks.repeat(0, TickTime.SECOND.x(2), () -> {
//			Vanish.refreshAll();

            OnlinePlayers.getAll().stream()
                    .map(Vanish::get)
                    .filter(VanishUser::isVanished)
                    .forEach(user -> {
                        ActionBarUtils.sendActionBar(user.getOnlinePlayer(), "&3You are vanished!");

                        if (user.getSetting(Setting.NIGHT_VISION))
                            user.getOnlinePlayer().addPotionEffect(NIGHT_VISION.build());
                    });
        });
    }

    public static VanishUser get(HasUniqueId player) {
        return service.get(player);
    }

    public static boolean vanish(Player player) {
        if (get(player).isVanished())
            return true;

        if (!new PreVanishToggleEvent(player).callEvent())
            return false;

        service.edit(player, user -> {
            user.setVanished(true);

            player.setMetadata("vanished", new FixedMetadataValue(Shield.getInstance(), true));

            if (user.getSetting(Setting.NIGHT_VISION))
                player.addPotionEffect(NIGHT_VISION.build());
        });

        refresh(player);

        return new VanishToggleEvent(player).callEvent();
    }

    public static boolean unvanish(Player player) {
        if (!get(player).isVanished())
            return true;

        if (!new PreVanishToggleEvent(player).callEvent())
            return false;

        service.edit(player, user -> {
            user.setVanished(false);

            player.removeMetadata("vanished", Shield.getInstance());

            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                if (user.getSetting(Setting.NIGHT_VISION) && !Nerd.of(user).isNightVision())
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        });

        refresh(player);

        return new VanishToggleEvent(player).callEvent();
    }

    public static void refreshAll() {
        OnlinePlayers.getAll().forEach(Vanish::refresh);
    }

    public static void refresh(Player player) {
        refresh(get(player));
    }

    public static void refresh(VanishUser user) {
        if (user.isVanished())
            new HidePlayer(user).from(OnlinePlayers.where(user::canHideFrom).get());
        else
            new ShowPlayer(user).toAll();
    }

    public static boolean isVanished(OptionalPlayer player) {
        if (player.getPlayer() == null)
            return false;

        return get(player.getPlayer()).isVanished();
    }
}
