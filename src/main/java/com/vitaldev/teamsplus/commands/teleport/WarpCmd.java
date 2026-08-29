package com.vitaldev.teamsplus.commands.teleport;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.commands.SubCmd;
import com.vitaldev.teamsplus.commands.BypassCmd;
import com.vitaldev.teamsplus.features.permissions.PermissableAction;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import com.vitaldev.vitallibs.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class WarpCmd extends SubCmd {

    private final TeamsPlus plugin;

    public WarpCmd(TeamsPlus teamsPlus) {
        super("warp", "teamsplus.base.warp", "teamsplus.admin.warp", Collections.emptyList());
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!plugin.isFeatureEnabled("warps")) {
            sender.sendMessage(langHandler.getMessage("messages.warps.disabled"));
            return;
        }
        TaskUtil taskUtil = new TaskUtil(plugin);

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }
        
        if (!Team.hasTeam(player)) {
            player.sendMessage(langHandler.getMessage("messages.need-team"));
            return;
        }

        if (args.length < 2) {
            new com.vitaldev.teamsplus.features.teleport.ChestWarpInventory(plugin, player).openInventory();
            return;
        }

        Team team = Team.getTeam(player);
        if (!team.canDo(player, PermissableAction.HOME) && !BypassCmd.isBypassing(player)) {
            player.sendMessage(langHandler.getMessage("messages.permissions.denied"));
            return;
        }
        
        String warpName = args[1];
        Location warpLoc = team.getWarp(warpName);
        if (warpLoc == null) {
            player.sendMessage(langHandler.getMessage("messages.warps.warp-not-found"));
            return;
        }

        int cooldown = plugin.getConfigFile().getInt("teams.cooldowns.warp");
        player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.home.commence").replace("{TIME}",String.valueOf(cooldown))));

        taskUtil.scheduleTask(player, "teleport", () -> {
            com.vitaldev.teamsplus.events.TeamWarpEvent warpEvent = new com.vitaldev.teamsplus.events.TeamWarpEvent(player, team, warpLoc);
            org.bukkit.Bukkit.getPluginManager().callEvent(warpEvent);
            if (warpEvent.isCancelled()) return;

            player.teleport(warpEvent.getLocation());
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.home.success")));
        }, cooldown);
    }
}
