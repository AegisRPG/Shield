package co.odisea.utils;


import co.odisea.Shield;
import me.lucko.helper.Services;
import me.lucko.helper.scoreboard.PacketScoreboardProvider;
import me.lucko.helper.scoreboard.Scoreboard;
import me.lucko.helper.scoreboard.ScoreboardObjective;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.ScoreboardManager;

import java.lang.reflect.Field;
import java.util.*;

import static co.odisea.api.common.utils.StringUtils.left;
import static co.odisea.utils.StringUtils.colorize;

public class AegisScoreboard {
    private static final ScoreboardManager manager = Shield.getInstance().getServer().getScoreboardManager();
    private static final Scoreboard scoreboard = Services.load(PacketScoreboardProvider.class).getScoreboard();
    private final ScoreboardObjective objective;
    private final Map<String, Integer> lines = new HashMap<>();

    public AegisScoreboard(String title) {
        this(title, title, Collections.emptyList());
    }

    public AegisScoreboard(String title, Player player) {
        this(title, title, Collections.singletonList(player));
    }

    public AegisScoreboard(String title, Collection<? extends Player> players) {
        this(title, title, players);
    }

    public AegisScoreboard(String id, String title) {
        this(id, title, Collections.emptyList());
    }

    public AegisScoreboard(String id, String title, Player player) {
        this(id, title, Collections.singletonList(player));
    }

    public AegisScoreboard(String id, String title, Collection<? extends Player> players) {
        try { scoreboard.removeObjective(left(id, 16)); } catch (Exception ignore) {}
        objective = scoreboard.createObjective(left(id, 16), colorize(title), DisplaySlot.SIDEBAR, false);
        for (Player player : players)
            subscribe(player);
    }

    public void delete() {
        clear();
        scoreboard.removeObjective(objective.getId());
    }

    private void clear() {
        objective.clearScores();
    }

    public boolean isSubscribed(Player player) {
        try {
            Field subscribed = objective.getClass().getDeclaredField("subscribed");
            subscribed.setAccessible(true);
            Set<Player> players = (Set<Player>) subscribed.get(objective);
            return players.contains(player);
        } catch (Exception ex) {
            ex.printStackTrace();

            // Cant read subscribers, assume they are subscribed
            return true;
        }
    }

    public void subscribe(Player... players) {
        subscribe(Arrays.asList(players));
    }

    public void subscribe(Collection<? extends Player> players) {
        for (Player player : players)
            objective.subscribe(player);
    }

    public void unsubscribe(Player... players) {
        unsubscribe(Arrays.asList(players));
    }

    public void unsubscribe(Collection<? extends Player> players) {
        for (Player player : players) {
            if (!isSubscribed(player)) return;
            objective.unsubscribe(player);
            player.setScoreboard(manager.getMainScoreboard());
        }
    }

    public String getTitle() {
        return objective.getDisplayName();
    }

    public void setTitle(String title) {
        objective.setDisplayName(title);
    }

    public Map<String, Integer> getLines() {
        return objective.getScores();
    }

    public void setLine(String id, int score) {
        objective.setScore(id, score);
    }

    public void setLines(Map<String, Integer> lines) {
        clear();
        objective.applyScores(lines);
    }

    public void setLines(List<String> lines) {
        clear();
        objective.applyLines(lines);
    }

    public void removeLine(String id) {
        objective.removeScore(id);
    }

    public void removeLines(String... lines) {
        removeLines(Arrays.asList(lines));
    }

    public void removeLines(List<String> lines) {
        lines.forEach(this::removeLine);
    }

}
