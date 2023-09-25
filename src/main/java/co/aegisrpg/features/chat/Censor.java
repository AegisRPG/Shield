package co.aegisrpg.features.chat;

import co.aegisrpg.*;
import co.aegisrpg.features.chat.events.ChatEvent;
import co.aegisrpg.features.chat.events.PublicChatEvent;
import co.aegisrpg.framework.commands.*;
import co.aegisrpg.models.chat.ChatterService;
import co.aegisrpg.models.chat.PublicChannel;
import co.aegisrpg.features.chat.Chat.Broadcast;
import co.aegisrpg.models.nerd.*;
import co.aegisrpg.features.chat.Chat.StaticChannel;
import co.aegisrpg.utils.*;
import co.aegisrpg.utils.StringUtils;
import lombok.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import org.apache.commons.lang.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;

import java.util.*;
import java.util.regex.*;

import static co.aegisrpg.api.common.utils.StringUtils.countUpperCase;
import static co.aegisrpg.api.common.utils.UUIDUtils.UUID0;

public class Censor {
    @Getter
    private static final String PREFIX = StringUtils.getPrefix("Censor");
    @Getter
    private static final List<CensorItem> censorItems = new ArrayList<>();

    static {
        reloadConfig();
    }

    public static void reloadConfig() {
        censorItems.clear();
        ConfigurationSection config = IOUtils.getAegisConfig("censor.yml").getConfigurationSection("censor");
        if (config != null) {
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (!config.isConfigurationSection(key) || section == null)
                    Shield.warn(PREFIX + "Configuration section " + key + " misconfigured");
                else
                    censorItems.add(CensorItem.builder()
                            .name(key)
                            .find(section.getStringList("find"))
                            .replace(section.getStringList("replace"))
                            .bad(section.getBoolean("bad"))
                            .whole(section.getBoolean("whole"))
                            .cancel(section.getBoolean("cancel"))
                            .punish(section.getBoolean("punish"))
                            .punishReason(section.getString("punishReason"))
                            .build());
            }
        }
    }

    @Data
    @Builder
    private static class CensorItem {
        private final String name;
        private final List<String> find;
        private final List<String> replace;
        private boolean whole;
        private boolean bad;
        private boolean cancel;
        private boolean punish;
        private String punishReason;

        String getCensored() {
            return RandomUtils.randomElement(replace);
        }
    }

    public static void process(ChatEvent event) {
        deUnicode(event);
        lowercase(event);
        censor(event);
        dotCommand(event);
        dots(event);
        Emotes.process(event);
    }

    public static boolean isCensored(Player player, String input) {
        ChatEvent chatEvent = new PublicChatEvent(new ChatterService().get(player), StaticChannel.GLOBAL.getChannel(), input, input, new HashSet<>());
        Censor.censor(chatEvent);
        return chatEvent.isBad() || chatEvent.isCancelled();
    }

    public static void censor(ChatEvent event) {
        String message = event.getMessage();

        if (event.getChannel() instanceof PublicChannel publicChannel)
            if (!publicChannel.isCensor())
                return;

        int bad = 0;
        for (CensorItem censorItem : censorItems) {
            for (String regex : censorItem.getFind()) {
                boolean matches;
                if (censorItem.isWhole())
                    matches = (" " + message + " ").matches("(?i).* " + regex + " .*");
                else
                    matches = message.matches("(?i).*" + regex + ".*");

                if (matches) {
                    if (!censorItem.getReplace().isEmpty())
                        message = message.replaceAll("(?i)" + regex, censorItem.getCensored());

                    event.setFiltered(true);

                    if (censorItem.isBad())
                        ++bad;

                    if (censorItem.isCancel())
                        event.setCancelled(true);

                    if (censorItem.isPunish()) {
                        if (!event.getChatter().getRank().isStaff()) {
                            event.setCancelled(true);

                            PunishmentType type;

                            if (Rank.of(event.getChatter()) == Rank.GUEST)
                                type = PunishmentType.BAN;
                            else if (event instanceof PublicChatEvent && !StaticChannel.LOCAL.getChannel().equals(event.getChannel()))
                                type = PunishmentType.BAN;
                            else
                                type = PunishmentType.WARN;

                            Punishments.of(event.getChatter()).add(Punishment.ofType(type)
                                    .punisher(UUID0)
                                    .input(censorItem.getPunishReason() + ": " + event.getOriginalMessage())
                                    .now(true));
                        }
                    }
                }
            }
        }

        if (bad >= 1) {
            event.setBad(true);
            String channelNick = "";
            if (event.getChannel() instanceof PublicChannel publicChannel)
                channelNick = "[" + publicChannel.getNickname() + "] ";
            else if (event.getChannel() instanceof PrivateChannel privateChannel)
                channelNick = "[" + String.join(", ", privateChannel.getOthersNames(event.getChatter())) + "] ";

            IOUtils.fileAppend("swears", channelNick + event.getChatter().getName() + ": " + event.getOriginalMessage());
            if (bad >= 3) {
                event.getChatter().sendMessage("&cPlease watch your language!");
                Broadcast.staff().prefix("Censor").message("&c" + event.getChatter().getName() + " cursed too much: " + event.getMessage()).send();
                event.setCancelled(true);
            }
        }

        event.setMessage(message);
    }

    private static void lowercase(ChatEvent event) {
        if (event.getChannel() instanceof PublicChannel && !((PublicChannel) event.getChannel()).isCensor())
            return;

        String message = event.getMessage();
        String characters = message.replaceAll(" ", "");
        if (characters.length() == 0)
            return;
        int upper = countUpperCase(message);
        int pct = (int) ((double) upper / characters.length() * 100);

        if (upper > 7 && pct > 40)
            message = message.toLowerCase();

        event.setMessage(message);
    }

    public static void dots(ChatEvent event) {
        if (event.getMessage().toLowerCase().matches(".*(\\(|<|\\{|\\[)dot(]|}|>|\\)).*")) {
            Broadcast.staff().prefix("Censor").message("Prevented a possible advertisement attempt by " + event.getOrigin() + ": " + event.getMessage()).send();
            event.setCancelled(true);
        }
    }

    public static void dotCommand(ChatEvent event) {
        String message = event.getMessage();
        Pattern pattern = Pattern.compile("(\\ |^)." + Commands.getPattern());
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String group = matcher.group();
            String replace = group.replace("./", "/");
            message = message.replace(group, replace);
        }
        event.setMessage(message);
    }

    // Supports:
    // https://en.wikipedia.org/wiki/Halfwidth_and_Fullwidth_Forms_(Unicode_block)
    // ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ
    // ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ
    //
    // https://en.wikipedia.org/wiki/List_of_Unicode_characters#Enclosed_Alphanumerics
    // ⒜⒝⒞⒟⒠⒡⒢⒣⒤⒥⒦⒧⒨⒩⒪⒫⒬⒭⒮⒯⒰⒱⒲⒳⒴⒵
    // ⒶⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏ
    // ⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ
    //
    // The decimal value for the first character of the real character sets
    // https://en.wikipedia.org/wiki/List_of_Unicode_characters#Basic_Latin

    private static final int LOWER = 97;
    private static final int UPPER = 65;

    public static void deUnicode(ChatEvent event) {
        String message = event.getMessage();
        List<String> characters = Arrays.asList(message.split(""));

        int index = 0;
        for (String character : new ArrayList<>(characters)) {
            if (character.length() == 0) continue;

            String unicode = toUnicode(character);

            characters.set(index, unicode);
            if (unicode.startsWith("\\uff")) {
                unicode = unicode.replaceAll("\\\\uff", "");
                int i = Integer.parseInt(unicode, 16);
                if (i >= 33 && i <= 58)
                    i += UPPER - 33;
                else if (i >= 65 && i <= 90)
                    i += LOWER - 65;

                String fixed = Integer.toString(i, 16);
                if (fixed.length() < 2)
                    fixed = "0" + fixed;
                if (!fixed.equals(unicode))
                    characters.set(index, "\\u00" + Integer.toString(i, 16));
            } else if (unicode.startsWith("\\u24")) {
                unicode = unicode.replaceAll("\\\\u24", "");
                int i = Integer.parseInt(unicode, 16);
                if (i >= 156 && i <= 181)
                    i -= 156 - LOWER;
                else if (i >= 182 && i <= 207)
                    i -= 182 - UPPER;
                else if (i >= 208 && i <= 233)
                    i -= 208 - LOWER;

                String fixed = Integer.toString(i, 16);
                if (fixed.length() < 2)
                    fixed = "0" + fixed;
                if (!fixed.equals(unicode))
                    characters.set(index, "\\u00" + Integer.toString(i, 16));
            }

            characters.set(index, StringEscapeUtils.unescapeJava(characters.get(index)));

            ++index;
        }

        event.setMessage(String.join("", characters));
    }

    public static String toUnicode(String input) {
        return toUnicode(input.toCharArray());
    }

    public static String toUnicode(char[] input) {
        StringBuilder output = new StringBuilder();
        for (char c : input)
            output.append("\\u").append(Integer.toHexString(c | 0x10000).substring(1));
        return output.toString();
    }
}
