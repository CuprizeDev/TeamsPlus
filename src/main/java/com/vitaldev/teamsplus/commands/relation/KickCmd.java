package com.vitaldev.teamsplus.commands.relation;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

public class KickCmd extends SubCmd {

    private final TeamsPlus plugin;

    public KickCmd(TeamsPlus teamsPlus) {
        super("kick", "teamsplus.base.kick", "", Arrays.asList("k", "remove", "ban"));
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
                    .replace("{COMMAND}", "/team kick"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        Team team = Team.getTeam(player);

        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {

            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);

            if (!offlineTarget.hasPlayedBefore()) {
                sender.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            if (!team.isMember(Objects.requireNonNull(offlineTarget.getPlayer()))) {
                player.sendMessage(langHandler.getMessage("messages.not-in-team")
                        .replace("{PLAYER}", offlineTarget.getName()));
                return;
            }

            player.sendMessage(langHandler.getMessage("messages.kick.sent")
                    .replace("{PLAYER}", Objects.requireNonNull(offlineTarget.getName())));
            team.removeMember(Objects.requireNonNull(offlineTarget.getPlayer()));

        } else {

            if (target == null) {
                sender.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            if (target == player) {
                player.sendMessage(langHandler.getMessage("messages.kick.cannot-kick-self"));
                return;
            }

            if (!Team.hasTeam(target)) {
                player.sendMessage(langHandler.getMessage("messages.not-in-team"));
                return;
            }

            player.sendMessage(langHandler.getMessage("messages.kick.sent")
                    .replace("{PLAYER}", target.getName()));
            target.sendMessage(langHandler.getMessage("messages.kick.received")
                    .replace("{TEAM}", team.getTeamName()));
            team.removeMember(target);

        }
    }
}
