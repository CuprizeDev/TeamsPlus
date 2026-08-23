package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.commands.CommandBuilder;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ReloadCmd extends SubCmd {

    private final TeamsPlus plugin;

    public ReloadCmd(TeamsPlus plugin) {
        super("reload", "teamsplus.admin.reload", "teamsplus.admin.reload", Arrays.asList("restart", "reset"));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ConfigHandler langHandler = plugin.getLangFile();

        if (!sender.hasPermission(plugin.getAdminPermission())) {
            sender.sendMessage(langHandler.getMessage("messages.no-permission"));
            return;
        }

        plugin.reloadConfiguration();
        plugin.getArtifactManager().reload();
        plugin.getBoosterManager().reload();
        plugin.getLogManager().reload();
        plugin.getShieldManager().reload();
        plugin.getLeaderboardService().loadConfig();

        sender.sendMessage(langHandler.getMessage("messages.reload.success"));
    }
}
