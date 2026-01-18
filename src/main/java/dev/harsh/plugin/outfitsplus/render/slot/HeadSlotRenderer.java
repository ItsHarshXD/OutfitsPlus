package dev.harsh.plugin.outfitsplus.render.slot;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
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

public final class HeadSlotRenderer implements SlotRenderer {

    private final CosmeticRegistry registry;

    public HeadSlotRenderer(CosmeticRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Equipment render(RenderContext context, Equipment original, PlayerData targetData) {
        if (original.getSlot() != EquipmentSlot.HELMET) {
            return original;
        }

        Optional<String> hatId = targetData.getEquipped(CosmeticCategory.HAT);
        Optional<String> maskId = targetData.getEquipped(CosmeticCategory.MASK);

        if (hatId.isPresent()) {
            return renderCosmetic(context, hatId.get(), original);
        } else if (maskId.isPresent()) {
            return renderCosmetic(context, maskId.get(), original);
        }

        return original;
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
