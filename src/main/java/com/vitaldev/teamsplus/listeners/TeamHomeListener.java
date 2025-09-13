package com.vitaldev.teamsplus.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.util.ChatUtil;
import com.vitaldev.vitallibs.util.TaskUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeamHomeListener implements Listener {

    public TeamsPlus plugin;
    private final TaskUtil taskUtil;


    public TeamHomeListener(TeamsPlus plugin) {
        this.plugin = plugin;
        this.taskUtil = new TaskUtil(plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (hasPlayerMoved(from, to)) {
            if (taskUtil.isTaskRunning(player, "teleport")) {
                taskUtil.cancelTask(player, "teleport");
                player.sendMessage(ChatUtil.color(this.plugin.getLangFile().getMessage("messages.home.failed")));
            }
        }
    }

    private boolean hasPlayerMoved(Location from, Location to) {
        // Check horizontal movement (ignores vertical changes and small float inaccuracies)
        double deltaX = from.getX() - to.getX();
        double deltaZ = from.getZ() - to.getZ();

        // We only care about horizontal distance moved
        return Math.abs(deltaX) > 0.05 || Math.abs(deltaZ) > 0.05;
    }
}
