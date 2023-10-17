package co.odisea.features.commands.staff;

import co.odisea.framework.commands.models.CustomCommand;
import co.odisea.framework.commands.models.annotations.Description;
import co.odisea.framework.commands.models.annotations.Path;
import co.odisea.framework.commands.models.events.CommandEvent;
import co.odisea.utils.PlayerUtils;
import co.odisea.utils.Tasks;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiCommandCommand extends CustomCommand {

    public MultiCommandCommand(@NotNull CommandEvent event) {
        super(event);
    }

    @Path("<commands...>")
    @Description("Run multiple commands at once separated by ' ;; '")
    void run(String input) {
        runMultiCommand(input.split(" ;; "));
    }

    public static void run(CommandSender sender, List<String> commands) {
        if (commands.size() == 0)
            return;

        AtomicInteger wait = new AtomicInteger(0);
        commands.forEach(command -> {
            if (command.toLowerCase().matches("^wait \\d+$"))
                wait.getAndAdd(Integer.parseInt(command.toLowerCase().replace("wait ", "")));
            else
                Tasks.wait(wait.getAndAdd(3), () -> PlayerUtils.runCommand(sender, command));
        });
    }

}
