package com.vitaldev.teamsplus.dependencies;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.teams.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    public final TeamsPlus plugin;

    public PlaceholderAPIHook(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "teamsplus";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Cuprize";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String parameters) {

        Player player = offlinePlayer.getPlayer();

        if (parameters.contains("name")) {
            if (Team.hasTeam(player)) {
            return String.valueOf(Team.getTeam(player).getTeamName());
            } else {
                return "None";
            }
        }

        if (parameters.contains("online")) {

            if (Team.hasTeam(player)) {
                return String.valueOf(Team.getTeam(player).getOnlineMemberCount());
            } else {
                return "0";
            }

        }

        if (parameters.contains("members")) {

            if (Team.hasTeam(player)) {
                return String.valueOf(Team.getTeam(player).getMemberCount());
            } else {
                return "0";
            }

        }

        return null;
    }
}

