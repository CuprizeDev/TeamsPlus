package com.vitaldev.teamsplus.features.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import org.bukkit.inventory.ItemStack;

public class ChestManager {

    private final ChestItemBuilder itemBuilder;
    private final ChestRecipeManager recipeManager;

    public ChestManager(TeamsPlus plugin) {
        this.itemBuilder = new ChestItemBuilder(plugin);
        this.recipeManager = new ChestRecipeManager(plugin, itemBuilder);
    }

    public ItemStack getClaimChestItem() {
        return itemBuilder.buildClaimChest();
    }

    public void registerRecipes() {
        recipeManager.registerRecipes();
    }

    public void removeRecipes() {
        recipeManager.removeRecipes();
    }
}
