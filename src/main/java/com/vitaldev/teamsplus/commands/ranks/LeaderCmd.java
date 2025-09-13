package com.vitaldev.teamsplus.commands.ranks;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.teams.PlayerRank;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LeaderCmd extends SubCmd {

    private final TeamsPlus plugin;

    public LeaderCmd(TeamsPlus teamsPlus) {
        super("leader", "teamsplus.base.leader", "teamplus.admin.leader", List.of("leader", "leaders"));
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
                    .replace("{ARGS}", "<player>")
                    .replace("{COMMAND}", "/team leader"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        Team team = Team.getTeam(player);

        if (target == null) {
            sender.sendMessage(langHandler.getMessage("messages.offline-player"));
            return;
        }

        PlayerRank playerRank = team.getPlayerRank(target.getUniqueId());


        if (target == player) {
            player.sendMessage(langHandler.getMessage("messages.promote.cant-promote-self"));
            return;
        }

        if (!Team.hasTeam(target)) {
            player.sendMessage(langHandler.getMessage("messages.not-in-team"));
            return;
        }

        if (team.getPlayerRank(player) != PlayerRank.LEADER) {
            player.sendMessage(langHandler.getMessage("messages.no-permission"));
            return;
        }

        if (team.getPlayerRank(player.getUniqueId()).getValue() <= playerRank.getValue()) {
            player.sendMessage(langHandler.getMessage("messages.promote.cant-promote"));
            return;
        }

        player.sendMessage(langHandler.getMessage("messages.promote.sent")
                .replace("{RANK}", playerRank.getNameFromValue(playerRank.value + 1))
                .replace("{PLAYER}", target.getName()));
        target.sendMessage(langHandler.getMessage("messages.promote.received")
                .replace("{RANK}", playerRank.getNameFromValue(playerRank.value + 1)));
        team.setLeader(target);
        team.setPlayerRank(player, PlayerRank.CO_LEADER);


    }
}
