package com.vitaldev.teamsplus.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class TeamChatListener implements Listener {

    public TeamsPlus plugin;

    public TeamChatListener(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        Team team = Team.getTeam(player);
        ConfigHandler langHandler = this.plugin.getLangFile();

        if (team.isTeamChatEnabled(player)) {
            for (UUID playerUUID : team.getMembers()) {
                Player onlinePlayer = Bukkit.getPlayer(playerUUID);
                if (Bukkit.getOnlinePlayers().contains(onlinePlayer)) {
                    onlinePlayer.sendMessage(langHandler.getMessage("messages.chat.team-chat")
                            .replace("{TEAM}", team.getTeamName())
                            .replace("{PLAYER}", event.getPlayer().getName())
                            .replace("{MESSAGE}", event.getMessage()));
                }
            }

            event.setCancelled(true);
        }
    }
}
