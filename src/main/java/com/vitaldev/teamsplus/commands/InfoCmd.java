package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class InfoCmd extends SubCmd {

    private final TeamsPlus plugin;
    public InfoCmd(TeamsPlus teamsPlus) {
        super("info", "teamsplus.base.info", "teamsplus.admin.info", Arrays.asList("info", "show", "who"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        Player player = (Player) sender;
        String teamName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        for (UUID teamUUID : Team.getTeamList()) {
            Team team = Team.getTeam(teamUUID);

            if (team.getTeamName().equalsIgnoreCase(teamName) || teamName.isEmpty()) {

                List<UUID> coLeaders = new ArrayList<>();
                List<UUID> officers = new ArrayList<>();
                List<UUID> members = new ArrayList<>();

                for (UUID playerUUID : team.getMembers()) {
                    if (team.getPlayerRank(playerUUID) == PlayerRank.CO_LEADER) {
                        coLeaders.add(playerUUID);
                    }
                    if (team.getPlayerRank(playerUUID) == PlayerRank.OFFICER) {
                        officers.add(playerUUID);
                    }
                    if (team.getPlayerRank(playerUUID) == PlayerRank.MEMBER) {
                        members.add(playerUUID);
                    }
                }

                String coLeadersNames = coLeaders.stream()
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.joining(", "));

                String officersNames = officers.stream()
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.joining(", "));

                String membersNames = members.stream()
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.joining(", "));

                String allyNames = team.getAllies().stream()
                        .map(Team::getTeam)
                        .filter(Objects::nonNull)
                        .map(Team::getTeamName)
                        .collect(Collectors.joining(", "));

                for (String line : langHandler.getColoredList("messages.info.team-message")) {
                    player.sendMessage(line
                            .replace("{TOTAL}", String.valueOf(team.getMemberCount()))
                            .replace("{ONLINE}", String.valueOf(team.getOnlineMemberCount()))
                            .replace("{TEAM}", team.getTeamName())
                            .replace("{LEADER}", team.getLeader().getName())
                            .replace("{CLAIMS}", String.valueOf(team.getClaimsCount()))
                            .replace("{ALLIES}", allyNames.isEmpty() ? "None" : allyNames)
                            .replace("{CO-LEADERS}", coLeadersNames.isEmpty() ? "None" : coLeadersNames)
                            .replace("{OFFICERS}", officersNames.isEmpty() ? "None" : officersNames)
                            .replace("{POWER}", String.valueOf(team.getPower()))
                            .replace("{MEMBERS}", membersNames.isEmpty() ? "None" : membersNames));
                }
                return;
            }
        }

        player.sendMessage(langHandler.getMessage("messages.invalid-team"));
    }
}

