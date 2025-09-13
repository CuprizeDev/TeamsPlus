package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCmd extends SubCmd {

    private final TeamsPlus plugin;

    public HelpCmd(TeamsPlus teamsPlus) {
        super("help", "teamsplus.base.help", "", List.of("?"));
        this.plugin = teamsPlus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String line: this.plugin.getLangFile().getColoredList("messages.help.help-message")) {
            sender.sendMessage(ChatUtil.color(line));
        }
    }
}
