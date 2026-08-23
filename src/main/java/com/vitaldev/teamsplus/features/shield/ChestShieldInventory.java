package com.vitaldev.teamsplus.features.shield;

import com.vitaldev.teamsplus.TeamsPlus;
import com.vitaldev.teamsplus.features.chest.ChestMenuInventory;
import com.vitaldev.teamsplus.model.Team;
import com.vitaldev.vitallibs.config.ConfigHandler;
import com.vitaldev.vitallibs.inventory.InventoryBuilder;
import com.vitaldev.vitallibs.items.ItemHandler;
import com.vitaldev.vitallibs.util.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class ChestShieldInventory {

    private final TeamsPlus plugin;
    private final ConfigHandler shieldConfig;
    private final Team team;
    private final Player player;
    private final InventoryBuilder builder;
    private final ConfigHandler langHandler;
    private final ShieldManager manager;
    private BukkitTask updateTask;

    public ChestShieldInventory(TeamsPlus plugin, Player player) {
        this.plugin = plugin;
        this.shieldConfig = plugin.getShieldManager().getConfig();
        this.langHandler = plugin.getLangFile();
        this.team = Team.getTeam(player);
        this.player = player;
        this.builder = new InventoryBuilder(shieldConfig.getInt("shield.menu.size"),
                shieldConfig.getMessage("shield.menu.title").replace("{TEAM}", team.getTeamName()), true);
        this.manager = plugin.getShieldManager();
    }

    // Helper method to convert milliseconds into short form string (e.g., 1h 20m 10s)
    private String formatTimeShort(long millis) {
        if (millis <= 0) return "0s";

        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || builder.length() == 0) {
            builder.append(seconds).append("s");
        }

        return builder.toString().trim();
    }

    public void openInventory() {
        setupMenu();
        setupStatus();
        setupAutoToggle();
        builder.open(player);

        // Start the repeating scheduler task to update timers live every second (20 ticks)
        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Safety check: Stop the task if the player logs out or closes the inventory
                if (!player.isOnline() || player.getOpenInventory().getTopInventory() == null) {
                    cancel();
                    return;
                }

                // Re-calculate live timings and rebuild status/toggle components
                setupStatus();
                setupAutoToggle();

                // Refresh the items inside the player's active open container view
                org.bukkit.inventory.Inventory openInv = player.getOpenInventory().getTopInventory();
                for (int slot = 0; slot < openInv.getSize(); slot++) {
                    ItemStack updatedItem = openInv.getItem(slot);
                    if (updatedItem != null) {
                        openInv.setItem(slot, updatedItem);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void setupMenu() {
        String fillerPath = "shield.menu.filler";
        String closePath = "shield.menu.close";
        String backPath = "shield.menu.back";

        ItemStack filler = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(fillerPath + ".material"))),
                shieldConfig.getMessage(fillerPath + ".name"),
                shieldConfig.getInt(fillerPath + ".amount"),
                shieldConfig.getStringList(fillerPath + ".lore"),
                shieldConfig.getBoolean(fillerPath + ".glow"),
                true
        );

        ItemStack backButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(backPath + ".material"))),
                shieldConfig.getMessage(backPath + ".name"),
                shieldConfig.getInt(backPath + ".amount"),
                shieldConfig.getStringList(backPath + ".lore"),
                shieldConfig.getBoolean(backPath + ".glow"),
                true
        );

        ItemStack closeButton = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(closePath + ".material"))),
                shieldConfig.getMessage(closePath + ".name"),
                shieldConfig.getInt(closePath + ".amount"),
                shieldConfig.getStringList(closePath + ".lore"),
                shieldConfig.getBoolean(closePath + ".glow"),
                true
        );

        builder.setBackButton(backButton, event -> {
            event.setCancelled(true);
            if (updateTask != null) {
                updateTask.cancel();
            }
            new ChestMenuInventory(plugin, player).openInventory();
        });

        builder.setCloseButton(closeButton, event -> {
            event.setCancelled(true);
            if (event.getCursor().getType() == Material.AIR) {
                if (updateTask != null) {
                    updateTask.cancel();
                }
                player.closeInventory();
            }
        });

        builder.fillWithBorderItem(filler);
    }

    public void setupStatus() {
        int slot = shieldConfig.getInt("shield.menu.status.slot");

        // Format the charge time using our short helper method
        String chargeFormatted = formatTimeShort(team.getShieldChargeSeconds() * 1000L);

        if (team.isShieldActive()) {
            String path = "shield.menu.status.active";
            long millis = manager.getShieldActiveMillis(team);
            String activeFormatted = formatTimeShort(millis);

            List<String> lore = shieldConfig.getStringList(path + ".lore").stream()
                    .map(line -> ChatUtil.color(line.replace("{CHARGE}", chargeFormatted).replace("{ACTIVE_TIME}", activeFormatted)))
                    .toList();

            ItemStack item = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(path + ".material"))),
                    shieldConfig.getMessage(path + ".name"),
                    1,
                    lore,
                    shieldConfig.getBoolean(path + ".glow"),
                    true
            );

            builder.addItem(slot, item, event -> {
                event.setCancelled(true);
                if (updateTask != null) {
                    updateTask.cancel();
                }
                manager.deactivateShield(team);
                openInventory();
            });

        } else if (team.isShieldDeploying()) {
            String path = "shield.menu.status.deploying";
            long deployMillis = manager.getDeployRemainingMillis(team);
            String deployFormatted = formatTimeShort(deployMillis);

            List<String> lore = shieldConfig.getStringList(path + ".lore").stream()
                    .map(line -> ChatUtil.color(line.replace("{DEPLOY_TIME}", deployFormatted)))
                    .toList();

            ItemStack item = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(path + ".material"))),
                    shieldConfig.getMessage(path + ".name"),
                    1,
                    lore,
                    shieldConfig.getBoolean(path + ".glow"),
                    true
            );

            builder.addItem(slot, item, event -> event.setCancelled(true));

        } else if (manager.getCooldownRemainingMillis(team) > 0) {
            String path = "shield.menu.status.cooldown";
            long cooldownMillis = manager.getCooldownRemainingMillis(team);
            String cooldownFormatted = formatTimeShort(cooldownMillis);

            List<String> lore = shieldConfig.getStringList(path + ".lore").stream()
                    .map(line -> ChatUtil.color(line.replace("{COOLDOWN}", cooldownFormatted).replace("{CHARGE}", chargeFormatted)))
                    .toList();

            ItemStack item = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(path + ".material"))),
                    shieldConfig.getMessage(path + ".name"),
                    1,
                    lore,
                    shieldConfig.getBoolean(path + ".glow"),
                    true
            );

            builder.addItem(slot, item, event -> {
                event.setCancelled(true);
                player.sendMessage(langHandler.getMessage("messages.shield.on-cooldown").replace("{TIME}", cooldownFormatted));
            });

        } else {
            String path = "shield.menu.status.inactive";

            List<String> lore = shieldConfig.getStringList(path + ".lore").stream()
                    .map(line -> ChatUtil.color(line.replace("{CHARGE}", chargeFormatted)))
                    .toList();

            ItemStack item = ItemHandler.buildItem(
                    Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(path + ".material"))),
                    shieldConfig.getMessage(path + ".name"),
                    1,
                    lore,
                    shieldConfig.getBoolean(path + ".glow"),
                    true
            );

            builder.addItem(slot, item, event -> {
                event.setCancelled(true);
                long minAuto = manager.getConfig().getLong("shield.settings.min-auto-activate-seconds");
                if (team.getShieldChargeSeconds() >= minAuto) {
                    if (updateTask != null) {
                        updateTask.cancel();
                    }
                    manager.activateShield(team);
                    openInventory();
                } else {
                    player.sendMessage(langHandler.getMessage("messages.shield.not-enough-charge"));
                }
            });
        }
    }

    public void setupAutoToggle() {
        int slot = shieldConfig.getInt("shield.menu.auto-toggle.slot");
        String path = team.isAutoShieldEnabled() ? "shield.menu.auto-toggle.enabled" : "shield.menu.auto-toggle.disabled";

        ItemStack item = ItemHandler.buildItem(
                Objects.requireNonNull(Material.getMaterial(shieldConfig.getString(path + ".material"))),
                shieldConfig.getMessage(path + ".name"),
                1,
                shieldConfig.getStringList(path + ".lore"),
                shieldConfig.getBoolean(path + ".glow"),
                true
        );

        builder.addItem(slot, item, event -> {
            event.setCancelled(true);
            manager.toggleAutoShield(team);
            if (updateTask != null) {
                updateTask.cancel();
            }
            openInventory();
        });
    }
}