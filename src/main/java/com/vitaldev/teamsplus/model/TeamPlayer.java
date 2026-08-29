package com.vitaldev.teamsplus.model;

import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.vitaldev.teamsplus.features.stats.StatType;

public class TeamPlayer {

    private static final Map<UUID, TeamPlayer> players = new HashMap<>();

    private Team team;
    private final Player player;
    private boolean friendlyFireEnabled;
    private boolean discordRaidAlertsEnabled = true;
    private final Map<StatType, Long> stats = new EnumMap<>(StatType.class);

    private TeamPlayer(Player player) {
        this.player = player;
    }

    public static TeamPlayer get(Player player) {
        return players.computeIfAbsent(
                player.getUniqueId(),
                uuid -> {
                    TeamPlayer tp = new TeamPlayer(player);
                    com.vitaldev.teamsplus.model.PlayerData.load(tp);
                    return tp;
                }
        );
    }

    public Player getPlayer() {
        return player;
    }

    public Team getTeam() {
        return Team.getTeam(player);
    }

    public boolean isClaim() {
        return true;
    }

    public Relation getRelationTo(Team randomTeam) {
        return getTeam() != null ? getTeam().getRelationTo(randomTeam) : Relation.WILDERNESS;
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFireEnabled;
    }

    public void setFriendlyFireEnabled(boolean friendlyFireEnabled) {
        this.friendlyFireEnabled = friendlyFireEnabled;
    }

    public boolean isDiscordRaidAlertsEnabled() {
        return discordRaidAlertsEnabled;
    }

    public void setDiscordRaidAlertsEnabled(boolean enabled) {
        this.discordRaidAlertsEnabled = enabled;
    }

    public void toggleFriendlyFire() {
        this.friendlyFireEnabled = !this.friendlyFireEnabled;
    }

    public long getStat(StatType type) {
        return stats.getOrDefault(type, 0L);
    }

    public void setStat(StatType type, long value) {
        stats.put(type, value);
    }

    public void addStat(StatType type, long value) {
        stats.put(type, getStat(type) + value);
    }

    public Map<StatType, Long> getStats() {
        return stats;
    }
    
    public static void remove(UUID uuid) {
        TeamPlayer tp = players.remove(uuid);
        if (tp != null) {
            com.vitaldev.teamsplus.model.PlayerData.save(tp);
        }
    }
    
    public static void saveAll() {
        for (TeamPlayer tp : players.values()) {
            com.vitaldev.teamsplus.model.PlayerData.save(tp);
        }
    }
}
