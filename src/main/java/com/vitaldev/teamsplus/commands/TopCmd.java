package com.vitaldev.teamsplus.commands; // Adjusted package to match the requested style

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.leaderboard.ChestTopInventory;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class TopCmd extends SubCmd {

    private final TeamsPlus plugin;

    public TopCmd(TeamsPlus teamsPlus) {
        // Passing arguments to parent: name, playerPermission, adminPermission, aliases
        super("top", "teamsplus.top", "teamsplus.admin.top", Collections.singletonList("leaderboard"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ConfigHandler langHandler = this.plugin.getLangFile();

        if (!plugin.isFeatureEnabled("leaderboard")) {
            sender.sendMessage(com.vitaldev.vitallibs.util.ChatUtil.color("&cThe leaderboard feature is currently disabled."));
            return;
        }

        if (!(sender instanceof Player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission(getPermission())) {
            player.sendMessage(langHandler.getMessage("messages.no-permission"));
            return;
        }

        new ChestTopInventory(this.plugin, player).openInventory();
    }
}