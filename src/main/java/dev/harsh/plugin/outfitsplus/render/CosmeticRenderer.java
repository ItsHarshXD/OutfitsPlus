package dev.harsh.plugin.outfitsplus.render;

import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.render.slot.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class CosmeticRenderer {

    private final CosmeticRegistry registry;
    private final PlayerDataCache playerCache;
    private final Map<EquipmentSlot, SlotRenderer> slotRenderers;

    public CosmeticRenderer(CosmeticRegistry registry, PlayerDataCache playerCache) {
        this.registry = registry;
        this.playerCache = playerCache;
        this.slotRenderers = new EnumMap<>(EquipmentSlot.class);

        registerSlotRenderers();
    }

    private void registerSlotRenderers() {
        slotRenderers.put(EquipmentSlot.HEAD, new HeadSlotRenderer(registry));
        slotRenderers.put(EquipmentSlot.CHEST, new ChestSlotRenderer(registry));
        slotRenderers.put(EquipmentSlot.LEGS, new LegsSlotRenderer(registry));
        slotRenderers.put(EquipmentSlot.FEET, new FeetSlotRenderer(registry));
    }

    /**
     * Gets the cosmetic item to display for a given equipment slot, if any.
     * Returns the cosmetic item if one should be shown, or empty if the real
     * equipment should be used.
     */
    public Optional<ItemStack> getCosmeticForSlot(RenderContext context, EquipmentSlot slot, PlayerData targetData) {
        if (targetData == null || !targetData.hasAnythingEquipped()) {
            return Optional.empty();
        }

        SlotRenderer renderer = slotRenderers.get(slot);
        if (renderer == null) {
            return Optional.empty();
        }

        return renderer.renderCosmetic(context, targetData);
    }

    /**
     * Builds a map of equipment slots to the items that should be displayed for a
     * target player,
     * considering both cosmetics and visibility settings.
     * Returns only slots that should show cosmetics (empty slots with cosmetics
     * equipped).
     */
    public Map<EquipmentSlot, ItemStack> buildCosmeticEquipment(UUID viewerId, Player target) {
        UUID targetId = target.getUniqueId();
        PlayerData viewerData = playerCache.get(viewerId).orElse(null);
        PlayerData targetData = playerCache.get(targetId).orElse(null);

        if (targetData == null || !targetData.hasAnythingEquipped()) {
            return Collections.emptyMap();
        }

        if (viewerData != null && !viewerData.getVisibility().canSee(viewerId, targetId)) {
            return Collections.emptyMap();
        }

        RenderContext context = new RenderContext(viewerId, targetId, target.getEntityId());
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET }) {
            ItemStack realItem = target.getInventory().getItem(slot);

            // Only show cosmetics if the slot is empty (no real armor)
            if (realItem == null || realItem.getType().isAir()) {
                getCosmeticForSlot(context, slot, targetData).ifPresent(cosmeticItem -> {
                    equipment.put(slot, cosmeticItem);
                });
            }
        }

        return equipment;
    }

    /**
     * Builds a map of ALL equipment for a target player as other viewers should see
     * them.
     * Includes both real equipment and cosmetics where applicable.
     */
    public Map<EquipmentSlot, ItemStack> buildFullEquipment(UUID viewerId, Player target) {
        UUID targetId = target.getUniqueId();
        PlayerData viewerData = playerCache.get(viewerId).orElse(null);
        PlayerData targetData = playerCache.get(targetId).orElse(null);

        RenderContext context = new RenderContext(viewerId, targetId, target.getEntityId());
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET }) {
            ItemStack realItem = target.getInventory().getItem(slot);

            // Check if we should show a cosmetic in this slot
            if ((realItem == null || realItem.getType().isAir()) && targetData != null
                    && targetData.hasAnythingEquipped()) {
                if (viewerData == null || viewerData.getVisibility().canSee(viewerId, targetId)) {
                    Optional<ItemStack> cosmetic = getCosmeticForSlot(context, slot, targetData);
                    if (cosmetic.isPresent()) {
                        equipment.put(slot, cosmetic.get());
                        continue;
                    }
                }
            }

            // Use the real item (or air if null)
            equipment.put(slot, realItem != null ? realItem : new ItemStack(org.bukkit.Material.AIR));
        }

        return equipment;
    }

    public PlayerDataCache getPlayerCache() {
        return playerCache;
    }
}
