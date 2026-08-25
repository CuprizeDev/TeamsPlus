package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.features.logs.LogEntry;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamLogEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Team team;
    private final LogEntry logEntry;

    public TeamLogEvent(Team team, LogEntry logEntry) {
        this.team = team;
        this.logEntry = logEntry;
    }

    public Team getTeam() { return team; }
    public LogEntry getLogEntry() { return logEntry; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
