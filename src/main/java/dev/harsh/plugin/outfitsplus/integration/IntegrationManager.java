package dev.harsh.plugin.outfitsplus.integration;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.config.PluginConfig;
import dev.harsh.plugin.outfitsplus.integration.itemsadder.ItemsAdderIntegration;
import dev.harsh.plugin.outfitsplus.integration.nexo.NexoIntegration;

import java.util.ArrayList;
import java.util.List;

public final class IntegrationManager {

    private final OutfitsPlus plugin;
    private final List<Integration> integrations = new ArrayList<>();
    private final List<Integration> enabledIntegrations = new ArrayList<>();

    public IntegrationManager(OutfitsPlus plugin) {
        this.plugin = plugin;
    }

    public void loadIntegrations() {
        PluginConfig.IntegrationSettings settings = plugin.getConfigManager().getConfig().integrations();

        integrations.add(new ItemsAdderIntegration(plugin));
        integrations.add(new NexoIntegration(plugin));

        for (Integration integration : integrations) {
            if (shouldEnable(integration, settings) && integration.isAvailable()) {
                try {
                    integration.enable();
                    enabledIntegrations.add(integration);
                    plugin.getLogger().info("Enabled integration: " + integration.getName());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to enable integration " + integration.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private boolean shouldEnable(Integration integration, PluginConfig.IntegrationSettings settings) {
        return switch (integration.getName().toLowerCase()) {
            case "itemsadder" -> settings.itemsAdderEnabled();
            case "nexo" -> settings.nexoEnabled();
            default -> false;
        };
    }

    public void disableAll() {
        for (Integration integration : enabledIntegrations) {
            try {
                integration.disable();
            } catch (Exception e) {
                plugin.getLogger().warning("Error disabling integration " + integration.getName() + ": " + e.getMessage());
            }
        }
        enabledIntegrations.clear();
    }

    public void reloadAll() {
        for (Integration integration : enabledIntegrations) {
            try {
                integration.reload();
            } catch (Exception e) {
                plugin.getLogger().warning("Error reloading integration " + integration.getName() + ": " + e.getMessage());
            }
        }
    }

    public List<Integration> getEnabledIntegrations() {
        return List.copyOf(enabledIntegrations);
    }

    public boolean isIntegrationEnabled(String name) {
        return enabledIntegrations.stream()
                .anyMatch(i -> i.getName().equalsIgnoreCase(name));
    }
}
