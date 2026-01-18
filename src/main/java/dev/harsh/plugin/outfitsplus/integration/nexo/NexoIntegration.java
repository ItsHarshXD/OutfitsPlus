package dev.harsh.plugin.outfitsplus.integration.nexo;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.integration.Integration;
import org.bukkit.Bukkit;

public final class NexoIntegration implements Integration {

    private final OutfitsPlus plugin;

    public NexoIntegration(OutfitsPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Nexo";
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("Nexo") != null;
    }

    @Override
    public void enable() {
        plugin.getLogger().info("Nexo integration enabled (stub - full implementation planned)");
    }

    @Override
    public void disable() {
    }

    @Override
    public void reload() {
    }
}
