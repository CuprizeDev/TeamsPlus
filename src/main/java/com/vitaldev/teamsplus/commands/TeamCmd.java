package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TeamCmd extends BukkitCommand {

    private final Map<String, SubCmd> subCommands = new HashMap<>();
    private final String adminPerm;
    TeamsPlus plugin;

    public TeamCmd(TeamsPlus plugin, String command, String[] aliases, String description, String basePerm, String adminPerm) {
        super(command);
        this.setAliases(Arrays.asList(aliases));
        this.setDescription(description);
        this.setPermission(basePerm);
        this.adminPerm = adminPerm;
        this.plugin = plugin;
        registerCommand();
    }

    private void registerCommand() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer());
            map.register(getName(), this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void registerSubCommand(SubCmd subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {

        ConfigHandler langHandler = this.plugin.getLangFile();

        if (args.length == 0) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-args"));
            return true;
        }

        String subCommandInput = args[0].toLowerCase();

        // Find the subcommand by name or alias
        SubCmd matchedSubCommand = subCommands.values().stream()
                .filter(subCommand -> subCommand.matches(subCommandInput))
                .findFirst()
                .orElse(null);

        if (matchedSubCommand == null) {
            sender.sendMessage(langHandler.getMessage("messages.invalid-args"));
            return true;
        }

        if (matchedSubCommand.getPermission() != null && !sender.hasPermission(matchedSubCommand.getPermission())) {
            sender.sendMessage(langHandler.getMessage("messages.no-permission"));
            return true;
        }

        matchedSubCommand.execute(sender, args);
        return true;
    }
}
