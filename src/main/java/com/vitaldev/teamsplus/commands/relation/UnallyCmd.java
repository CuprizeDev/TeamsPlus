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

public class UnallyCmd extends SubCmd {

    private final TeamsPlus plugin;

    public UnallyCmd(TeamsPlus teamsPlus) {
        super("unally", "teamsplus.base.unally", "teamplus.admin.unally", List.of("una"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute (CommandSender sender, String[]args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-sub-args")
                    .replace("{ARGS}", "<team>")
                    .replace("{COMMAND}", "/team unally"));
            return;
        }
        String teamName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Team team = Team.getTeam(player);
        for (UUID teamUUID : Team.getTeamList()) {
            Team targetTeam = Team.getTeam(teamUUID);
            if (targetTeam.getTeamName().equals(teamName)) {

                if (!team.getAllyRequests().contains(teamUUID) && !team.getAllies().contains(teamUUID)) {
                    player.sendMessage(langHandler.getMessage("messages.ally.not-allied")
                            .replace("{TEAM}", teamName));
                    return;
                }

                if (team.getAllyRequests().contains(teamUUID)) {
                    team.removeAllyRequest(teamUUID);
                    player.sendMessage(langHandler.getMessage("messages.ally.request-removed"));
                    return;
                }

                player.sendMessage(langHandler.getMessage("messages.ally.removed"));
                targetTeam.removeAlly(teamUUID);
                team.removeAlly(teamUUID);
                return;
            }
        }
    }
}
