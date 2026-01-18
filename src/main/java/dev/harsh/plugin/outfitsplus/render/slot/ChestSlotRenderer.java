package dev.harsh.plugin.outfitsplus.render.slot;

import dev.harsh.plugin.outfitsplus.api.event.CosmeticRenderEvent;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.RenderContext;
import dev.harsh.plugin.outfitsplus.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class ChestSlotRenderer implements SlotRenderer {

    private final CosmeticRegistry registry;

    public ChestSlotRenderer(CosmeticRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Optional<ItemStack> renderCosmetic(RenderContext context, PlayerData targetData) {
        Player target = Bukkit.getPlayer(context.target());
        if (target == null) {
            return Optional.empty();
        }

        ItemStack chestItem = target.getInventory().getChestplate();
        boolean hasElytra = chestItem != null && chestItem.getType() == Material.ELYTRA;

        if (hasElytra) {
            return Optional.empty();
        }

        Optional<String> wingsId = targetData.getEquipped(CosmeticCategory.WINGS);
        Optional<String> topId = targetData.getEquipped(CosmeticCategory.TOP);

        if (wingsId.isPresent()) {
            return tryRenderCosmetic(context, wingsId.get());
        } else if (topId.isPresent()) {
            return tryRenderCosmetic(context, topId.get());
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
