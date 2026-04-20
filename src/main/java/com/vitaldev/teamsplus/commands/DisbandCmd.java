package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DisbandCmd extends SubCmd {

    private final TeamsPlus plugin;
    public DisbandCmd(TeamsPlus teamsPlus) {
        super("disband", "teamsplus.base.disband", "teamsplus.admin.disband", Arrays.asList("dissolve", "disband"));
        this.plugin = teamsPlus;}

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        Team team = Team.getTeam(player);

        if (team.getPlayerRank(player) != PlayerRank.LEADER) {
            player.sendMessage(langHandler.getMessage("messages.no-permission"));
            return;
        }

        Team.disbandTeam(team, plugin);
        player.sendMessage(langHandler.getMessage("messages.disband"));

    }
}
