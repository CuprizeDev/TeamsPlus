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
        if (team == null) return;
        
        if (team.isTeamChatEnabled(player)) {
            ConfigHandler langHandler = this.plugin.getLangFile();
            String messageTemplate = langHandler.getMessage("messages.chat.team-chat")
                    .replace("{TEAM}", team.getTeamName())
                    .replace("{PLAYER}", player.getName())
                    .replace("{MESSAGE}", event.getMessage());
            Set<UUID> memberSnapshot = team.getMembers();

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (UUID memberUUID : memberSnapshot) {
                    Player onlinePlayer = Bukkit.getPlayer(memberUUID);
                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                        onlinePlayer.sendMessage(messageTemplate);
                    }
                }
            });
        } else if (team.isAllyChatEnabled(player)) {
            String messageTemplate = com.vitaldev.vitallibs.util.ChatUtil.color("&d&lALLY CHAT &8| &7[&d" + team.getTeamName() + "&7] &f" + player.getName() + " &8\u00bb &d" + event.getMessage());
            Set<UUID> memberSnapshot = team.getMembers();
            Set<UUID> allySnapshot = team.getAllies();
            
            event.setCancelled(true);

            Bukkit.getScheduler().runTask(plugin, () -> {
                // Send to own team
                for (UUID memberUUID : memberSnapshot) {
                    Player onlinePlayer = Bukkit.getPlayer(memberUUID);
                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                        onlinePlayer.sendMessage(messageTemplate);
                    }
                }
                
                // Send to all allies
                for (UUID allyUUID : allySnapshot) {
                    Team allyTeam = Team.getTeam(allyUUID);
                    if (allyTeam != null) {
                        for (UUID allyMemberUUID : allyTeam.getMembers()) {
                            Player onlineAlly = Bukkit.getPlayer(allyMemberUUID);
                            if (onlineAlly != null && onlineAlly.isOnline()) {
                                onlineAlly.sendMessage(messageTemplate);
                            }
                        }
                    }
                }
            });
        }
    }
}
