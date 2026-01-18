package dev.harsh.plugin.outfitsplus.packet.sender;

import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.CosmeticRenderer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * Sends cosmetic equipment packets using Bukkit's sendEquipmentChange API.
 */
public final class CosmeticPacketSender {

    private final CosmeticRenderer renderer;

    public CosmeticPacketSender(CosmeticRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Broadcasts cosmetic updates for a player to all viewers and themselves.
     */
    public void sendCosmeticUpdate(Player target) {
        for (Player viewer : target.getWorld().getPlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            if (!viewer.canSee(target)) {
                continue;
            }
            sendCosmeticUpdateToViewer(target, viewer);
        }

        sendSelfCosmeticUpdate(target);
    }

    /**
     * Sends cosmetic updates for a target to a specific viewer.
     */
    public void sendCosmeticUpdate(Player target, Player viewer) {
        if (viewer.equals(target)) {
            sendSelfCosmeticUpdate(target);
            return;
        }
        if (!viewer.canSee(target)) {
            return;
        }
        sendCosmeticUpdateToViewer(target, viewer);
    }

    private void sendSelfCosmeticUpdate(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData playerData = renderer.getPlayerCache().get(playerId).orElse(null);

        if (playerData == null || !playerData.hasAnythingEquipped()) {
            return;
        }

        if (!playerData.getVisibility().isShowOwnCosmetics()) {
            sendRealEquipment(player);
            return;
        }

        Map<EquipmentSlot, ItemStack> equipment = renderer.buildFullEquipment(playerId, player);
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            player.sendEquipmentChange(player, entry.getKey(), entry.getValue());
        }
    }

    private void sendRealEquipment(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET }) {
            ItemStack realItem = player.getInventory().getItem(slot);
            if (realItem == null) {
                realItem = new ItemStack(org.bukkit.Material.AIR);
            }
            player.sendEquipmentChange(player, slot, realItem);
        }
    }

    private void sendCosmeticUpdateToViewer(Player target, Player viewer) {
        Map<EquipmentSlot, ItemStack> equipment = renderer.buildFullEquipment(viewer.getUniqueId(), target);
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            viewer.sendEquipmentChange(target, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Resyncs cosmetics for a player (what they see and what others see on them).
     */
    public void resyncCosmetics(Player player) {
        sendCosmeticUpdate(player);

        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player) && player.canSee(other)) {
                sendCosmeticUpdateToViewer(other, player);
            }
        }
    }

    /**
     * Resyncs cosmetics for all online players.
     */
    public void resyncAllPlayers() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            sendCosmeticUpdate(player);
        }
    }
}
