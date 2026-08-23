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

import java.util.Set;
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

        // getTeam and isTeamChatEnabled use read-only maps — safe to call here
        if (team == null || !team.isTeamChatEnabled(player)) return;

        // Capture all state while still on the async thread (immutable primitives / copies)
        ConfigHandler langHandler = this.plugin.getLangFile();
        String messageTemplate = langHandler.getMessage("messages.chat.team-chat")
                .replace("{TEAM}", team.getTeamName())
                .replace("{PLAYER}", player.getName())
                .replace("{MESSAGE}", event.getMessage());
        Set<UUID> memberSnapshot = team.getMembers(); // unmodifiable view, safe to read

        // Cancel the public chat event — safe to do from async thread
        event.setCancelled(true);

        // Dispatch to main thread for all Bukkit API calls
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID memberUUID : memberSnapshot) {
                Player onlinePlayer = Bukkit.getPlayer(memberUUID);
                if (onlinePlayer != null && onlinePlayer.isOnline()) {
                    onlinePlayer.sendMessage(messageTemplate);
                }
            }
        });
    }
}
