package co.aegisrpg.framework.commands.models.events;

import co.aegisrpg.Shield;
import co.aegisrpg.api.common.exceptions.ShieldException;
import co.aegisrpg.framework.commands.Commands;
import co.aegisrpg.framework.commands.models.annotations.Description;
import co.aegisrpg.framework.commands.models.annotations.Path;
import co.aegisrpg.framework.exceptions.AegisException;
import co.aegisrpg.utils.JsonBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static co.aegisrpg.utils.Extensions.isNullOrEmpty;

@Data
@RequiredArgsConstructor
public class CommandRunEvent extends CommandEvent {
    private Method method;
    private String usage;

    public CommandRunEvent(CommandSender sender, CustomCommand command, String aliasUsed, List<String> args, List<String> originalArgs) {
        super(sender, command, aliasUsed, args, originalArgs, false);
    }

    public void setUsage(Method method) {
        this.method = method;
        Path path = method.getAnnotation(Path.class);
        if (path != null) {
            this.usage = path.value();
            Description desc = method.getAnnotation(Description.class);
            if (desc != null)
                this.usage += " &7- " + desc.value();
        }
    }

    public String getUsageMessage() {
        return "Correct usage: /" + aliasUsed + " " + usage;
    }

    public void handleException(Throwable ex) {
        if (Shield.isDebug()) {
            Shield.debug("Handling command framework exception for " + getSender().getName());
            ex.printStackTrace();
        }

        String PREFIX = command.getPrefix();
        if (isNullOrEmpty(PREFIX))
            PREFIX = Commands.getPrefix(command);

        if (ex instanceof MissingArgumentException) {
            reply(PREFIX + "&c" + getUsageMessage());
            return;
        }

        if (ex.getCause() != null && ex.getCause() instanceof ShieldException shieldException) {
            reply(new JsonBuilder(PREFIX + "&c").next(shieldException.getJson()));
            return;
        }

        if (ex instanceof ShieldException shieldException) {
            reply(new JsonBuilder(PREFIX + "&c").next(shieldException.getJson()));
            return;
        }

        if (ex.getCause() != null && ex.getCause() instanceof AegisException aegisException) {
            reply(PREFIX + "&c" + aegisException.getMessage());
            return;
        }

        if (ex instanceof ShieldException) {
            reply(PREFIX + "&c" + ex.getMessage());
            return;
        }

        if (ex instanceof IllegalArgumentException && ex.getMessage() != null && ex.getMessage().contains("type mismatch")) {
            reply(PREFIX + "&c" + getUsageMessage());
            return;
        }

        reply("&cAn internal error occurred while attempting to execute this command");

        if (ex.getCause() != null && ex instanceof InvocationTargetException)
            ex.getCause().printStackTrace();
        else
            ex.printStackTrace();
    }

}

