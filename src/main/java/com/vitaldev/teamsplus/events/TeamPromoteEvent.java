package com.vitaldev.teamsplus.events;

import com.vitaldev.teamsplus.features.permissions.PlayerRank;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeamPromoteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player promoter;
    private final org.bukkit.OfflinePlayer target;
    private final Team team;
    private final PlayerRank oldRank;
    private final PlayerRank newRank;

    public TeamPromoteEvent(Player promoter, org.bukkit.OfflinePlayer target, Team team, PlayerRank oldRank, PlayerRank newRank) {
        this.promoter = promoter;
        this.target = target;
        this.team = team;
        this.oldRank = oldRank;
        this.newRank = newRank;
    }

    public Player getPromoter() { return promoter; }
    public org.bukkit.OfflinePlayer getTarget() { return target; }
    public Team getTeam() { return team; }
    public PlayerRank getOldRank() { return oldRank; }
    public PlayerRank getNewRank() { return newRank; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
