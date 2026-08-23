package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.commands.CommandBuilder;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class VersionCmd extends SubCmd {

    private final TeamsPlus plugin;

    public VersionCmd(TeamsPlus plugin) {
        super("version", "teamplus.base.version", "", Arrays.asList("v", "ver"));
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("TeamsPlus version " + plugin.getDescription().getVersion());
    }
}
