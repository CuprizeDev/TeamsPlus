package com.vitaldev.teamsplus.commands.relation;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LeaveCmd extends SubCmd {

    private final TeamsPlus plugin;

    public LeaveCmd(TeamsPlus teamsPlus) {
        super("leave", "teamsplus.base.leave", "teamplus.admin.leave", Arrays.asList("leave", "quit"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (args.length < 1) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-sub-args")
                    .replace("{ARGS}", "")
                    .replace("{COMMAND}", "/team leave"));
            return;
        }

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.leave.not-in-team"));
            return;
        }

        Team team = Team.getTeam(player);

        if (team.getPlayerRank(player) == PlayerRank.LEADER) {
            player.sendMessage(langHandler.getMessage("messages.leave.cant-leave"));
            return;
        }

        com.vitaldev.teamsplus.events.TeamLeaveEvent leaveEvent = new com.vitaldev.teamsplus.events.TeamLeaveEvent(player, team);
        org.bukkit.Bukkit.getPluginManager().callEvent(leaveEvent);
        if (leaveEvent.isCancelled()) return;

        team.removeMember(player);
        this.plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.LEAVE, player, player.getLocation(), null);
        player.sendMessage(langHandler.getMessage("messages.leave.leave").replace("{TEAM}", team.getTeamName()));

    }
}
