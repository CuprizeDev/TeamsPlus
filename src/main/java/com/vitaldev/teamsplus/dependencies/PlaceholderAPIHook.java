package com.vitaldev.teamsplus.dependencies;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.model.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// PlaceholderAPI expansion for TeamsPlus.
// <p>
// Supported placeholders:
// <ul>
// <li>{@code %teamsplus_name%}              — Team name or "None"</li>
// <li>{@code %teamsplus_has_team%}           — "true" / "false"</li>
// <li>{@code %teamsplus_rank%}               — Player's rank display name</li>
// <li>{@code %teamsplus_members%}            — Total member count</li>
// <li>{@code %teamsplus_online%}             — Online member count</li>
// <li>{@code %teamsplus_power%}              — Team power</li>
// <li>{@code %teamsplus_claims%}             — Number of claimed chunks</li>
// <li>{@code %teamsplus_allies%}             — Number of allies</li>
// <li>{@code %teamsplus_leader%}             — Leader's name</li>
// <li>{@code %teamsplus_shield_charge%}      — Shield charge (formatted)</li>
// <li>{@code %teamsplus_shield_status%}      — Active / Deploying / Cooldown / Inactive</li>
// <li>{@code %teamsplus_leaderboard_rank%}   — Leaderboard position (#1, #2…)</li>
// </ul>
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final TeamsPlus plugin;

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
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Keep the expansion loaded across /papi reload
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        // Resolve an online Player handle (may be null if offline)
        Player player = offlinePlayer.getPlayer();

        // --- Placeholders that don't need an online player ---

        if (params.equalsIgnoreCase("has_team")) {
            return player != null && Team.hasTeam(player) ? "true" : "false";
        }

        // --- All remaining placeholders require an online player with a team ---

        if (player == null) return "";
        if (!Team.hasTeam(player)) {
            // Return safe defaults for players without a team
            return switch (params.toLowerCase()) {
                case "name"             -> "None";
                case "rank"             -> "None";
                case "members"          -> "0";
                case "online"           -> "0";
                case "power"            -> "0";
                case "claims"           -> "0";
                case "allies"           -> "0";
                case "leader"           -> "None";
                case "shield_charge"    -> "00:00";
                case "shield_status"    -> "None";
                case "leaderboard_rank" -> "Unranked";
                default                 -> null;
            };
        }

        Team team = Team.getTeam(player);
        if (team == null) return "";

        return switch (params.toLowerCase()) {
            case "name"    -> team.getTeamName();
            case "rank"    -> {
                PlayerRank rank = team.getPlayerRank(player);
                yield rank != null ? rank.getDisplayName() : "Member";
            }
            case "members" -> String.valueOf(team.getMemberCount());
            case "online"  -> String.valueOf(team.getOnlineMemberCount());
            case "power"   -> String.valueOf(team.getPower());
            case "claims"  -> String.valueOf(team.getClaims().size());
            case "allies"  -> String.valueOf(team.getAllyCount());
            case "leader"  -> {
                Player leader = Bukkit.getPlayer(team.getLeader().getUniqueId());
                yield leader != null ? leader.getName() : Bukkit.getOfflinePlayer(team.getLeader().getUniqueId()).getName();
            }

            // Shield placeholders
            case "shield_charge" -> {
                long charge = team.getShieldChargeSeconds();
                yield plugin.getShieldManager().formatTime(charge);
            }
            case "shield_status" -> {
                if (team.isShieldActive())    yield "Active";
                if (team.isShieldDeploying()) yield "Deploying";
                if (plugin.getShieldManager().getCooldownRemainingMillis(team) > 0) yield "Cooldown";
                yield "Inactive";
            }

            // Leaderboard
            case "leaderboard_rank" -> {
                int rank = plugin.getLeaderboardCache().getRank(team);
                yield rank > 0 ? "#" + rank : "Unranked";
            }

            default -> null; // Unknown placeholder — let PAPI handle it
        };
    }
}
