package com.vitaldev.teamsplus.util;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ChestUtil {


    public TeamsPlus plugin;
    public ChestUtil(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    private static Set<NamespacedKey> recipeKeys = new HashSet<>(); // Store the keys


    public ItemStack createClaimChest() {

        ItemStack itemStack = ItemHandler.buildItem(
                Material.getMaterial(this.plugin.getConfig().getString("teams.chest.item.material")),
                ChatUtil.color(this.plugin.getConfig().getString("teams.chest.item.name")),
                1,
                this.plugin.getConfig().getStringList("teams.chest.item.lore").stream().map(ChatUtil::color).collect(Collectors.toList()),
                this.plugin.getConfig().getBoolean("teams.chest.item.glow"), true);

        NBTHandler nbtUtil = new NBTHandler(this.plugin);

        nbtUtil.addBoolean(itemStack, nbtUtil.getKey() + "claim_chest", true);

        return itemStack;
    }

    public void removeCustomRecipes() {

        for (NamespacedKey key : recipeKeys) {
            Bukkit.removeRecipe(key);
        }

        recipeKeys.clear();
    }

    public void addCustomRecipe() {
        Material[] doorTypes = {
                Material.ACACIA_DOOR,
                Material.BIRCH_DOOR,
                Material.DARK_OAK_DOOR,
                Material.SPRUCE_DOOR,
                Material.OAK_DOOR,
                Material.JUNGLE_DOOR,
                Material.CRIMSON_DOOR,
                Material.WARPED_DOOR,
                Material.IRON_DOOR,
                Material.MANGROVE_DOOR,
                Material.BAMBOO_DOOR,
                Material.CHERRY_DOOR
        };

        for (Material doorType : doorTypes) {
            NamespacedKey key = new NamespacedKey(plugin, doorType.toString().toLowerCase() + "_claim_chest");

            ShapedRecipe recipe = new ShapedRecipe(key, createClaimChest());
            recipe.shape("D  ", "C  ", "   ");
            recipe.setIngredient('D', doorType);
            recipe.setIngredient('C', Material.CHEST);

            Bukkit.addRecipe(recipe);

            recipeKeys.add(key);
        }
    }
}
