package com.vitaldev.teamsplus.features.boosters;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.stream.Collectors;

public class BoosterItemBuilder {

    private final TeamsPlus plugin;

    public BoosterItemBuilder(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    public ItemStack buildBooster(String id) {
        BoosterDefinition def = plugin.getBoosterManager().getBooster(id);
        if (def == null) return null;

        ItemStack item = ItemHandler.buildItem(
                def.getMaterial(),
                def.getDisplayName().replace("{TIER-COLOR}", def.getTier().getColor()),
                1,
                def.getLore().stream().map(ChatUtil::color).collect(Collectors.toList()),
                def.isGlow(),
                true
        );

        NBTHandler nbt = new NBTHandler(plugin);
        nbt.addString(item, nbt.getKey() + "uuid", UUID.randomUUID().toString());
        nbt.addString(item, nbt.getKey() + "booster_id", id);
        nbt.addBoolean(item, nbt.getKey() + "booster", true);
        return item;
    }
}
