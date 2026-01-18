package dev.harsh.plugin.outfitsplus;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;

import dev.harsh.plugin.outfitsplus.api.OutfitsPlusAPI;
import dev.harsh.plugin.outfitsplus.command.CommandManager;
import dev.harsh.plugin.outfitsplus.config.ConfigManager;
import dev.harsh.plugin.outfitsplus.config.ReloadManager;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticLoader;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.integration.IntegrationManager;
import dev.harsh.plugin.outfitsplus.listener.*;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.packet.listener.EquipmentPacketListener;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.render.CosmeticRenderer;
import dev.harsh.plugin.outfitsplus.storage.StorageFactory;
import dev.harsh.plugin.outfitsplus.storage.StorageProvider;
import dev.harsh.plugin.outfitsplus.util.SchedulerUtil;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class OutfitsPlus extends JavaPlugin {

    private static OutfitsPlus instance;

    private ConfigManager configManager;
    private LocaleManager localeManager;
    private ReloadManager reloadManager;

    private StorageProvider storageProvider;
    private PlayerDataCache playerCache;

    private CosmeticRegistry cosmeticRegistry;
    private CosmeticLoader cosmeticLoader;

    private CosmeticRenderer cosmeticRenderer;
    private CosmeticPacketSender packetSender;

    private CommandManager commandManager;
    private IntegrationManager integrationManager;

    @Override
    public void onLoad() {
        instance = this;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(true)
                .checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        try {
            SchedulerUtil.init(this);

            initializeConfig();
            initializeStorage();
            initializeCosmetics();
            initializeRendering();
            initializeCommands();
            initializeListeners();
            initializeIntegrations();
            initializeAPI();
            setupReloadManager();
            startAutoSaveTask();

            getLogger().info("OutfitsPlus v" + getDescription().getVersion() + " enabled!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable OutfitsPlus: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (integrationManager != null) {
            integrationManager.disableAll();
        }

        if (playerCache != null) {
            playerCache.saveAll();
        }

        if (storageProvider != null) {
            storageProvider.shutdown();
        }

        PacketEvents.getAPI().terminate();

        getLogger().info("OutfitsPlus disabled.");
    }

    private void initializeConfig() throws Exception {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.reload();

        localeManager = new LocaleManager(this, configManager.getConfig().defaultLocale());
        localeManager.reload();
    }

    private void initializeStorage() {
        storageProvider = StorageFactory.create(configManager.getConfig(), this);
        storageProvider.initialize();
        playerCache = new PlayerDataCache(storageProvider, configManager.getConfig());
    }

    private void initializeCosmetics() throws Exception {
        cosmeticRegistry = new CosmeticRegistry();
        cosmeticLoader = new CosmeticLoader(this, cosmeticRegistry, new File(getDataFolder(), "outfits"));
        if (configManager.getConfig().generateDefaults()) {
            cosmeticLoader.generateDefaultCosmetics();
        }
        cosmeticLoader.reload();
    }

    private void initializeRendering() {
        cosmeticRenderer = new CosmeticRenderer(cosmeticRegistry, playerCache);
        packetSender = new CosmeticPacketSender(cosmeticRenderer);

        EquipmentPacketListener packetListener = new EquipmentPacketListener(cosmeticRenderer, playerCache);
        PacketEvents.getAPI().getEventManager().registerListener(packetListener, PacketListenerPriority.NORMAL);
    }

    private void initializeCommands() {
        commandManager = new CommandManager(this);

        PluginCommand cmd = getCommand("outfits");
        if (cmd != null) {
            cmd.setExecutor(commandManager);
            cmd.setTabCompleter(commandManager);
        }
    }

    private void initializeListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new PlayerWorldChangeListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
    }

    private void initializeIntegrations() {
        integrationManager = new IntegrationManager(this);
        integrationManager.loadIntegrations();
    }

    private void initializeAPI() {
        OutfitsPlusAPI api = new OutfitsPlusAPI(
                cosmeticRegistry,
                playerCache,
                packetSender,
                localeManager
        );
        OutfitsPlusAPI.setInstance(api);
    }

    private void setupReloadManager() {
        reloadManager = new ReloadManager(this);
        reloadManager.register(configManager);
        reloadManager.register(localeManager);
        reloadManager.register(cosmeticLoader);

        reloadManager.addListener(result -> {
            if (result.hasSuccesses()) {
                packetSender.resyncAllPlayers();
            }
            integrationManager.reloadAll();
        });
    }

    private void startAutoSaveTask() {
        int interval = configManager.getConfig().autoSaveInterval();
        if (interval > 0) {
            long ticks = interval * 60L * 20L;
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                playerCache.saveAll();
            }, ticks, ticks);
        }
    }

    public static OutfitsPlus getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public ReloadManager getReloadManager() {
        return reloadManager;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public PlayerDataCache getPlayerCache() {
        return playerCache;
    }

    public CosmeticRegistry getCosmeticRegistry() {
        return cosmeticRegistry;
    }

    public CosmeticRenderer getCosmeticRenderer() {
        return cosmeticRenderer;
    }

    public CosmeticPacketSender getPacketSender() {
        return packetSender;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
}
