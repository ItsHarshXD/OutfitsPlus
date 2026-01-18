package dev.harsh.plugin.outfitsplus.storage;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.config.PluginConfig;
import dev.harsh.plugin.outfitsplus.storage.yaml.YamlStorageProvider;

public final class StorageFactory {

    private StorageFactory() {
    }

    public static StorageProvider create(PluginConfig config, OutfitsPlus plugin) {
        String type = config.storage().type().toLowerCase();

        return switch (type) {
            case "yaml", "flatfile", "file" -> new YamlStorageProvider(plugin);
            case "mysql", "sql", "database" -> {
                plugin.getLogger().warning("MySQL storage is not yet implemented, falling back to YAML");
                yield new YamlStorageProvider(plugin);
            }
            default -> {
                plugin.getLogger().warning("Unknown storage type: " + type + ", defaulting to YAML");
                yield new YamlStorageProvider(plugin);
            }
        };
    }
}
