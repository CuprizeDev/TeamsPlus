package com.vitaldev.teamsplus.features.permissions;

import org.bukkit.Material;

public enum PermissableAction {

    BUILD("Build", "Place blocks inside team claims", Material.BRICKS),
    DESTROY("Destroy", "Break blocks inside team claims", Material.TNT),
    CONTAINER("Containers", "Open chests, barrels & shulkers", Material.CHEST),
    DOOR("Doors", "Use doors, trapdoors & fence gates", Material.OAK_DOOR),
    BUTTON("Buttons", "Use buttons inside team claims", Material.OAK_BUTTON),
    LEVER("Levers", "Use levers inside team claims", Material.LEVER),
    PLATE("Pressure Plates", "Trigger pressure plates", Material.OAK_PRESSURE_PLATE),
    REDSTONE("Redstone", "Interact with redstone devices", Material.REDSTONE),
    INVITE("Invite", "Invite players to the team", Material.PAPER),
    KICK("Kick", "Kick members from the team", Material.IRON_BOOTS),
    PROMOTE("Promote", "Promote members to a higher rank", Material.DIAMOND),
    TERRITORY("Territory", "Claim or unclaim chunks", Material.GRASS_BLOCK),
    HOME("Home", "Teleport to the team home", Material.RED_BED),
    ARTIFACTS("Artifacts", "Change team artifacts", Material.TOTEM_OF_UNDYING),
    SET_HOME("Set Home", "Set the team home location", Material.COMPASS),
    ALLY("Allies", "Manage team alliances", Material.BLUE_BANNER),
    SETTINGS("Settings", "Change team settings and name", Material.COMPARATOR),
    CHAT("Chat", "Toggle team chat mode", Material.WRITABLE_BOOK),
    UPGRADES("Upgrades", "Purchase team upgrades", Material.GOLD_INGOT);

    private final String displayName;
    private final String description;
    private final Material displayMaterial;

    PermissableAction(String displayName, String description, Material displayMaterial) {
        this.displayName = displayName;
        this.description = description;
        this.displayMaterial = displayMaterial;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    @Override
    public String toString() {
        return displayName;
    }

}

