package dev.harsh.plugin.outfitsplus.listener;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final PlayerDataCache playerCache;
    private final LocaleManager localeManager;

    public PlayerQuitListener(OutfitsPlus plugin) {
        this.playerCache = plugin.getPlayerCache();
        this.localeManager = plugin.getLocaleManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        SchedulerUtil.runAsync(() -> {
            playerCache.unload(player.getUniqueId());
        });

        localeManager.removePlayerLocale(player.getUniqueId());
    }
}
