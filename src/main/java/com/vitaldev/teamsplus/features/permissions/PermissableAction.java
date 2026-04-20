package com.vitaldev.teamsplus.features.permissions;

public enum PermissableAction {

    BUILD("BUILD"),
    DESTROY("DESTROY"),
    ITEM("ITEM_FRAME"),
    CONTAINER("CHESTS"),
    BUTTON("BUTTONS"),
    DOOR("DOORS"),
    LEVER("LEVERS"),
    PLATE("PRESSURE_PLATES"),
    FROST_WALK("FROST_WALK"),
    INVITE("INVITE"),
    KICK("KICK"),
    BAN("BAN"),
    PROMOTE("PROMOTE"),
    TERRITORY("TERRITORY"),
    HOME("HOME"),
    REDSTONE("REDSTONE"),
    SET_HOME("SET_HOME"),
    SET_WARP("SET_WARP"),
    WARP("WARP");

    private final String displayName;

    PermissableAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
