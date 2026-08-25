package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamNameEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Team team;
    private final String oldName;
    private final String newName;

    public TeamNameEvent(Player player, Team team, String oldName, String newName) {
        this.player = player;
        this.team = team;
        this.oldName = oldName;
        this.newName = newName;
    }

    public Player getPlayer() { return player; }
    public Team getTeam() { return team; }
    public String getOldName() { return oldName; }
    public String getNewName() { return newName; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
