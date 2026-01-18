package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class LocaleCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final PlayerDataCache playerCache;

    public LocaleCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.playerCache = plugin.getPlayerCache();
    }

    @Override
    public String getName() {
        return "locale";
    }

    @Override
    public List<String> getAliases() {
        return List.of("lang", "language");
    }

    @Override
    public String getDescription() {
        return "View or change your locale";
    }

    @Override
    public String getUsage() {
        return "/outfits locale [locale]";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.command.locale";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            String currentLocale = localeManager.getLocale(sender);
            localeManager.sendMessage(sender, MessageKey.LOCALE_CURRENT, "locale", currentLocale);
            return;
        }

        String newLocale = args[0].toLowerCase();

        if (!localeManager.isValidLocale(newLocale)) {
            String available = String.join(", ", localeManager.getAvailableLocales());
            localeManager.sendMessage(sender, MessageKey.LOCALE_INVALID,
                    "locale", newLocale,
                    "available", available);
            return;
        }

        PlayerData data = playerCache.getOrLoad(player.getUniqueId());
        data.setLocale(newLocale);
        localeManager.setPlayerLocale(player.getUniqueId(), newLocale);

        localeManager.sendMessage(sender, MessageKey.LOCALE_CHANGED, "locale", newLocale);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return localeManager.getAvailableLocales().stream()
                    .filter(locale -> locale.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
