package co.aegisrpg.framework.commands;

import co.aegisrpg.utils.Nullables;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.stream.Collectors;

import static co.aegisrpg.api.common.utils.StringUtils.trimFirst;

@NoArgsConstructor
public class CommandListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        for (Map.Entry<String, String> redirect : Commands.getRedirects().entrySet()) {
            if (!(event.getMessage() + " ").toLowerCase().startsWith(redirect.getKey() + " "))
                continue;

            event.setCancelled(true);
            String command = redirect.getValue() + event.getMessage().substring(redirect.getKey().length());
            PlayerUtils.runCommand(event.getPlayer(), trimFirst(command));
            return;
        }
    }

    @EventHandler
    @SneakyThrows
    public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
        String buffer = event.getBuffer();
        if ((!event.isCommand() && !buffer.startsWith("/")) || buffer.indexOf(' ') == -1)
            return;

        List<String> args = new ArrayList<>(Arrays.asList(buffer.split(" ")));
        String alias = trimFirst(args.get(0));
        CustomCommand customCommand = Commands.get(alias);
        if (customCommand == null)
            return;

        boolean lastIndexIsEmpty = Nullables.isNullOrEmpty(args.get(args.size() - 1));
        args.removeIf(Strings::isNullOrEmpty);
        if (lastIndexIsEmpty || buffer.endsWith(" ")) args.add("");
        args.remove(0);

        CommandTabEvent tabEvent = new CommandTabEvent(event.getSender(), customCommand, alias, args, Collections.unmodifiableList(args));
        if (!tabEvent.callEvent())
            return;

        List<String> completions = customCommand.tabComplete(tabEvent);
        if (completions == null)
            return;

        event.setCompletions(completions.stream().distinct().collect(Collectors.toList()));
        event.setHandled(true);
    }

}
