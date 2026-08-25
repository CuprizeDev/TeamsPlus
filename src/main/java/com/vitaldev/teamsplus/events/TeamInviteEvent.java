package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamInviteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player inviter;
    private final org.bukkit.OfflinePlayer target;
    private final Team team;

    public TeamInviteEvent(Player inviter, org.bukkit.OfflinePlayer target, Team team) {
        this.inviter = inviter;
        this.target = target;
        this.team = team;
    }

    public Player getInviter() { return inviter; }
    public org.bukkit.OfflinePlayer getTarget() { return target; }
    public Team getTeam() { return team; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
