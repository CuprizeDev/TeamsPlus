package com.vitaldev.teamsplus.commands;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.artifacts.ArtifactDefinition;
import com.vitaldev.teamsplus.features.artifacts.ArtifactManager;
import com.vitaldev.teamsplus.features.artifacts.ArtifactType;
import com.vitaldev.vitallibs.commands.CommandBuilder;
import com.vitaldev.vitallibs.commands.CommandUtil;
import com.vitaldev.vitallibs.config.ConfigHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArtifactCmd extends CommandBuilder {

    private final TeamsPlus plugin;

    public ArtifactCmd(TeamsPlus plugin, String command, String[] aliases, String description, String basePerm) {
        super(command, aliases, description, basePerm, "");
        this.plugin = plugin;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {

        ConfigHandler langHandler = plugin.getLangFile();

        if (!CommandUtil.validateArgsLength(sender,
                args,
                2,
                langHandler.getString("messages.invalid-args")
                        .replace("{ARGS}", "give <player>")
                        .replace("{COMMAND}", "sword"))) {
            return;
        }

        if (args[0].equalsIgnoreCase("give")) {

            if (!CommandUtil.checkPermission(sender,
                    "tools.admin.*",
                    langHandler.getString("messages.no-permission"))) {
                return;
            }

            Player target = CommandUtil.getValidPlayerArgument(sender,
                    args,
                    1,
                    langHandler.getString("messages.offline-player"));

            ArtifactType type = ArtifactType.fromString(args[2]);

            if (type == null) {
                return;
            }

            ArtifactManager artifactManager = plugin.getArtifactManager();

            if (!artifactManager.isRegistered(type)) {
                sender.sendMessage(langHandler.getMessage("messages.artifacts.invalid-artifact"));
                return;
            }

            ArtifactDefinition def = artifactManager.get(type);
            sender.sendMessage(langHandler.getMessage("messages.artifacts.add-sender")
                    .replace("{PLAYER}", target.getName())
                    .replace("{TIER-COLOR}", def.getTier().getColor())
                    .replace("{TYPE}", def.getDisplayName()));

            target.sendMessage(langHandler.getMessage("messages.artifacts.add-recipient")
                    .replace("{TIER-COLOR}", def.getTier().getColor())
                    .replace("{TYPE}", def.getDisplayName()));

            target.getInventory().addItem(plugin.getArtifactItemBuilder().buildArtifact(type));
        }
    }
}
