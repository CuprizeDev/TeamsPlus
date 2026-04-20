package com.vitaldev.teamsplus.features.chest;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class ChestItemBuilder {

    private final TeamsPlus plugin;

    public ChestItemBuilder(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    public ItemStack buildClaimChest() {
        ConfigHandler configHandler = this.plugin.getConfigFile();

        ItemStack item = ItemHandler.buildItem(
                Material.getMaterial(configHandler.getString("teams.chest.item.material")),
                ChatUtil.color(configHandler.getString("teams.chest.item.name")),
                1,
                configHandler.getStringList("teams.chest.item.lore")
                        .stream().map(ChatUtil::color).collect(Collectors.toList()),
                configHandler.getBoolean("teams.chest.item.glow"),
                true
        );

        NBTHandler nbt = new NBTHandler(plugin);
        nbt.addBoolean(item, nbt.getKey() + "claim_chest", true);

        return item;
    }
}
