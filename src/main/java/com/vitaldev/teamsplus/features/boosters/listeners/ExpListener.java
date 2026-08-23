package com.vitaldev.teamsplus.features.boosters.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.boosters.ActiveBooster;
import com.vitaldev.teamsplus.features.boosters.BoosterType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class ExpListener implements Listener {

    private final TeamsPlus plugin;

    public ExpListener(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (!Team.hasTeam(player)) return;

        Team team = Team.getTeam(player);
        if (team == null) return;

        if (team.hasActiveBooster(BoosterType.EXP)) {
            ActiveBooster booster = team.getActiveBooster(BoosterType.EXP);
            if (booster != null && !booster.isExpired()) {
                int original = event.getAmount();
                event.setAmount((int) (original * booster.getMultiplier()));
            }
        }
    }
}
