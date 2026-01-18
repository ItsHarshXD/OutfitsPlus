package dev.harsh.plugin.outfitsplus.config;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;

public final class ConfigManager implements ReloadManager.Reloadable {

    private final OutfitsPlus plugin;
    private PluginConfig config;

    public ConfigManager(OutfitsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Configuration";
    }

    @Override
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = PluginConfig.parse(plugin.getConfig());

        if (config.debug()) {
            plugin.getLogger().info("Debug mode enabled");
            plugin.getLogger().info("Storage type: " + config.storage().type());
            plugin.getLogger().info("Default locale: " + config.defaultLocale());
            plugin.getLogger().info("Auto-save interval: " + config.autoSaveInterval() + " minutes");
        }
    }

    public PluginConfig getConfig() {
        return config;
    }

    public boolean isDebug() {
        return config != null && config.debug();
    }

    public void debug(String message) {
        if (isDebug()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}
