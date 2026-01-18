package dev.harsh.plugin.outfitsplus.render.slot;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import dev.harsh.plugin.outfitsplus.api.event.CosmeticRenderEvent;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.RenderContext;
import dev.harsh.plugin.outfitsplus.util.ItemBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;

import java.util.Optional;

public final class ChestSlotRenderer implements SlotRenderer {

    private final CosmeticRegistry registry;

    public ChestSlotRenderer(CosmeticRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Equipment render(RenderContext context, Equipment original, PlayerData targetData) {
        if (original.getSlot() != EquipmentSlot.CHEST_PLATE) {
            return original;
        }

        boolean hasElytra = isElytra(original.getItem());

        Optional<String> wingsId = targetData.getEquipped(CosmeticCategory.WINGS);
        Optional<String> topId = targetData.getEquipped(CosmeticCategory.TOP);

        if (wingsId.isPresent() && !hasElytra) {
            return renderCosmetic(context, wingsId.get(), original);
        } else if (topId.isPresent() && !hasElytra) {
            return renderCosmetic(context, topId.get(), original);
        }

        return original;
    }

    private boolean isElytra(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        return item.getType() == ItemTypes.ELYTRA;
    }

    private Equipment renderCosmetic(RenderContext context, String cosmeticId, Equipment original) {
        return registry.get(cosmeticId)
                .map(cosmetic -> {
                    CosmeticRenderEvent event = new CosmeticRenderEvent(
                            context.viewer(),
                            context.target(),
                            cosmetic
                    );
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return original;
                    }
                    ItemStack cosmeticItem = buildCosmeticItem(cosmetic);
                    return new Equipment(original.getSlot(), cosmeticItem);
                })
                .orElse(original);
    }

    private ItemStack buildCosmeticItem(Cosmetic cosmetic) {
        org.bukkit.inventory.ItemStack bukkitItem = ItemBuilder.of(cosmetic.baseMaterial())
                .customModelData(cosmetic.customModelData())
                .build();

        return SpigotConversionUtil.fromBukkitItemStack(bukkitItem);
    }
}
