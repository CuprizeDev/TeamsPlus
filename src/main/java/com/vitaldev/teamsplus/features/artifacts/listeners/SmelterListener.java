package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SmelterListener implements Listener {

    private static final Map<Material, Material> SMELT_RESULTS = new HashMap<>();

    static {
        SMELT_RESULTS.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_RESULTS.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_RESULTS.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_RESULTS.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_RESULTS.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_RESULTS.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_RESULTS.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        SMELT_RESULTS.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
        SMELT_RESULTS.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_RESULTS.put(Material.RAW_IRON_BLOCK, Material.IRON_INGOT);
        SMELT_RESULTS.put(Material.RAW_GOLD_BLOCK, Material.GOLD_INGOT);
        SMELT_RESULTS.put(Material.RAW_COPPER_BLOCK, Material.COPPER_INGOT);
    }

    public SmelterListener(TeamsPlus plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Team team = ArtifactListenerUtil.getPlayerTeamInOwnClaim(player, ArtifactType.SMELTER);
        if (team == null) {
            return;
        }

        Block block = event.getBlock();
        Material result = SMELT_RESULTS.get(block.getType());
        if (result == null) {
            return;
        }

        event.setDropItems(false);
        for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand(), player)) {
            Material smeltResult = SMELT_RESULTS.get(drop.getType());
            if (smeltResult != null) {
                Location dropLoc = block.getLocation();
                dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(smeltResult, drop.getAmount()));
            } else {
                Location dropLoc = block.getLocation();
                dropLoc.getWorld().dropItemNaturally(dropLoc, drop);
            }
        }
    }
}
