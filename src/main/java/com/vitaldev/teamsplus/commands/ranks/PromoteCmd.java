package com.vitaldev.teamsplus.commands.ranks;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class PromoteCmd extends SubCmd {

    private final TeamsPlus plugin;

    public PromoteCmd(TeamsPlus teamsPlus) {
        super("promote", "teamsplus.base.promote", "teamplus.admin.promote", List.of("promo"));
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
                    .replace("{COMMAND}", "/team promote"));
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

            if (!team.isMember(offlineTarget.getUniqueId())) {
                player.sendMessage(langHandler.getMessage("messages.not-in-team")
                        .replace("{PLAYER}", Objects.requireNonNull(offlineTarget.getName())));
                return;
            }
            team.promote(offlineTarget.getPlayer());

            player.sendMessage(langHandler.getMessage("messages.promote.sent")
                    .replace("{RANK}", playerRank.getDisplayName())
                    .replace("{PLAYER}", target.getName()));
            target.sendMessage(langHandler.getMessage("messages.promote.received")
                    .replace("{RANK}", playerRank.getDisplayName()));
        } else {

            PlayerRank playerRank = team.getPlayerRank(target.getUniqueId());

            if (target == null) {
                sender.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            if (target == player) {
                player.sendMessage(langHandler.getMessage("messages.promote.cant-promote-self"));
                return;
            }

            if (!Team.hasTeam(target)) {
                player.sendMessage(langHandler.getMessage("messages.not-in-team"));
                return;
            }

            if (team.getPlayerRank(player.getUniqueId()).getValue() <= playerRank.getValue()) {
                player.sendMessage(langHandler.getMessage("messages.promote.cant-promote"));
                return;
            }

            if (team.getPlayerRank(target.getUniqueId()) == PlayerRank.CO_LEADER) {
                player.sendMessage(langHandler.getMessage("messages.promote.cant-promote-leader"));
                return;
            }

            team.promote(target);
            player.sendMessage(langHandler.getMessage("messages.promote.sent")
                    .replace("{RANK}", playerRank.getDisplayName())
                    .replace("{PLAYER}", target.getName()));
            target.sendMessage(langHandler.getMessage("messages.promote.received")
                    .replace("{RANK}", playerRank.getDisplayName()));
        }
    }
}
