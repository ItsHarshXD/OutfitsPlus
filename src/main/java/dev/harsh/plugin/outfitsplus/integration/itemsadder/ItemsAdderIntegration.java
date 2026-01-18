package dev.harsh.plugin.outfitsplus.integration.itemsadder;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.integration.Integration;
import org.bukkit.Bukkit;

public final class ItemsAdderIntegration implements Integration {

    private final OutfitsPlus plugin;

    public ItemsAdderIntegration(OutfitsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "ItemsAdder";
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }

    @Override
    public void enable() {
        plugin.getLogger().info("ItemsAdder integration enabled (stub - full implementation planned)");
    }

    @Override
    public void disable() {
    }

    @Override
    public void reload() {
    }
}
