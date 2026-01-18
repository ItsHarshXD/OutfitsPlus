package dev.harsh.plugin.outfitsplus.cosmetic.registry;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.config.ReloadManager;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.MaskType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class CosmeticLoader implements ReloadManager.Reloadable {

    private final OutfitsPlus plugin;
    private final CosmeticRegistry registry;
    private final File outfitsFolder;

    public CosmeticLoader(OutfitsPlus plugin, CosmeticRegistry registry, File outfitsFolder) {
        this.plugin = plugin;
        this.registry = registry;
        this.outfitsFolder = outfitsFolder;
    }

    @Override
    public String getName() {
        return "Cosmetics";
    }

    @Override
    public void reload() throws Exception {
        registry.clear();
        ensureFoldersExist();

        int loaded = 0;
        int failed = 0;

        for (CosmeticCategory category : CosmeticCategory.values()) {
            File categoryFolder = new File(outfitsFolder, category.getConfigFolder());
            if (!categoryFolder.exists() || !categoryFolder.isDirectory()) {
                continue;
            }

            File[] files = categoryFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) {
                continue;
            }

            for (File file : files) {
                try {
                    Cosmetic cosmetic = parseCosmetic(file, category);
                    if (registry.exists(cosmetic.id())) {
                        Cosmetic existing = registry.get(cosmetic.id()).orElse(null);
                        String existingCategory = existing != null ? existing.category().name().toLowerCase() : "unknown";
                        throw new Exception("Duplicate cosmetic id: " + cosmetic.id() + " (already used in " + existingCategory + ")");
                    }
                    registry.register(cosmetic);
                    loaded++;
                    plugin.getLogger().fine("Loaded cosmetic: " + cosmetic.id());
                } catch (Exception e) {
                    failed++;
                    plugin.getLogger().warning("Failed to load cosmetic from " + file.getName() + ": " + e.getMessage());
                    if (plugin.getConfigManager().isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " cosmetics" + (failed > 0 ? " (" + failed + " failed)" : ""));

        if (loaded == 0) {
            plugin.getLogger().info("No cosmetics found. Create YAML files in plugins/OutfitsPlus/outfits/<category>/");
        }
    }

    private void ensureFoldersExist() {
        if (!outfitsFolder.exists()) {
            outfitsFolder.mkdirs();
        }

        for (CosmeticCategory category : CosmeticCategory.values()) {
            File categoryFolder = new File(outfitsFolder, category.getConfigFolder());
            if (!categoryFolder.exists()) {
                categoryFolder.mkdirs();
            }
        }
    }

    private Cosmetic parseCosmetic(File file, CosmeticCategory category) throws Exception {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        if (id == null || id.isEmpty()) {
            id = file.getName().replace(".yml", "");
        }

        String displayNameKey = yaml.getString("display-name");
        String descriptionKey = yaml.getString("description");

        String materialName = yaml.getString("material", "LEATHER_HELMET");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new Exception("Invalid material: " + materialName);
        }

        int customModelData = yaml.getInt("custom-model-data", 0);
        String permission = yaml.getString("permission");
        boolean defaultUnlocked = yaml.getBoolean("default-unlocked", false);
        int priority = yaml.getInt("priority", 0);

        MaskType maskType = null;
        if (category == CosmeticCategory.MASK) {
            String maskTypeStr = yaml.getString("mask-type");
            if (maskTypeStr != null) {
                maskType = MaskType.fromString(maskTypeStr).orElse(null);
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        ConfigurationSection metadataSection = yaml.getConfigurationSection("metadata");
        if (metadataSection != null) {
            for (String key : metadataSection.getKeys(false)) {
                metadata.put(key, metadataSection.get(key));
            }
        }

        return Cosmetic.builder()
                .id(id)
                .category(category)
                .displayNameKey(displayNameKey)
                .descriptionKey(descriptionKey)
                .baseMaterial(material)
                .customModelData(customModelData)
                .maskType(maskType)
                .permission(permission)
                .defaultUnlocked(defaultUnlocked)
                .priority(priority)
                .metadata(metadata)
                .build();
    }

    public void saveExampleCosmetic(CosmeticCategory category, String id) {
        File categoryFolder = new File(outfitsFolder, category.getConfigFolder());
        File file = new File(categoryFolder, id + ".yml");

        if (file.exists()) {
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", id);
        yaml.set("display-name", "cosmetic." + category.getConfigFolder() + "." + id + ".name");
        yaml.set("description", "cosmetic." + category.getConfigFolder() + "." + id + ".description");
        yaml.set("material", "LEATHER_HELMET");
        yaml.set("custom-model-data", 10001);
        yaml.set("permission", null);
        yaml.set("default-unlocked", false);
        yaml.set("priority", 0);

        if (category == CosmeticCategory.MASK) {
            yaml.set("mask-type", "FULL");
        }

        yaml.set("metadata.rarity", "common");
        yaml.set("metadata.author", "Server");

        try {
            yaml.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save example cosmetic: " + e.getMessage());
        }
    }

    public void generateDefaultCosmetics() {
        ensureFoldersExist();
        saveExampleCosmetic(CosmeticCategory.HAT, "example_hat");
        saveExampleCosmetic(CosmeticCategory.MASK, "example_mask");
        saveExampleCosmetic(CosmeticCategory.WINGS, "example_wings");
        saveExampleCosmetic(CosmeticCategory.TOP, "example_top");
        saveExampleCosmetic(CosmeticCategory.PANTS, "example_pants");
        saveExampleCosmetic(CosmeticCategory.SHOES, "example_shoes");
    }
}
