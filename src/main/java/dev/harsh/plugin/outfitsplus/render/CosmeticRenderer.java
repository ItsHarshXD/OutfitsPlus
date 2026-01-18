package dev.harsh.plugin.outfitsplus.render;

import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.render.slot.*;
import dev.harsh.plugin.outfitsplus.util.ItemBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Player;
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
        slotRenderers.put(EquipmentSlot.HELMET, new HeadSlotRenderer(registry));
        slotRenderers.put(EquipmentSlot.CHEST_PLATE, new ChestSlotRenderer(registry));
        slotRenderers.put(EquipmentSlot.LEGGINGS, new LegsSlotRenderer(registry));
        slotRenderers.put(EquipmentSlot.BOOTS, new FeetSlotRenderer(registry));
    }

    public List<Equipment> renderCosmetics(RenderContext context, List<Equipment> originalEquipment) {
        PlayerData viewerData = playerCache.get(context.viewer()).orElse(null);
        PlayerData targetData = playerCache.get(context.target()).orElse(null);

        if (targetData == null || !targetData.hasAnythingEquipped()) {
            return originalEquipment;
        }

        if (viewerData != null && !viewerData.getVisibility().canSee(context.viewer(), context.target())) {
            return originalEquipment;
        }

        List<Equipment> modifiedEquipment = new ArrayList<>(originalEquipment.size());

        for (Equipment equipment : originalEquipment) {
            if (equipment.getItem() != null && equipment.getItem().getType() != ItemTypes.AIR) {
                modifiedEquipment.add(equipment);
                continue;
            }

            EquipmentSlot slot = equipment.getSlot();
            SlotRenderer renderer = slotRenderers.get(slot);

            if (renderer != null) {
                Equipment rendered = renderer.render(context, equipment, targetData);
                modifiedEquipment.add(rendered);
            } else {
                modifiedEquipment.add(equipment);
            }
        }

        return modifiedEquipment;
    }

    public List<Equipment> buildCosmeticEquipment(UUID targetId, Player targetPlayer) {
        PlayerData targetData = playerCache.get(targetId).orElse(null);
        if (targetData == null || !targetData.hasAnythingEquipped()) {
            return Collections.emptyList();
        }

        List<Equipment> equipment = new ArrayList<>();

        for (CosmeticCategory category : CosmeticCategory.values()) {
            targetData.getEquipped(category).ifPresent(cosmeticId -> {
                registry.get(cosmeticId).ifPresent(cosmetic -> {
                    com.github.retrooper.packetevents.protocol.item.ItemStack cosmeticItem = buildCosmeticItem(cosmetic);
                    EquipmentSlot slot = toPacketSlot(category.getEquipmentSlot());
                    equipment.add(new Equipment(slot, cosmeticItem));
                });
            });
        }

        return equipment;
    }

    public List<Equipment> buildBaseEquipment(Player targetPlayer) {
        List<Equipment> equipment = new ArrayList<>();

        equipment.add(buildBaseEquipmentForSlot(targetPlayer, EquipmentSlot.HELMET));
        equipment.add(buildBaseEquipmentForSlot(targetPlayer, EquipmentSlot.CHEST_PLATE));
        equipment.add(buildBaseEquipmentForSlot(targetPlayer, EquipmentSlot.LEGGINGS));
        equipment.add(buildBaseEquipmentForSlot(targetPlayer, EquipmentSlot.BOOTS));

        return equipment;
    }

    public List<Equipment> buildFullEquipment(Player targetPlayer, UUID targetId) {
        PlayerData targetData = playerCache.get(targetId).orElse(null);

        List<Equipment> equipment = new ArrayList<>();

        equipment.add(buildEquipmentForSlot(targetPlayer, EquipmentSlot.HELMET, targetData, CosmeticCategory.HAT, CosmeticCategory.MASK));
        equipment.add(buildEquipmentForSlot(targetPlayer, EquipmentSlot.CHEST_PLATE, targetData, CosmeticCategory.WINGS, CosmeticCategory.TOP));
        equipment.add(buildEquipmentForSlot(targetPlayer, EquipmentSlot.LEGGINGS, targetData, CosmeticCategory.PANTS));
        equipment.add(buildEquipmentForSlot(targetPlayer, EquipmentSlot.BOOTS, targetData, CosmeticCategory.SHOES));

        return equipment;
    }

    private Equipment buildEquipmentForSlot(Player player, EquipmentSlot slot, PlayerData data, CosmeticCategory... categories) {
        org.bukkit.inventory.EquipmentSlot bukkitSlot = toBukkitSlot(slot);
        ItemStack realItem = player.getInventory().getItem(bukkitSlot);

        if (data != null) {
            for (CosmeticCategory category : categories) {
                Optional<String> cosmeticId = data.getEquipped(category);
                if (cosmeticId.isPresent()) {
                    Optional<Cosmetic> cosmetic = registry.get(cosmeticId.get());
                    if (cosmetic.isPresent()) {
                        if (category == CosmeticCategory.WINGS || category == CosmeticCategory.TOP) {
                            if (realItem != null && realItem.getType() == org.bukkit.Material.ELYTRA) {
                                break;
                            }
                        }
                        return new Equipment(slot, buildCosmeticItem(cosmetic.get()));
                    }
                }
            }
        }

        com.github.retrooper.packetevents.protocol.item.ItemStack packetItem =
                realItem != null ? SpigotConversionUtil.fromBukkitItemStack(realItem)
                        : com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY;

        return new Equipment(slot, packetItem);
    }

    private Equipment buildBaseEquipmentForSlot(Player player, EquipmentSlot slot) {
        org.bukkit.inventory.EquipmentSlot bukkitSlot = toBukkitSlot(slot);
        ItemStack realItem = player.getInventory().getItem(bukkitSlot);
        com.github.retrooper.packetevents.protocol.item.ItemStack packetItem =
                realItem != null ? SpigotConversionUtil.fromBukkitItemStack(realItem)
                        : com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY;
        return new Equipment(slot, packetItem);
    }

    private com.github.retrooper.packetevents.protocol.item.ItemStack buildCosmeticItem(Cosmetic cosmetic) {
        ItemStack bukkitItem = ItemBuilder.of(cosmetic.baseMaterial())
                .customModelData(cosmetic.customModelData())
                .build();

        return SpigotConversionUtil.fromBukkitItemStack(bukkitItem);
    }

    private EquipmentSlot toPacketSlot(org.bukkit.inventory.EquipmentSlot bukkitSlot) {
        return switch (bukkitSlot) {
            case HEAD -> EquipmentSlot.HELMET;
            case CHEST -> EquipmentSlot.CHEST_PLATE;
            case LEGS -> EquipmentSlot.LEGGINGS;
            case FEET -> EquipmentSlot.BOOTS;
            case HAND -> EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
            default -> EquipmentSlot.HELMET;
        };
    }

    private org.bukkit.inventory.EquipmentSlot toBukkitSlot(EquipmentSlot packetSlot) {
        return switch (packetSlot) {
            case HELMET -> org.bukkit.inventory.EquipmentSlot.HEAD;
            case CHEST_PLATE -> org.bukkit.inventory.EquipmentSlot.CHEST;
            case LEGGINGS -> org.bukkit.inventory.EquipmentSlot.LEGS;
            case BOOTS -> org.bukkit.inventory.EquipmentSlot.FEET;
            case MAIN_HAND -> org.bukkit.inventory.EquipmentSlot.HAND;
            case OFF_HAND -> org.bukkit.inventory.EquipmentSlot.OFF_HAND;
            default -> org.bukkit.inventory.EquipmentSlot.HEAD;
        };
    }
}
