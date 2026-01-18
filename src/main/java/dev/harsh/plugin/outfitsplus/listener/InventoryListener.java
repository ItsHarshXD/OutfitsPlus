package dev.harsh.plugin.outfitsplus.listener;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.util.SchedulerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

public final class InventoryListener implements Listener {

    private final CosmeticPacketSender packetSender;
    private final PlayerDataCache playerCache;

    public InventoryListener(OutfitsPlus plugin) {
        this.packetSender = plugin.getPacketSender();
        this.playerCache = plugin.getPlayerCache();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();

        if (!playerCache.isLoaded(player.getUniqueId())) {
            return;
        }

        SchedulerUtil.runForPlayerLater(player, () -> {
            packetSender.sendCosmeticUpdate(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!playerCache.isLoaded(player.getUniqueId())) {
            return;
        }

        PlayerData data = playerCache.get(player.getUniqueId()).orElse(null);
        if (data == null) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }

        boolean isArmorSlotClick = false;

        if (clicked.getType() == InventoryType.PLAYER) {
            int slot = event.getSlot();
            isArmorSlotClick = isArmorSlot(slot);

            if (isArmorSlotClick && isCosmeticOnlySlot(player, data, slot)) {
                ItemStack cursor = event.getCursor();
                boolean cursorEmpty = cursor == null || cursor.getType() == Material.AIR;

                int hotbarButton = event.getHotbarButton();
                if (hotbarButton >= 0) {
                    ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);
                    boolean hotbarEmpty = hotbarItem == null || hotbarItem.getType() == Material.AIR;
                    if (hotbarEmpty) {
                        event.setCancelled(true);
                        SchedulerUtil.runForPlayerLater(player, player::updateInventory, 1L);
                        return;
                    }
                }

                if (cursorEmpty) {
                    event.setCancelled(true);
                    SchedulerUtil.runForPlayerLater(player, player::updateInventory, 1L);
                    return;
                }
            }
        }

        if (isArmorSlotClick || event.isShiftClick()) {
            SchedulerUtil.runForPlayerLater(player, () -> {
                packetSender.sendCosmeticUpdate(player);
            }, 1L);
        }
    }

    private boolean isArmorSlot(int slot) {
        return slot >= 36 && slot <= 39;
    }

    private boolean isCosmeticOnlySlot(Player player, PlayerData data, int slot) {
        ItemStack current = player.getInventory().getItem(slot);
        if (current != null && current.getType() != Material.AIR) {
            return false;
        }

        return switch (slot) {
            case 39 -> data.getEquipped(CosmeticCategory.HAT).isPresent()
                    || data.getEquipped(CosmeticCategory.MASK).isPresent();
            case 38 -> data.getEquipped(CosmeticCategory.WINGS).isPresent()
                    || data.getEquipped(CosmeticCategory.TOP).isPresent();
            case 37 -> data.getEquipped(CosmeticCategory.PANTS).isPresent();
            case 36 -> data.getEquipped(CosmeticCategory.SHOES).isPresent();
            default -> false;
        };
    }
}
