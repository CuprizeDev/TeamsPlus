package com.vitaldev.teamsplus.features.boosters.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.boosters.ActiveBooster;
import com.vitaldev.teamsplus.features.boosters.BoosterDefinition;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BoosterListener implements Listener {

    private final TeamsPlus plugin;

    public BoosterListener(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBoosterUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) return;

        String boosterId = plugin.getBoosterManager().getBoosterIdFromItem(item);
        if (boosterId == null) return;

        BoosterDefinition def = plugin.getBoosterManager().getBooster(boosterId);
        if (def == null) return;

        event.setCancelled(true);

        if (!Team.hasTeam(player)) {
            player.sendMessage(plugin.getLangFile().getMessage("messages.no-team"));
            return;
        }

        Team team = Team.getTeam(player);

        if (team.hasActiveBooster(def.getType())) {
            player.sendMessage(plugin.getLangFile().getMessage("messages.boosters.booster-already-active")
                    .replace("{TYPE}", def.getType().getDisplayName()));
            return;
        }

        // Consume 1 item
        item.setAmount(item.getAmount() - 1);

        // Activate Booster
        ActiveBooster activeBooster = new ActiveBooster(
                boosterId,
                def.getType(),
                def.getMultiplier(),
                player.getUniqueId(),
                System.currentTimeMillis(),
                def.getDurationSeconds() * 1000L
        );
        
        team.addActiveBooster(activeBooster);

        team.sendMessage(plugin.getLangFile().getMessage("messages.boosters.booster-activated")
                .replace("{PLAYER}", player.getName())
                .replace("{BOOSTER}", def.getDisplayName()));
    }
}
