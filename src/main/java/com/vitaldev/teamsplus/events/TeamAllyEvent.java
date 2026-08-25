package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamAllyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final Team team;
    private final Team targetTeam;

    public TeamAllyEvent(Player player, Team team, Team targetTeam) {
        this.player = player;
        this.team = team;
        this.targetTeam = targetTeam;
    }

    public Player getPlayer() { return player; }
    public Team getTeam() { return team; }
    public Team getTargetTeam() { return targetTeam; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
