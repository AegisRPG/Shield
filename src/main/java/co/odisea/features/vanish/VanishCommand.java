package co.odisea.features.vanish;

import co.odisea.features.store.perks.chat.joinquit.*;
import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.Description;
import co.odisea.framework.commands.models.annotations.Path;
import co.odisea.framework.commands.models.annotations.Permission;
import co.odisea.framework.commands.models.annotations.Permission.Group;
import co.odisea.framework.commands.models.annotations.Redirects.Redirect;
import co.odisea.models.vanish.VanishUser.Setting;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.models.vanish.VanishUser;
import co.odisea.models.vanish.VanishUserService;
import lombok.NonNull;

@Redirect(from = {"/vanish fj", "/fj"}, to = "/vanish fakejoin")
@Redirect(from = {"/vanish fq", "/fq"}, to = "/vanish fakequit")
@Redirect(from = {"/vanish ni", "/ni"}, to = "/vanish settings interact")
@Permission(Group.MODERATOR)
public class VanishCommand extends CustomCommand {
    private static final VanishUserService service = new VanishUserService();
    private VanishUser user;

    public VanishCommand(@NonNull CommandEvent event) {
        super(event);
        if (isPlayerCommandEvent())
            user = service.get(player());
    }

    @Path("[state]")
    @Description("Toggle vanish")
    void toggle(Boolean state) {
        if (state == null)
            state = !user.isVanished();

        if (state) {
            if (user.isVanished())
                error("You are already vanished");

            Vanish.vanish(player());
        } else {
            if (!user.isVanished())
                error("You are already unvanished");

            Vanish.unvanish(player());
        }
    }

    @Path("fakeJoin")
    @Description("Send a fake join message and unvanish")
    void fakeJoin() {
        if (user.isUnvanished())
            error("You are already unvanished");

        Vanish.unvanish(player());
        JoinQuit.join(player());
    }

    @Path("fakeQuit")
    @Description("Send a fake quit message and vanish")
    void fakeQuit() {
        if (user.isVanished())
            error("You are already vanished");

        JoinQuit.quit(player());
        Vanish.vanish(player());
    }

    @Path("settings")
    @Description("View your vanish settings")
    void settings() {
        line();
        send(PREFIX + "Settings");
        line();
        for (Setting setting : Setting.values()) {
            final boolean state = user.getSetting(setting);
            send(json(" &e" + camelCase(setting) + " &7- " + (state ? "&aEnabled" : "&cDisabled"))
                    .hover("&3Click to toggle")
                    .command("/vanish settings " + setting.name().toLowerCase() + " " + !state));
        }
    }

    @Path("settings <setting> [state]")
    @Description("Toggle vanish settings")
    void settings(Setting setting, Boolean state) {
        if (state == null)
            state = !user.getSetting(setting);

        user.setSetting(setting, state);
        service.save(user);
        send(PREFIX + setting.getVerb() + " " + (state ? "&aenabled" : "&cdisabled"));
    }

}