package dev.harsh.plugin.outfitsplus.player;

import dev.harsh.plugin.outfitsplus.config.PluginConfig;
import dev.harsh.plugin.outfitsplus.storage.StorageProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataCache {

    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final StorageProvider storageProvider;
    private final PluginConfig config;

    public PlayerDataCache(StorageProvider storageProvider, PluginConfig config) {
        this.storageProvider = storageProvider;
        this.config = config;
    }

    public PlayerData getOrLoad(UUID playerId) {
        return cache.computeIfAbsent(playerId, id ->
            storageProvider.load(id).orElseGet(() -> createDefaultData(id))
        );
    }

    public CompletableFuture<PlayerData> getOrLoadAsync(UUID playerId) {
        PlayerData cached = cache.get(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return storageProvider.loadAsync(playerId).thenApply(optData -> {
            PlayerData data = optData.orElseGet(() -> createDefaultData(playerId));
            cache.put(playerId, data);
            return data;
        });
    }

    private PlayerData createDefaultData(UUID playerId) {
        PlayerData data = new PlayerData(playerId);
        if (config != null) {
            data.getVisibility().setShowOwnCosmetics(config.defaults().showOwnCosmetics());
            data.getVisibility().setShowOthersCosmetics(config.defaults().showOthersCosmetics());
            data.setLocale(config.defaultLocale());
        }
        return data;
    }

    public Optional<PlayerData> get(UUID playerId) {
        return Optional.ofNullable(cache.get(playerId));
    }

    public boolean isLoaded(UUID playerId) {
        return cache.containsKey(playerId);
    }

    public void save(UUID playerId) {
        PlayerData data = cache.get(playerId);
        if (data != null && data.isDirty()) {
            storageProvider.save(data);
        }
    }

    public CompletableFuture<Void> saveAsync(UUID playerId) {
        PlayerData data = cache.get(playerId);
        if (data != null && data.isDirty()) {
            return storageProvider.saveAsync(data);
        }
        return CompletableFuture.completedFuture(null);
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                storageProvider.save(data);
            }
        }
    }

    public CompletableFuture<Void> saveAllAsync() {
        return CompletableFuture.allOf(
                cache.values().stream()
                        .filter(PlayerData::isDirty)
                        .map(storageProvider::saveAsync)
                        .toArray(CompletableFuture[]::new)
        );
    }

    public void unload(UUID playerId) {
        save(playerId);
        cache.remove(playerId);
    }

    public void invalidate(UUID playerId) {
        cache.remove(playerId);
    }

    public void invalidateAll() {
        cache.clear();
    }

    public void delete(UUID playerId) {
        cache.remove(playerId);
        storageProvider.delete(playerId);
    }

    public Collection<PlayerData> getAllLoaded() {
        return cache.values();
    }

    public int size() {
        return cache.size();
    }
}
