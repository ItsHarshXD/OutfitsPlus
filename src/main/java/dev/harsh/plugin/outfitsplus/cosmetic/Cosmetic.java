package dev.harsh.plugin.outfitsplus.cosmetic;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Cosmetic(
        String id,
        CosmeticCategory category,
        String displayName,
        List<String> lore,
        Material baseMaterial,
        int customModelData,
        Optional<MaskType> maskType,
        String permission,
        boolean defaultUnlocked,
        int priority,
        Map<String, Object> metadata) {

    public String getEffectivePermission() {
        if (permission != null && !permission.isEmpty()) {
            return permission;
        }
        return "outfitsplus.cosmetic." + category.getConfigFolder() + "." + id;
    }

    public String getMetadataString(String key, String defaultValue) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public int getMetadataInt(String key, int defaultValue) {
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    public boolean getMetadataBoolean(String key, boolean defaultValue) {
        Object value = metadata.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return defaultValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private CosmeticCategory category;
        private String displayName;
        private List<String> lore = List.of();
        private Material baseMaterial = null;
        private int customModelData = 0;
        private MaskType maskType;
        private String permission;
        private boolean defaultUnlocked = false;
        private int priority = 0;
        private Map<String, Object> metadata = Map.of();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder category(CosmeticCategory category) {
            this.category = category;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder lore(List<String> lore) {
            this.lore = lore != null ? lore : List.of();
            return this;
        }

        public Builder baseMaterial(Material baseMaterial) {
            this.baseMaterial = baseMaterial;
            return this;
        }

        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder maskType(MaskType maskType) {
            this.maskType = maskType;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder defaultUnlocked(boolean defaultUnlocked) {
            this.defaultUnlocked = defaultUnlocked;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Cosmetic build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Cosmetic id is required");
            }
            if (category == null) {
                throw new IllegalStateException("Cosmetic category is required");
            }

            if (displayName == null) {
                displayName = id;
            }
            if (baseMaterial == null) {
                baseMaterial = category.getDefaultMaterial();
            }

            return new Cosmetic(
                    id,
                    category,
                    displayName,
                    lore,
                    baseMaterial,
                    customModelData,
                    Optional.ofNullable(maskType),
                    permission,
                    defaultUnlocked,
                    priority,
                    metadata);
        }
    }
}
