package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AerialListener implements Listener {

    private final Map<UUID, Boolean> playersInFly = new HashMap<>();
    private final Set<UUID> noFall = new HashSet<>();

    private final String FLY_BYPASS_PERM = "teamsplus.admin.fly";
    private final String ADMIN_PERM = "teamsplus.admin.fly";

    public final TeamsPlus plugin;
    private final ConfigHandler langHandler;

    public AerialListener(TeamsPlus plugin) {
        this.plugin = plugin;
        this.langHandler = plugin.getLangFile();
    }

    public void addNoFall(Player player) {
        noFall.add(player.getUniqueId());
    }

    public void removeNoFall(Player player) {
        noFall.remove(player.getUniqueId());
    }

    public boolean hasNoFall(Player player) {
        return noFall.contains(player.getUniqueId());
    }

    public boolean isFlyToggled(Player player) {
        return playersInFly.getOrDefault(player.getUniqueId(), false);
    }

    public void setFly(Player player, Boolean mode) {
        playersInFly.put(player.getUniqueId(), mode);
    }

    public void enableFly(Player player) {
        playersInFly.put(player.getUniqueId(), true);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void removeFly(Player player) {
        playersInFly.put(player.getUniqueId(), false);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    private void checkAndToggleFly(Player player, Team team) {
        if (!team.isInClaim(player)) {
            if (isFlyToggled(player)) {
                player.sendMessage(langHandler.getMessage("messages.artifacts.fly-disabled"));
                removeFly(player);
                addNoFall(player);
            }
        } else {
            if (!isFlyToggled(player)) {
                player.sendMessage(langHandler.getMessage("messages.artifacts.fly-enabled"));
                enableFly(player);
            }
        }
    }

    @EventHandler
    public void onFlyListener(PlayerMoveEvent event) {
        // Performance guard: Only check flight if player actually changed blocks
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (!hasRequirements(player)) {
            return;
        }

        checkAndToggleFly(player, Team.getTeam(player));
    }

    @EventHandler
    public void onTeleportListener(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!hasRequirements(player)) {
            return;
        }

        checkAndToggleFly(player, Team.getTeam(player));
    }

    @EventHandler
    public void onDeathListener(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        if (player == null || !hasRequirements(player)) {
            return;
        }

        checkAndToggleFly(player, Team.getTeam(player));
    }

    @EventHandler
    public void onDamageListener(EntityDamageByEntityEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!hasRequirements(player)) {
            return;
        }

        Team team = Team.getTeam(player);
        if (!team.isInClaim(player)) {
            if (isFlyToggled(player)) {
                player.sendMessage(langHandler.getMessage("messages.artifacts.fly-disabled"));
                removeFly(player);
                addNoFall(player);
            }
        } else {
            if (!isFlyToggled(player)) {
                // Kept scheduled delay match task exactly as original logic specified
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage(langHandler.getMessage("messages.artifacts.fly-enabled"));
                }, 20L);
                enableFly(player);
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL || event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!hasNoFall(player)) {
            return;
        }

        event.setCancelled(true);
        removeNoFall(player);
    }

    private boolean hasAdminPerms(Player player) {
        return player.hasPermission(ADMIN_PERM) || player.hasPermission(FLY_BYPASS_PERM);
    }

    public boolean hasRequirements(Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return false;
        }

        Team team = Team.getTeam(player);
        if (team == null || !team.hasArtifactApplied(ArtifactType.AERIAL)) {
            return false;
        }

        return true;
    }
}