package dev.harsh.plugin.outfitsplus.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public record PluginConfig(
        StorageConfig storage,
        String defaultLocale,
        int autoSaveInterval,
    boolean generateDefaults,
        boolean debug,
        DefaultSettings defaults,
        IntegrationSettings integrations
) {

    public static PluginConfig parse(FileConfiguration config) {
        return new PluginConfig(
                StorageConfig.parse(config.getConfigurationSection("storage")),
                config.getString("default-locale", "en"),
                config.getInt("auto-save-interval", 5),
            config.getBoolean("generate-defaults", true),
                config.getBoolean("debug", false),
                DefaultSettings.parse(config.getConfigurationSection("defaults")),
                IntegrationSettings.parse(config.getConfigurationSection("integrations"))
        );
    }

    public record StorageConfig(
            String type,
            MySqlConfig mysql
    ) {
        public static StorageConfig parse(ConfigurationSection section) {
            if (section == null) {
                return new StorageConfig("yaml", MySqlConfig.defaults());
            }
            return new StorageConfig(
                    section.getString("type", "yaml"),
                    MySqlConfig.parse(section.getConfigurationSection("mysql"))
            );
        }
    }

    public record MySqlConfig(
            String host,
            int port,
            String database,
            String username,
            String password,
            String tablePrefix,
            int poolSize,
            int connectionTimeout
    ) {
        public static MySqlConfig defaults() {
            return new MySqlConfig("localhost", 3306, "outfitsplus", "root", "", "op_", 10, 5000);
        }

        public static MySqlConfig parse(ConfigurationSection section) {
            if (section == null) {
                return defaults();
            }
            return new MySqlConfig(
                    section.getString("host", "localhost"),
                    section.getInt("port", 3306),
                    section.getString("database", "outfitsplus"),
                    section.getString("username", "root"),
                    section.getString("password", ""),
                    section.getString("table-prefix", "op_"),
                    section.getInt("pool-size", 10),
                    section.getInt("connection-timeout", 5000)
            );
        }
    }

    public record DefaultSettings(
            boolean showOwnCosmetics,
            boolean showOthersCosmetics
    ) {
        public static DefaultSettings parse(ConfigurationSection section) {
            if (section == null) {
                return new DefaultSettings(true, true);
            }
            return new DefaultSettings(
                    section.getBoolean("show-own-cosmetics", true),
                    section.getBoolean("show-others-cosmetics", true)
            );
        }
    }

    public record IntegrationSettings(
            boolean itemsAdderEnabled,
            boolean nexoEnabled
    ) {
        public static IntegrationSettings parse(ConfigurationSection section) {
            if (section == null) {
                return new IntegrationSettings(false, false);
            }
            return new IntegrationSettings(
                    section.getBoolean("itemsadder.enabled", false),
                    section.getBoolean("nexo.enabled", false)
            );
        }
    }
}
