package com.vitaldev.teamsplus.commands.ranks;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.teams.PlayerRank;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class DemoteCmd extends SubCmd {

    private final TeamsPlus plugin;

    public DemoteCmd(TeamsPlus teamsPlus) {
        super("demote", "teamsplus.base.demote", "teamplus.admin.demote", List.of("demo"));
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
                    .replace("{COMMAND}", "/team demote"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        Team team = Team.getTeam(player);

        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {

            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);
            PlayerRank playerRank = team.getPlayerRank(offlineTarget.getUniqueId());

            if (!offlineTarget.hasPlayedBefore()) {
                sender.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            if (!team.isMember(Objects.requireNonNull(offlineTarget.getPlayer()))) {
                player.sendMessage(langHandler.getMessage("messages.not-in-team")
                        .replace("{PLAYER}", Objects.requireNonNull(offlineTarget.getName())));
                return;
            }

            player.sendMessage(langHandler.getMessage("messages.demote.sent")
                    .replace("{RANK}", playerRank.getName(playerRank))
                    .replace("{PLAYER}", target.getName()));
            target.sendMessage(langHandler.getMessage("messages.demote.received")
                    .replace("{RANK}", playerRank.getName(playerRank)));
            team.demote(offlineTarget.getUniqueId());

        } else {

            PlayerRank playerRank = team.getPlayerRank(target);

            if (target == null) {
                sender.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            if (target == player) {
                player.sendMessage(langHandler.getMessage("messages.demote.cant-demote-self"));
                return;
            }

            if (!Team.hasTeam(target)) {
                player.sendMessage(langHandler.getMessage("messages.not-in-team"));
                return;
            }

            if (team.getPlayerRank(player.getUniqueId()).getValue() <= playerRank.getValue()) {
                player.sendMessage(langHandler.getMessage("messages.demote.cant-demote"));
                return;
            }

            if (team.getPlayerRank(target.getUniqueId()) == PlayerRank.MEMBER) {
                player.sendMessage(langHandler.getMessage("messages.demote.cant-demote-member"));
                return;
            }

            player.sendMessage(langHandler.getMessage("messages.demote.sent")
                    .replace("{RANK}", playerRank.getNameFromValue(playerRank.value-1))
                    .replace("{PLAYER}", target.getName()));
            target.sendMessage(langHandler.getMessage("messages.demote.received")
                    .replace("{RANK}", playerRank.getNameFromValue(playerRank.value-1)));
            team.demote(target);
        }
    }
}
