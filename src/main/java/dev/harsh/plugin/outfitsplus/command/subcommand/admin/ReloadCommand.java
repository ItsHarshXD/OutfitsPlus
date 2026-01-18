package dev.harsh.plugin.outfitsplus.command.subcommand.admin;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.command.subcommand.SubCommand;
import dev.harsh.plugin.outfitsplus.config.ReloadManager;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class ReloadCommand implements SubCommand {

    private final OutfitsPlus plugin;
    private final LocaleManager localeManager;
    private final ReloadManager reloadManager;

    public ReloadCommand(OutfitsPlus plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
        this.reloadManager = plugin.getReloadManager();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rl");
    }

    @Override
    public String getDescription() {
        return "Reload configuration files";
    }

    @Override
    public String getUsage() {
        return "/outfits reload";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.admin.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ReloadManager.ReloadResult result = reloadManager.reload();

        if (result.isFullSuccess()) {
            localeManager.sendMessage(sender, MessageKey.RELOAD_SUCCESS);
        } else {
            StringBuilder errors = new StringBuilder();
            for (var entry : result.getFailures().entrySet()) {
                errors.append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("; ");
            }
            localeManager.sendMessage(sender, MessageKey.RELOAD_FAILED, "error", errors.toString());
        }

        plugin.getPacketSender().resyncAllPlayers();
    }
}
