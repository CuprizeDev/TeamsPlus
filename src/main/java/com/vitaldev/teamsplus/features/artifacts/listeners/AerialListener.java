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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AerialListener implements Listener {

    private final Map<UUID, Boolean> playersInFly = new HashMap<>();
    private final List<UUID> noFall = new ArrayList<>();

    private final String FLY_BYPASS_PERM = "teamsplus.admin.fly";
    private final String ADMIN_PERM = "teamsplus.admin.fly";

    public TeamsPlus plugin;
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

    public void setFly(Player player, Boolean mode) { playersInFly.put(player.getUniqueId(), mode); }

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

    @EventHandler
    public void onFlyListener(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Team team = Team.getTeam(player);

        if (!hasRequirements(player)) {
            return;
        }

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
    public void onTeleportListener(PlayerTeleportEvent event) {

        Player player = event.getPlayer();
        Team team = Team.getTeam(player);

        if (!hasRequirements(player)) {
            return;
        }

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
    public void onJoinListener(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Team team = Team.getTeam(player);

        if (!hasRequirements(player)) {
            return;
        }

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
    public void onDeathListener(PlayerDeathEvent event) {

        Player player = event.getEntity().getPlayer();

        if (player == null) {
            return;
        }

        Team team = Team.getTeam(player);

        if (!hasRequirements(player)) {
            return;
        }

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
    public void onDamageListener(EntityDamageByEntityEvent event) {

        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        if (event.getDamager().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();

        Team team = Team.getTeam(player);

        if (!hasRequirements(player)) {
            return;
        }

        if (!team.isInClaim(player)) {
            if (isFlyToggled(player)) {
                player.sendMessage(langHandler.getMessage("messages.artifacts.fly-disabled"));
                removeFly(player);
                addNoFall(player);
            }
        } else {
            if (!isFlyToggled(player)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            player.sendMessage(langHandler.getMessage("messages.artifacts.fly-enabled"));
                        }, 20L);
                enableFly(player);
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (event.getEntityType() != EntityType.PLAYER) {
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

        if (!Team.getTeam(player).hasArtifactApplied(ArtifactType.AERIAL)) {
            return false;
        }

        //if (hasAdminPerms(player)) {
        //    return false;
        //}

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return false;
        }

        return true;
    }

}
