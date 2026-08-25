package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.features.upgrades.UpgradeType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamUpgradeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Team team;
    private final UpgradeType upgradeType;
    private final int oldLevel;
    private final int newLevel;

    public TeamUpgradeEvent(Player player, Team team, UpgradeType upgradeType, int oldLevel, int newLevel) {
        this.player = player;
        this.team = team;
        this.upgradeType = upgradeType;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() { return player; }
    public Team getTeam() { return team; }
    public UpgradeType getUpgradeType() { return upgradeType; }
    public int getOldLevel() { return oldLevel; }
    public int getNewLevel() { return newLevel; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
