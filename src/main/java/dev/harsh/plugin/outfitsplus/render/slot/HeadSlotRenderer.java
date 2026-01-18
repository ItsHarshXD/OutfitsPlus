package dev.harsh.plugin.outfitsplus.render.slot;

import dev.harsh.plugin.outfitsplus.api.event.CosmeticRenderEvent;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.RenderContext;
import dev.harsh.plugin.outfitsplus.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class HeadSlotRenderer implements SlotRenderer {

    private final CosmeticRegistry registry;

    public HeadSlotRenderer(CosmeticRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Optional<ItemStack> renderCosmetic(RenderContext context, PlayerData targetData) {
        Optional<String> hatId = targetData.getEquipped(CosmeticCategory.HAT);
        Optional<String> maskId = targetData.getEquipped(CosmeticCategory.MASK);

        if (hatId.isPresent()) {
            return tryRenderCosmetic(context, hatId.get());
        } else if (maskId.isPresent()) {
            return tryRenderCosmetic(context, maskId.get());
        }

        return Optional.empty();
    }

    private Optional<ItemStack> tryRenderCosmetic(RenderContext context, String cosmeticId) {
        return registry.get(cosmeticId)
                .flatMap(cosmetic -> {
                    CosmeticRenderEvent event = new CosmeticRenderEvent(
                            context.viewer(),
                            context.target(),
                            cosmetic);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return Optional.empty();
                    }
                    return Optional.of(buildCosmeticItem(cosmetic));
                });
    }

    private ItemStack buildCosmeticItem(Cosmetic cosmetic) {
        return ItemBuilder.of(cosmetic.baseMaterial())
                .customModelData(cosmetic.customModelData())
                .build();
    }
}
