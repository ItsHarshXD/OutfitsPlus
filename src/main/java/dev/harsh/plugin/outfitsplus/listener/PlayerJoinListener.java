package dev.harsh.plugin.outfitsplus.listener;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final OutfitsPlus plugin;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;
    private final LocaleManager localeManager;

    public PlayerJoinListener(OutfitsPlus plugin) {
        this.plugin = plugin;
        this.playerCache = plugin.getPlayerCache();
        this.packetSender = plugin.getPacketSender();
        this.localeManager = plugin.getLocaleManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        playerCache.getOrLoadAsync(player.getUniqueId()).thenAccept(data -> {
            if (data.getLocale() != null) {
                localeManager.setPlayerLocale(player.getUniqueId(), data.getLocale());
            }

            SchedulerUtil.runForPlayerLater(player, () -> {
                packetSender.resyncCosmetics(player);
            }, 5L);
        });
    }
}
