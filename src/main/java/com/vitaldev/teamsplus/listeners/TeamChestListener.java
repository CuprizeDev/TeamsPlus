package com.vitaldev.teamsplus.listeners;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.teams.Team;
import com.vitaldev.teamsplus.util.TeamData;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TeamChestListener implements Listener {

    public TeamsPlus plugin;

    public TeamChestListener(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();


    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {

    }

    @EventHandler
    public void onChestExplode(BlockExplodeEvent event) {

    }

    @EventHandler
    public void onChestPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType() != Material.CHEST) {
            return;
        }

        Player player = event.getPlayer();
        NBTHandler nbtUtil = new NBTHandler(this.plugin);
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ConfigHandler langHandler = this.plugin.getLangFile();
        TeamData teamData = new TeamData(this.plugin);

        if (offHand.getType() == Material.CHEST
                && Boolean.TRUE.equals(nbtUtil.getBoolean(offHand, nbtUtil.getKey() + "claim_chest"))) {
            event.setCancelled(true);
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR
                && Boolean.TRUE.equals(nbtUtil.getBoolean(mainHand, nbtUtil.getKey() + "claim_chest"))) {

            if (Team.hasTeam(player)) {
                player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.chest.has-team")));
                event.setCancelled(true);
                return;
            }

            if (Team.containsClaimChest(event.getBlockPlaced().getLocation().getChunk())) {
                player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.chest.already-claimed")));
                event.setCancelled(true);
                return;
            }
            player.sendMessage(ChatUtil.color(langHandler.getMessage("messages.chest.placed")));
            Team team = new Team(plugin, player.getName() + "'s Team", player.getUniqueId(), UUID.randomUUID(), event.getBlockPlaced().getLocation());
            teamData.saveTeam(team);
            Team.addUUID(team.getTeamUUID(), team);
        }
    }
}
