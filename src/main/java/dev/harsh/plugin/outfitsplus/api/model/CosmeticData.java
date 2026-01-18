package dev.harsh.plugin.outfitsplus.api.model;

import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.MaskType;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record CosmeticData(
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

    public static CosmeticData from(Cosmetic cosmetic) {
        return new CosmeticData(
                cosmetic.id(),
                cosmetic.category(),
                cosmetic.displayName(),
                cosmetic.lore(),
                cosmetic.baseMaterial(),
                cosmetic.customModelData(),
                cosmetic.maskType(),
                cosmetic.getEffectivePermission(),
                cosmetic.defaultUnlocked(),
                cosmetic.priority(),
                cosmetic.metadata());
    }
}
