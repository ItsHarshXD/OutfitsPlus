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

public final class LegsSlotRenderer implements SlotRenderer {

    private final CosmeticRegistry registry;

    public LegsSlotRenderer(CosmeticRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Optional<ItemStack> renderCosmetic(RenderContext context, PlayerData targetData) {
        Optional<String> pantsId = targetData.getEquipped(CosmeticCategory.PANTS);

        if (pantsId.isPresent()) {
            return tryRenderCosmetic(context, pantsId.get());
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
