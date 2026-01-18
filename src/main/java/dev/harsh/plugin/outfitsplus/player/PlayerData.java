package dev.harsh.plugin.outfitsplus.player;

import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;

import java.util.*;

public final class PlayerData {

    private final UUID playerId;
    private final Map<CosmeticCategory, String> equippedCosmetics;
    private final Set<String> unlockedCosmetics;
    private final PlayerVisibilitySettings visibility;
    private String locale;
    private boolean dirty;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.equippedCosmetics = new EnumMap<>(CosmeticCategory.class);
        this.unlockedCosmetics = new HashSet<>();
        this.visibility = new PlayerVisibilitySettings();
        this.locale = "en";
        this.dirty = false;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Optional<String> getEquipped(CosmeticCategory category) {
        return Optional.ofNullable(equippedCosmetics.get(category));
    }

    public Map<CosmeticCategory, String> getAllEquipped() {
        return Collections.unmodifiableMap(equippedCosmetics);
    }

    public boolean hasEquipped(CosmeticCategory category) {
        return equippedCosmetics.containsKey(category);
    }

    public boolean hasAnythingEquipped() {
        return !equippedCosmetics.isEmpty();
    }

    public void equip(CosmeticCategory category, String cosmeticId) {
        for (CosmeticCategory cat : CosmeticCategory.values()) {
            if (cat != category && cat.conflictsWith(category)) {
                equippedCosmetics.remove(cat);
            }
        }
        equippedCosmetics.put(category, cosmeticId);
        markDirty();
    }

    public void unequip(CosmeticCategory category) {
        if (equippedCosmetics.remove(category) != null) {
            markDirty();
        }
    }

    public void unequipAll() {
        if (!equippedCosmetics.isEmpty()) {
            equippedCosmetics.clear();
            markDirty();
        }
    }

    public Set<String> getUnlockedCosmetics() {
        return unlockedCosmetics;
    }

    public boolean hasUnlocked(String cosmeticId) {
        return unlockedCosmetics.contains(cosmeticId);
    }

    public void unlock(String cosmeticId) {
        if (unlockedCosmetics.add(cosmeticId)) {
            markDirty();
        }
    }

    public void lock(String cosmeticId) {
        if (unlockedCosmetics.remove(cosmeticId)) {
            markDirty();
        }
    }

    public PlayerVisibilitySettings getVisibility() {
        return visibility;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        if (!this.locale.equals(locale)) {
            this.locale = locale;
            markDirty();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    public void reset() {
        equippedCosmetics.clear();
        unlockedCosmetics.clear();
        visibility.setShowOwnCosmetics(true);
        visibility.setShowOthersCosmetics(true);
        locale = "en";
        markDirty();
    }
}
