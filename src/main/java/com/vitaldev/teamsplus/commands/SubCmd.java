package com.vitaldev.teamsplus.commands;


import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCmd {

    private final String name;
    private final String permission;
    private final String adminPermission;
    private final List<String> aliases;

    public SubCmd(String name, String permission, String adminPermission, List<String> aliases) {
        this.name = name;
        this.permission = permission;
        this.adminPermission = adminPermission;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getAdminPermission() {
        return adminPermission;
    }

    public List<String> getAliases() {
        return aliases;
    }

    // Check if the argument matches the command name or any alias
    public boolean matches(String input) {
        return name.equalsIgnoreCase(input) || aliases.contains(input.toLowerCase());
    }

    // Abstract method for executing subcommands
    public abstract void execute(CommandSender sender, String[] args);
}
