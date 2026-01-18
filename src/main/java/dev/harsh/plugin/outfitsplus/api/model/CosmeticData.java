package dev.harsh.plugin.outfitsplus.api.model;

import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.MaskType;
import org.bukkit.Material;

import java.util.Map;
import java.util.Optional;

public record CosmeticData(
        String id,
        CosmeticCategory category,
        String displayNameKey,
        String descriptionKey,
        Material baseMaterial,
        int customModelData,
        Optional<MaskType> maskType,
        String permission,
        boolean defaultUnlocked,
        int priority,
        Map<String, Object> metadata
) {

    public static CosmeticData from(Cosmetic cosmetic) {
        return new CosmeticData(
                cosmetic.id(),
                cosmetic.category(),
                cosmetic.displayNameKey(),
                cosmetic.descriptionKey(),
                cosmetic.baseMaterial(),
                cosmetic.customModelData(),
                cosmetic.maskType(),
                cosmetic.getEffectivePermission(),
                cosmetic.defaultUnlocked(),
                cosmetic.priority(),
                cosmetic.metadata()
        );
    }
}
