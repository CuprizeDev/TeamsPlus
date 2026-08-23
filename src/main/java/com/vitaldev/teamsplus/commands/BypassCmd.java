package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.util.ConsoleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BypassCmd extends SubCmd {

    private static final Set<UUID> BYPASS_PLAYERS = ConcurrentHashMap.newKeySet();
    private final TeamsPlus plugin;

    public BypassCmd(TeamsPlus plugin) {
        super("bypass", "Toggle personal bypass mode", "teamsplus.admin.bypass", Arrays.asList("bp", "admin"));
        this.plugin = plugin;
    }

    public static boolean isBypassing(Player player) {
        return player != null && BYPASS_PLAYERS.contains(player.getUniqueId());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ConfigHandler langHandler = plugin.getLangFile();

        if (!(sender instanceof Player player)) {
            ConsoleUtil.sendMessage(langHandler.getMessage("messages.only-players"));
            return;
        }

        if (!sender.hasPermission(plugin.getAdminPermission())) {
            sender.sendMessage(langHandler.getMessage("messages.no-permission"));
            return;
        }

        boolean enabled;
        UUID uuid = player.getUniqueId();
        if (BYPASS_PLAYERS.contains(uuid)) {
            BYPASS_PLAYERS.remove(uuid);
            enabled = false;
        } else {
            BYPASS_PLAYERS.add(uuid);
            enabled = true;
        }

        sender.sendMessage(enabled
                ? langHandler.getMessage("messages.bypass.enabled")
                : langHandler.getMessage("messages.bypass.disabled"));
    }
}
