package dev.harsh.plugin.outfitsplus.storage;

import dev.harsh.plugin.outfitsplus.player.PlayerData;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageProvider {

    void initialize();

    void shutdown();

    Optional<PlayerData> load(UUID playerId);

    CompletableFuture<Optional<PlayerData>> loadAsync(UUID playerId);

    void save(PlayerData data);

    CompletableFuture<Void> saveAsync(PlayerData data);

    void delete(UUID playerId);

    boolean exists(UUID playerId);

    Collection<UUID> getAllPlayerIds();

    String getType();
}
