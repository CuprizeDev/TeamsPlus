package com.vitaldev.teamsplus.features.artifacts;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.items.NBTHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.stream.Collectors;

public class ArtifactItemBuilder {

    private final TeamsPlus plugin;

    public ArtifactItemBuilder(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    public ItemStack buildArtifact(ArtifactType type) {
        ArtifactDefinition def = plugin.getArtifactManager().get(type);

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
        nbt.addString(item, nbt.getKey() + "artifact_type", def.getType().name().toLowerCase());
        nbt.addBoolean(item, nbt.getKey() + "artifact", true);
        return item;
    }

}
