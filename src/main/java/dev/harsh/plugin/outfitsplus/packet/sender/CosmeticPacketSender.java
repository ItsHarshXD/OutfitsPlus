package dev.harsh.plugin.outfitsplus.packet.sender;

import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.CosmeticRenderer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

/**
 * Handles sending cosmetic equipment changes to players using Bukkit's
 * sendEquipmentChange API.
 */
public final class CosmeticPacketSender {

    private final CosmeticRenderer renderer;

    public CosmeticPacketSender(CosmeticRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Sends cosmetic updates for a target player to all nearby viewers (including
     * themselves).
     * Each viewer will see the target's cosmetics based on their visibility
     * settings.
     */
    public void sendCosmeticUpdate(Player target) {
        // Send to other players
        for (Player viewer : target.getWorld().getPlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            if (!viewer.canSee(target)) {
                continue;
            }

            sendCosmeticUpdateToViewer(target, viewer);
        }

        // Also send to the player themselves if they want to see their own cosmetics
        sendSelfCosmeticUpdate(target);
    }

    /**
     * Sends cosmetic updates for a target player to a specific viewer.
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

    /**
     * Sends cosmetic updates to the player about their own character.
     * Respects the player's "show own cosmetics" visibility setting.
     */
    private void sendSelfCosmeticUpdate(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerData playerData = renderer.getPlayerCache().get(playerId).orElse(null);

        if (playerData == null || !playerData.hasAnythingEquipped()) {
            return;
        }

        // Check if player wants to see their own cosmetics
        if (!playerData.getVisibility().isShowOwnCosmetics()) {
            // Send real equipment to clear any cosmetic visuals
            sendRealEquipment(player);
            return;
        }

        // Send cosmetics to self
        Map<EquipmentSlot, ItemStack> equipment = renderer.buildFullEquipment(playerId, player);

        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            player.sendEquipmentChange(player, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sends the player's real equipment to themselves (clears cosmetic visuals).
     */
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

    /**
     * Internal method to send cosmetic updates to a non-self viewer.
     */
    private void sendCosmeticUpdateToViewer(Player target, Player viewer) {
        Map<EquipmentSlot, ItemStack> equipment = renderer.buildFullEquipment(viewer.getUniqueId(), target);

        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            viewer.sendEquipmentChange(target, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Resyncs all cosmetics for a player - updates what they see on others and what
     * others see on them.
     */
    public void resyncCosmetics(Player player) {
        // Update what others see on this player
        sendCosmeticUpdate(player);

        // Update what this player sees on others
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
