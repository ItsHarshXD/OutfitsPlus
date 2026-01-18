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

public final class FeetSlotRenderer implements SlotRenderer {

    private final CosmeticRegistry registry;

    public FeetSlotRenderer(CosmeticRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Equipment render(RenderContext context, Equipment original, PlayerData targetData) {
        if (original.getSlot() != EquipmentSlot.BOOTS) {
            return original;
        }

        Optional<String> shoesId = targetData.getEquipped(CosmeticCategory.SHOES);

        if (shoesId.isPresent()) {
            return renderCosmetic(context, shoesId.get(), original);
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
