package dev.harsh.plugin.outfitsplus.api;

import dev.harsh.plugin.outfitsplus.api.event.CosmeticEquipEvent;
import dev.harsh.plugin.outfitsplus.api.event.CosmeticUnequipEvent;
import dev.harsh.plugin.outfitsplus.api.model.CosmeticData;
import dev.harsh.plugin.outfitsplus.api.model.PlayerCosmeticState;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class OutfitsPlusAPI {

    private static OutfitsPlusAPI instance;

    private final CosmeticRegistry registry;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;
    private final LocaleManager localeManager;

    public OutfitsPlusAPI(CosmeticRegistry registry, PlayerDataCache playerCache,
                          CosmeticPacketSender packetSender, LocaleManager localeManager) {
        this.registry = registry;
        this.playerCache = playerCache;
        this.packetSender = packetSender;
        this.localeManager = localeManager;
    }

    public static OutfitsPlusAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("OutfitsPlus API not initialized");
        }
        return instance;
    }

    public static void setInstance(OutfitsPlusAPI api) {
        instance = api;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    public Optional<CosmeticData> getCosmetic(String id) {
        return registry.get(id).map(CosmeticData::from);
    }

    public Collection<CosmeticData> getAllCosmetics() {
        return registry.getAll().stream()
                .map(CosmeticData::from)
                .collect(Collectors.toList());
    }

    public Collection<CosmeticData> getCosmeticsByCategory(CosmeticCategory category) {
        return registry.getByCategory(category).stream()
                .map(CosmeticData::from)
                .collect(Collectors.toList());
    }

    public void registerProvider(CosmeticProvider provider) {
        for (Cosmetic cosmetic : provider.getCosmetics()) {
            registry.register(cosmetic);
        }
    }

    public Optional<PlayerCosmeticState> getPlayerState(UUID playerId) {
        return playerCache.get(playerId).map(PlayerCosmeticState::from);
    }

    public boolean equipCosmetic(UUID playerId, String cosmeticId) {
        Optional<Cosmetic> cosmeticOpt = registry.get(cosmeticId);
        if (cosmeticOpt.isEmpty()) {
            return false;
        }

        Cosmetic cosmetic = cosmeticOpt.get();
        PlayerData data = playerCache.getOrLoad(playerId);

        CosmeticEquipEvent event = new CosmeticEquipEvent(playerId, cosmetic);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        data.equip(cosmetic.category(), cosmeticId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            packetSender.sendCosmeticUpdate(player);
        }

        return true;
    }

    public void unequipCosmetic(UUID playerId, CosmeticCategory category) {
        playerCache.get(playerId).ifPresent(data -> {
            data.getEquipped(category).ifPresent(cosmeticId -> {
                registry.get(cosmeticId).ifPresent(cosmetic -> {
                    CosmeticUnequipEvent event = new CosmeticUnequipEvent(playerId, cosmetic);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }

                    data.unequip(category);

                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        packetSender.sendCosmeticUpdate(player);
                    }
                });
            });
        });
    }

    public void unequipAll(UUID playerId) {
        playerCache.get(playerId).ifPresent(data -> {
            data.unequipAll();

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                packetSender.sendCosmeticUpdate(player);
            }
        });
    }

    public void unlockCosmetic(UUID playerId, String cosmeticId) {
        playerCache.getOrLoad(playerId).unlock(cosmeticId);
    }

    public void lockCosmetic(UUID playerId, String cosmeticId) {
        PlayerData data = playerCache.getOrLoad(playerId);
        data.lock(cosmeticId);

        registry.get(cosmeticId).ifPresent(cosmetic -> {
            if (data.getEquipped(cosmetic.category()).map(id -> id.equals(cosmeticId)).orElse(false)) {
                unequipCosmetic(playerId, cosmetic.category());
            }
        });
    }

    public boolean hasUnlocked(UUID playerId, String cosmeticId) {
        return playerCache.get(playerId)
                .map(data -> data.hasUnlocked(cosmeticId))
                .orElse(false);
    }

    public void setShowOwnCosmetics(UUID playerId, boolean show) {
        playerCache.get(playerId).ifPresent(data -> {
            data.getVisibility().setShowOwnCosmetics(show);
            data.markDirty();

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                packetSender.resyncCosmetics(player);
            }
        });
    }

    public void setShowOthersCosmetics(UUID playerId, boolean show) {
        playerCache.get(playerId).ifPresent(data -> {
            data.getVisibility().setShowOthersCosmetics(show);
            data.markDirty();

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                packetSender.resyncCosmetics(player);
            }
        });
    }

    public void resyncCosmetics(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            packetSender.resyncCosmetics(player);
        }
    }

    public void resyncAllCosmetics() {
        packetSender.resyncAllPlayers();
    }
}
