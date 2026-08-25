package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player creator;
    private final String teamName;
    private final Team team;

    public TeamCreateEvent(Player creator, String teamName, Team team) {
        this.creator = creator;
        this.teamName = teamName;
        this.team = team;
    }

    public Player getCreator() { return creator; }
    public String getTeamName() { return teamName; }
    public Team getTeam() { return team; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
