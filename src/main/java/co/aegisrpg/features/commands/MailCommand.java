package co.aegisrpg.features.commands;

import co.aegisrpg.api.common.utils.TimeUtils;
import co.aegisrpg.api.mongodb.models.nickname.Nickname;
import co.aegisrpg.features.menus.api.ClickableItem;
import co.aegisrpg.features.menus.api.TemporaryMenuListener;
import co.aegisrpg.features.menus.api.annotations.Title;
import co.aegisrpg.features.menus.api.content.InventoryProvider;
import co.aegisrpg.framework.commands.models.CustomCommand;
import co.aegisrpg.framework.commands.models.annotations.*;
import co.aegisrpg.framework.commands.models.annotations.Redirects.Redirect;
import co.aegisrpg.framework.commands.models.cooldown.CooldownService;
import co.aegisrpg.framework.commands.models.events.CommandEvent;
import co.aegisrpg.models.mail.Mailer;
import co.aegisrpg.models.mail.MailerService;
import co.aegisrpg.utils.*;
import co.aegisrpg.utils.worldgroup.WorldGroup;
import co.aegisrpg.models.mail.Mailer.Mail;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static co.aegisrpg.api.common.utils.Nullables.isNullOrEmpty;

@NoArgsConstructor
@Aliases("delivery")
@Redirect(from = "/mailbox", to = "/mail box")
public class MailCommand extends CustomCommand implements Listener {
    public static final String PREFIX = StringUtils.getPrefix("Mail");
    private final MailerService service = new MailerService();
    private Mailer from;

    public MailCommand(@NonNull CommandEvent event) {
        super(event);
        if (isPlayerCommandEvent())
            from = service.get(player());
    }

    @Path("send <player> [message...]")
    @Description("Send a message and/or items to a player")
    void send(Mailer to, String message) {
        if (from.getUuid().equals(to.getUuid()))
            error("You cannot send mail yourself");

        if (from.hasPending())
            send(PREFIX + "&cYou already have pending mail to " + from.getPending().getNickname());
        else
            from.addPending(new Mail(to.getUuid(), uuid(), worldGroup(), message));

        save(from);
        menu();
    }

    @Path("menu")
    @HideFromHelp
//    @HideFromWiki
    @TabCompleteIgnore
    private void menu() {
        Mail mail = from.getPending();
        line(3);
        send(PREFIX + "Sending mail to " + mail.getNickname() + " with " + mail.getContents());
        send(json("&3   ")
                .next("&c&lCancel")
                .command("/mail cancel")
                .hover("&cClick to cancel")
                .group()
                .next("  &3|  ")
                .group()
                .next("&e&l" + (mail.hasMessage() ? "Edit" : "Add") + " Message")
                .suggest("/mail message " + (mail.hasMessage() ? new ItemBuilder(mail.getMessage()).getBookPlainContents() : ""))
                .hover("&eClick to " + (mail.hasMessage() ? "edit" : "add a") + " message")
                .group()
                .next("  &3|  ")
                .group()
                .next("&e&l" + (mail.hasItems() ? "Edit" : "Add") + " Items")
                .command("/mail items")
                .hover("&6Click to " + (mail.hasItems() ? "edit" : "add") + " items")
                .group()
                .next("  &3|  ")
                .group()
                .next("&a&lSend")
                .command("/mail confirm")
                .hover("&aClick to send"));
    }

    @Path("cancel")
    @HideFromHelp
//    @HideFromWiki
    @TabCompleteIgnore
    void cancel() {
        Mail mail = from.getPending();
        mail.cancel();
        save(from);
        send(PREFIX + "Mail to " + mail.getNickname() + " cancelled");
    }

    @Path("message [message...]")
    @HideFromHelp
//    @HideFromWiki
    @TabCompleteIgnore
    void message(String message) {
        from.getPending().setMessage(message);
        save(from);
        menu();
    }

    @Path("items")
    @HideFromHelp
//    @HideFromWiki
    @TabCompleteIgnore
    void items() {
        new EditItemsMenu(from.getPending());
    }

    @Path("confirm")
    @HideFromHelp
//    @HideFromWiki
    @TabCompleteIgnore
    void confirm() {
        Mail mail = from.getPending();
        mail.send();
        save(mail.getOwner());
        save(mail.getFromMailer());
        send(PREFIX + "Your mail is on its way to " + mail.getNickname());
        mail.getOwner().sendNotification();
    }

    @Path("box")
    @Description("View your mail box")
    void box() {
        new MailBoxMenu(from).open(player());
    }

    private void save(Mailer mailer) {
        service.save(mailer);
    }

    @Data
    public static class EditItemsMenu implements TemporaryMenuListener {
        private final String title;
        private final Player player;
        private final Mail mail;

        public EditItemsMenu(Mail mail) {
            this.title = (mail.hasItems() ? "Add" : "Edit") + " Items to " + mail.getNickname();
            this.player = mail.getFromMailer().getOnlinePlayer();
            this.mail = mail;

            open(6, mail.getItems());
        }

        @Override
        public void onClose(InventoryCloseEvent event, List<ItemStack> contents) {
            mail.setItems(contents);
            new MailerService().save(mail.getFromMailer());
            PlayerUtils.runCommand(player, "mail menu");
        }
    }

    @Title("&3Your Deliveries")
    public static class MailBoxMenu extends InventoryProvider {
        private final Mailer mailer;
        private final WorldGroup worldGroup;

        public MailBoxMenu(Mailer mailer) {
            this.mailer = mailer;
            this.worldGroup = mailer.getWorldGroup();
        }

        @Override
        public void open(Player viewer, int page) {
            if (isNullOrEmpty(mailer.getUnreadMail(worldGroup)))
                viewer.sendMessage(JsonBuilder.fromError("Mail", "There is no mail in your " + StringUtils.camelCase(worldGroup) + " mailbox"));
            else
                super.open(viewer, page);
        }

        @Override
        public void init() {
            addCloseItem();

            ItemStack info = new ItemBuilder(Material.BOOK).name("&3Info")
                    .lore("&eOpened mail cannot be closed", "&eAny items left over, will be", "&egiven to you, or dropped")
                    .loreize(false)
                    .build();

            contents.set(0, 8, ClickableItem.empty(info));

            List<ClickableItem> items = new ArrayList<>();

            List<Mail> mails = mailer.getUnreadMail(worldGroup);

            for (Mail mail : mails)
                items.add(ClickableItem.of(mail.getDisplayItem().build(), e -> new OpenMailMenu(mail)));

            paginate(items);
        }
    }

    @Data
    public static class OpenMailMenu implements TemporaryMenuListener {
        private final Mail mail;
        private final Mailer mailer;
        private final Player player;
        private final String title;

        public OpenMailMenu(Mail mail) {
            this.mail = mail;
            this.mailer = mail.getOwner();
            this.player = mailer.getOnlinePlayer();
            this.title = "From " + Nickname.of(mail.getFrom());

            open(6, mail.getAllItems());

            mail.received();
            new MailerService().save(mailer);
        }

        @Override
        public void onClose(InventoryCloseEvent event, List<ItemStack> contents) {
            PlayerUtils.giveItems((Player) event.getPlayer(), contents);

            Tasks.wait(1, () -> {
                if (!mailer.getMail(mail.getWorldGroup()).isEmpty())
                    new MailBoxMenu(mailer).open(player);
            });
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        WorldGroup worldGroup = WorldGroup.of(player);
        Mailer mailer = service.get(player);

        List<Mail> mails = new ArrayList<>(mailer.getMail(worldGroup));
        if (isNullOrEmpty(mails))
            return;

        if (!new CooldownService().check(player, "youhavemail", TimeUtils.TickTime.MINUTE.x(5)))
            return;

        mailer.sendNotification();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Mailer user = service.get(event.getPlayer());

        if (!user.getMail().isEmpty())
            Tasks.wait(3, user::sendNotification);
    }

    //TODO: uncomment if doing custom decor

//    @EventHandler
//    public void on(DecorationInteractEvent event) {
//        final Player player = event.getPlayer();
//        final UUID owner = event.getDecoration().getOwner(player);
//
//        if (!event.getDecoration().is(DecorationType.MAILBOX))
//            return;
//
//        if (owner == null) {
//            PlayerUtils.send(player, PREFIX + "Could not determine owner of mailbox");
//            return;
//        }
//
//        if (player.getUniqueId().equals(owner)) {
//            PlayerUtils.runCommand(player, "mail box");
//            return;
//        }
//
//        final MailerService service = new MailerService();
//        Mailer from = service.get(player);
//        Mailer to = service.get(owner);
//
//        if (from.hasPending()) {
//            if (!from.getPending().getUuid().equals(to.getUuid())) {
//                send(PREFIX + "&cYou already have pending mail to " + from.getPending().getNickname());
//                return;
//            }
//        } else
//            from.addPending(new Mail(to.getUuid(), from.getUuid(), WorldGroup.of(player), null));
//
//        save(from);
//
//        new EditItemsMenu(from.getPending());
//    }

}
