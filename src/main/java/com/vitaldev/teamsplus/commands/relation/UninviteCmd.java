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
import java.util.Map;

public class UninviteCmd extends SubCmd {

    private final TeamsPlus plugin;

    public UninviteCmd(TeamsPlus teamsPlus) {
        super("uninvite", "teamsplus.base.uninvite", "", Arrays.asList("uninv", "remove"));
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
                    .replace("{COMMAND}", "/team uninvite"));
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

            if (!team.isInvited(offlineTarget.getUniqueId())) {
                player.sendMessage(langHandler.getMessage("messages.invite.is-not-invited")
                        .replace("{PLAYER}", offlineTarget.getName()));
                return;
            }

            com.vitaldev.teamsplus.events.TeamUninviteEvent uninviteEvent = new com.vitaldev.teamsplus.events.TeamUninviteEvent(player, offlineTarget, team);
            org.bukkit.Bukkit.getPluginManager().callEvent(uninviteEvent);
            if (uninviteEvent.isCancelled()) return;

            team.removeInvite(offlineTarget.getUniqueId());
            this.plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.INVITE_REMOVE, player, player.getLocation(), Map.of("target", offlineTarget.getName()));
            player.sendMessage(langHandler.getMessage("messages.invite.removed")
                    .replace("{PLAYER}", offlineTarget.getName()));

        } else {

            if (target == null) {
                player.sendMessage(langHandler.getMessage("messages.offline-player"));
                return;
            }

            Team team = Team.getTeam(player);

            if (!team.isInvited(target)) {
                player.sendMessage(langHandler.getMessage("messages.invite.is-not-invited")
                        .replace("{PLAYER}", target.getName()));
                return;
            }

            com.vitaldev.teamsplus.events.TeamUninviteEvent uninviteEvent = new com.vitaldev.teamsplus.events.TeamUninviteEvent(player, target, team);
            org.bukkit.Bukkit.getPluginManager().callEvent(uninviteEvent);
            if (uninviteEvent.isCancelled()) return;

            team.removeInvite(target);
            this.plugin.getLogManager().logEvent(team, com.vitaldev.teamsplus.features.logs.LogType.INVITE_REMOVE, player, player.getLocation(), Map.of("target", target.getName()));
            player.sendMessage(langHandler.getMessage("messages.invite.removed")
                    .replace("{PLAYER}", target.getName()));
        }
    }
}