package dev.harsh.plugin.outfitsplus.listener;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.util.SchedulerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public final class PlayerWorldChangeListener implements Listener {

    private final CosmeticPacketSender packetSender;

    public PlayerWorldChangeListener(OutfitsPlus plugin) {
        this.packetSender = plugin.getPacketSender();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        SchedulerUtil.runForPlayerLater(player, () -> {
            packetSender.resyncCosmetics(player);
        }, 2L);
    }
}
