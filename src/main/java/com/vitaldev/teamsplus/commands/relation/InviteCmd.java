package com.vitaldev.teamsplus.commands.relation;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

public class InviteCmd extends SubCmd {

    private final TeamsPlus plugin;

    public InviteCmd(TeamsPlus teamsPlus) {
        super("invite", "teamsplus.base.invite", "", Arrays.asList("add", "inv"));
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
                    .replace("{COMMAND}", "/team invite"));
            return;
        }

        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))) {

            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);

            if (!offlineTarget.hasPlayedBefore()) {
                sender.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            Team team = Team.getTeam(player);

            if (team.isMember(offlineTarget.getUniqueId())) {
                player.sendMessage(langHandler.getMessage("messages.invite.in-team")
                        .replace("{PLAYER}", offlineTarget.getName()));
                return;
            }

            if (team.isInvited(offlineTarget.getUniqueId())) {
                player.sendMessage(langHandler.getMessage("messages.invite.already-sent")
                        .replace("{PLAYER}", offlineTarget.getName()));
                return;
            }

            team.addInvite(Objects.requireNonNull(offlineTarget.getPlayer()));
            player.sendMessage(langHandler.getMessage("messages.invite.sent")
                    .replace("{PLAYER}", offlineTarget.getName()));

        } else {

            if (target == null) {
                player.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            if (target == player) {
                player.sendMessage(langHandler.getMessage("messages.invite.cannot-invite-self"));
                return;
            }

            Team team = Team.getTeam(player);

            if (team.isMember(target)) {
                player.sendMessage(langHandler.getMessage("messages.invite.in-team")
                        .replace("{PLAYER}", target.getName()));
                return;
            }

            if (team.isInvited(target)) {
                player.sendMessage(langHandler.getMessage("messages.invite.already-sent")
                        .replace("{PLAYER}", target.getName()));
                return;
            }

            team.addInvite(target);
            player.sendMessage(langHandler.getMessage("messages.invite.sent")
                    .replace("{PLAYER}", target.getName()));
            target.sendMessage(langHandler.getMessage("messages.invite.received")
                    .replace("{TEAM}", team.getTeamName()));

        }
    }
}
