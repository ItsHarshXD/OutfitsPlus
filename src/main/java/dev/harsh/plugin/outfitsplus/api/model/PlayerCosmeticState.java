package dev.harsh.plugin.outfitsplus.api.model;

import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.player.PlayerData;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record PlayerCosmeticState(
        UUID playerId,
        Map<CosmeticCategory, String> equippedCosmetics,
        Set<String> unlockedCosmetics,
        boolean showOwnCosmetics,
        boolean showOthersCosmetics,
        String locale
) {

    public Optional<String> getEquipped(CosmeticCategory category) {
        return Optional.ofNullable(equippedCosmetics.get(category));
    }

    public boolean hasEquipped(CosmeticCategory category) {
        return equippedCosmetics.containsKey(category);
    }

    public boolean hasUnlocked(String cosmeticId) {
        return unlockedCosmetics.contains(cosmeticId);
    }

    public static PlayerCosmeticState from(PlayerData data) {
        return new PlayerCosmeticState(
                data.getPlayerId(),
                Map.copyOf(data.getAllEquipped()),
                Set.copyOf(data.getUnlockedCosmetics()),
                data.getVisibility().isShowOwnCosmetics(),
                data.getVisibility().isShowOthersCosmetics(),
                data.getLocale()
        );
    }
}
