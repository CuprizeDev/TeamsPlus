package com.vitaldev.teamsplus.commands.relation;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AllyCmd extends SubCmd {
    
    private final TeamsPlus plugin;

    public AllyCmd(TeamsPlus teamsPlus) {
        super("ally", "teamsplus.base.ally", "teamplus.admin.ally", List.of("a"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-sub-args")
                    .replace("{ARGS}", "<team>")
                    .replace("{COMMAND}", "/team ally"));
            return;
        }

        String teamName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Team team = Team.getTeam(player);

        for (UUID teamUUID : Team.getTeamList()) {

            Team targetTeam = Team.getTeam(teamUUID);

            if (targetTeam.getTeamName().equalsIgnoreCase(teamName)) {

                if (team == targetTeam) {
                    player.sendMessage(langHandler.getMessage("messages.ally.cannot-ally-self"));
                    return;
                }

                if (team.getAllyRequests().contains(teamUUID)) {
                    player.sendMessage(langHandler.getMessage("messages.ally.request-already-sent"));
                    return;
                }

                if (team.getAllies().contains(teamUUID)) {
                    player.sendMessage(langHandler.getMessage("messages.ally.already-allied")
                            .replace("TEAM", targetTeam.getTeamName()));
                    return;
                }

                if (team.getAllyCount() >= plugin.getConfig().getInt("teams.allies.maximum")) {
                    player.sendMessage(langHandler.getMessage("messages.ally.maximum-allies"));
                    return;
                }

                if (targetTeam.getAllyRequests().contains(team.getTeamUUID())) {
                    team.sendMessage(langHandler.getMessage("messages.ally.allied")
                            .replace("{TEAM}", targetTeam.getTeamName()));
                    targetTeam.sendMessage(langHandler.getMessage("messages.ally.allied")
                            .replace("{TEAM}", team.getTeamName()));

                    targetTeam.addAlly(team.getTeamUUID());
                    team.addAlly(teamUUID);
                } else {
                    team.sendMessage(langHandler.getMessage("messages.ally.sent")
                            .replace("{TEAM}", targetTeam.getTeamName()));
                    targetTeam.sendMessage(langHandler.getMessage("messages.ally.received")
                            .replace("{TEAM}", team.getTeamName()));

                    team.addAllyRequest(teamUUID);
                }
                return;
            }
        }

        player.sendMessage(langHandler.getMessage("messages.invalid-team"));

    }
}
