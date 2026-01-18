package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InfoCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final CosmeticRegistry registry;
    private final PlayerDataCache playerCache;

    public InfoCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.registry = plugin.getCosmeticRegistry();
        this.playerCache = plugin.getPlayerCache();
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public List<String> getAliases() {
        return List.of("i");
    }

    @Override
    public String getDescription() {
        return "View cosmetic information";
    }

    @Override
    public String getUsage() {
        return "/outfits info <category> <id>";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.command.info";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 2) {
            localeManager.sendMessage(sender, MessageKey.INVALID_USAGE, "usage", getUsage());
            return;
        }

        Optional<CosmeticCategory> categoryOpt = CosmeticCategory.fromString(args[0]);
        if (categoryOpt.isEmpty()) {
            localeManager.sendMessage(sender, MessageKey.INVALID_CATEGORY,
                    "category", args[0],
                    "valid", CosmeticCategory.getValidCategories());
            return;
        }

        CosmeticCategory category = categoryOpt.get();
        String cosmeticId = args[1].toLowerCase();

        Optional<Cosmetic> cosmeticOpt = registry.get(cosmeticId);
        if (cosmeticOpt.isEmpty() || cosmeticOpt.get().category() != category) {
            localeManager.sendMessage(sender, MessageKey.INVALID_COSMETIC,
                    "cosmetic", cosmeticId,
                    "category", category.name().toLowerCase());
            return;
        }

        Cosmetic cosmetic = cosmeticOpt.get();
        PlayerData data = playerCache.getOrLoad(player.getUniqueId());

        String name = localeManager.getCosmeticName(sender, cosmetic.id(), category.name());
        String description = localeManager.getCosmeticDescription(sender, cosmetic.id(), category.name());

        boolean isUnlocked = player.hasPermission("outfitsplus.bypass.unlock") ||
                cosmetic.defaultUnlocked() ||
                data.hasUnlocked(cosmetic.id());

        boolean isEquipped = data.getEquipped(category)
                .map(id -> id.equals(cosmetic.id()))
                .orElse(false);

        String statusYes = localeManager.getMessage(sender, MessageKey.INFO_STATUS_YES);
        String statusNo = localeManager.getMessage(sender, MessageKey.INFO_STATUS_NO);

        localeManager.sendRawMessage(sender, MessageKey.INFO_HEADER);
        localeManager.sendRawMessage(sender, MessageKey.INFO_NAME, "name", name);
        localeManager.sendRawMessage(sender, MessageKey.INFO_CATEGORY, "category", category.name().toLowerCase());
        localeManager.sendRawMessage(sender, MessageKey.INFO_DESCRIPTION, "description", description);
        localeManager.sendRawMessage(sender, MessageKey.INFO_UNLOCKED, "status", isUnlocked ? statusYes : statusNo);
        localeManager.sendRawMessage(sender, MessageKey.INFO_EQUIPPED, "status", isEquipped ? statusYes : statusNo);
        localeManager.sendRawMessage(sender, MessageKey.INFO_PERMISSION, "permission", cosmetic.getEffectivePermission());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (CosmeticCategory category : CosmeticCategory.values()) {
                if (category.name().toLowerCase().startsWith(input)) {
                    suggestions.add(category.name().toLowerCase());
                }
            }
            return suggestions;
        }

        if (args.length == 2) {
            Optional<CosmeticCategory> categoryOpt = CosmeticCategory.fromString(args[0]);
            if (categoryOpt.isEmpty()) {
                return List.of();
            }

            CosmeticCategory category = categoryOpt.get();
            String input = args[1].toLowerCase();

            List<String> suggestions = new ArrayList<>();
            for (Cosmetic cosmetic : registry.getByCategory(category)) {
                if (cosmetic.id().toLowerCase().startsWith(input)) {
                    suggestions.add(cosmetic.id());
                }
            }
            return suggestions;
        }

        return List.of();
    }
}
