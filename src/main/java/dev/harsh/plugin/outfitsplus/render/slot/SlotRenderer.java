package dev.harsh.plugin.outfitsplus.render.slot;

import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.RenderContext;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface SlotRenderer {

    Optional<ItemStack> renderCosmetic(RenderContext context, PlayerData targetData);
}
