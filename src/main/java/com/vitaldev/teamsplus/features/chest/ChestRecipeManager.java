package com.vitaldev.teamsplus.features.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChestRecipeManager {

    private final TeamsPlus plugin;
    private final ChestItemBuilder itemBuilder;

    private final Set<NamespacedKey> recipeKeys = new HashSet<>();

    public ChestRecipeManager(TeamsPlus plugin, ChestItemBuilder itemBuilder) {
        this.plugin = plugin;
        this.itemBuilder = itemBuilder;
    }

    public void registerRecipes() {

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

        ItemStack claimChest = itemBuilder.buildClaimChest();

        for (Material door : doorTypes) {
            NamespacedKey key = new NamespacedKey(plugin, door.toString().toLowerCase() + "_claim_chest");

            ShapedRecipe recipe = new ShapedRecipe(key, claimChest);
            recipe.shape("D  ", "C  ", "   ");

            recipe.setIngredient('D', door);
            recipe.setIngredient('C', Material.CHEST);

            Bukkit.addRecipe(recipe);
            recipeKeys.add(key);
        }
    }

    public void removeRecipes() {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();

        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();

            if (recipe instanceof Keyed keyed) {
                NamespacedKey key = keyed.getKey();

                if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                    iterator.remove();
                }
            }
        }

        recipeKeys.clear();
    }
}