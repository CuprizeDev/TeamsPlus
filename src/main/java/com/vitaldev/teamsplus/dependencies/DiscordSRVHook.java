package com.vitaldev.teamsplus.dependencies;

import com.vitaldev.teamsplus.TeamsPlus;
import github.scarsz.discordsrv.DiscordSRV;

// Optional DiscordSRV integration.
// <p>
// This class references DiscordSRV classes directly, so it must only be
// constructed when DiscordSRV is confirmed present on the classpath.
// {@link DependencyManager} guards construction with a try/catch.
public class DiscordSRVHook {

    private final TeamsPlus plugin;

    public DiscordSRVHook(TeamsPlus plugin) {
        this.plugin = plugin;
    }

    // Sends an embed to a named DiscordSRV channel.
// @param channel      the DiscordSRV game-channel name (from DiscordSRV config)
// @param embedBuilder the embed to send
    public void sendEmbed(String channelOrId, String title, String description, String colorHex) {
        github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel textChannel = null;
        if (channelOrId.matches("\\\\d{17,19}")) {
            textChannel = DiscordSRV.getPlugin().getJda().getTextChannelById(channelOrId);
        }
        if (textChannel == null) {
            textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelOrId);
        }
        if (textChannel != null) {
            github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder embed = new github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder();
            if (title != null && !title.isEmpty()) embed.setTitle(title);
            if (description != null && !description.isEmpty()) embed.setDescription(description);
            if (colorHex != null && !colorHex.isEmpty()) {
                try {
                    embed.setColor(java.awt.Color.decode(colorHex));
                } catch (Exception ignored) {}
            }
            textChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public void sendPrivateEmbed(java.util.UUID playerUuid, String title, String description, String colorHex) {
        String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(playerUuid);
        if (discordId != null && !discordId.isEmpty()) {
            github.scarsz.discordsrv.dependencies.jda.api.entities.User user = DiscordSRV.getPlugin().getJda().getUserById(discordId);
            if (user != null) {
                user.openPrivateChannel().queue(ch -> {
                    github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder embed = new github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder();
                    if (title != null && !title.isEmpty()) embed.setTitle(title);
                    if (description != null && !description.isEmpty()) embed.setDescription(description);
                    if (colorHex != null && !colorHex.isEmpty()) {
                        try {
                            embed.setColor(java.awt.Color.decode(colorHex));
                        } catch (Exception ignored) {}
                    }
                    ch.sendMessageEmbeds(embed.build()).queue();
                });
            }
        }
    }
}
