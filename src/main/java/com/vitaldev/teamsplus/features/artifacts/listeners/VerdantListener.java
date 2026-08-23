package com.vitaldev.teamsplus.features.artifacts.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.teamsplus.model.Team;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class VerdantListener implements Listener {

    private static final Set<Material> CROPS = EnumSet.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.COCOA,
            Material.MELON,
            Material.PUMPKIN,
            Material.SWEET_BERRY_BUSH,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP
    );

    public VerdantListener(TeamsPlus plugin) {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Team team = ArtifactListenerUtil.getPlayerTeamInOwnClaim(player, ArtifactType.VERDANT);
        if (team == null) {
            return;
        }

        Block block = event.getBlock();
        if (!CROPS.contains(block.getType())) {
            return;
        }

        event.setDropItems(false);
        Location dropLoc = block.getLocation();
        for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand(), player)) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, drop.clone());
            dropLoc.getWorld().dropItemNaturally(dropLoc, drop.clone());
        }
    }
}
