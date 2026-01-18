package dev.harsh.plugin.outfitsplus.storage.yaml;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.storage.StorageProvider;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class YamlStorageProvider implements StorageProvider {

    private final OutfitsPlus plugin;
    private final File playerDataFolder;

    public YamlStorageProvider(OutfitsPlus plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
    }

    @Override
    public void initialize() {
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        plugin.getLogger().info("Initialized YAML storage provider");
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Optional<PlayerData> load(UUID playerId) {
        File file = getPlayerFile(playerId);
        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            PlayerData data = new PlayerData(playerId);

            if (yaml.contains("equipped")) {
                for (String categoryName : yaml.getConfigurationSection("equipped").getKeys(false)) {
                    CosmeticCategory.fromString(categoryName).ifPresent(category -> {
                        String cosmeticId = yaml.getString("equipped." + categoryName);
                        if (cosmeticId != null && !cosmeticId.isEmpty()) {
                            data.equip(category, cosmeticId);
                        }
                    });
                }
            }

            List<String> unlocked = yaml.getStringList("unlocked");
            for (String cosmeticId : unlocked) {
                data.unlock(cosmeticId);
            }

            data.getVisibility().setShowOwnCosmetics(yaml.getBoolean("visibility.own", true));
            data.getVisibility().setShowOthersCosmetics(yaml.getBoolean("visibility.others", true));
            data.setLocale(yaml.getString("locale", "en"));

            data.markClean();
            return Optional.of(data);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load player data for " + playerId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> loadAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> load(playerId));
    }

    @Override
    public void save(PlayerData data) {
        File file = getPlayerFile(data.getPlayerId());
        YamlConfiguration yaml = new YamlConfiguration();

        for (Map.Entry<CosmeticCategory, String> entry : data.getAllEquipped().entrySet()) {
            yaml.set("equipped." + entry.getKey().name().toLowerCase(), entry.getValue());
        }

        yaml.set("unlocked", new ArrayList<>(data.getUnlockedCosmetics()));
        yaml.set("visibility.own", data.getVisibility().isShowOwnCosmetics());
        yaml.set("visibility.others", data.getVisibility().isShowOthersCosmetics());
        yaml.set("locale", data.getLocale());

        try {
            yaml.save(file);
            data.markClean();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + data.getPlayerId() + ": " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync(PlayerData data) {
        return CompletableFuture.runAsync(() -> save(data));
    }

    @Override
    public void delete(UUID playerId) {
        File file = getPlayerFile(playerId);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public boolean exists(UUID playerId) {
        return getPlayerFile(playerId).exists();
    }

    @Override
    public Collection<UUID> getAllPlayerIds() {
        Set<UUID> playerIds = new HashSet<>();
        File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".yml", "");
                try {
                    playerIds.add(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return playerIds;
    }

    @Override
    public String getType() {
        return "yaml";
    }

    private File getPlayerFile(UUID playerId) {
        return new File(playerDataFolder, playerId.toString() + ".yml");
    }
}
