package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamDisbandEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player disbander;
    private final Team team;

    public TeamDisbandEvent(Player disbander, Team team) {
        this.disbander = disbander;
        this.team = team;
    }

    public Player getDisbander() { return disbander; }
    public Team getTeam() { return team; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
