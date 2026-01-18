package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ListCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final CosmeticRegistry registry;
    private final PlayerDataCache playerCache;

    public ListCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.registry = plugin.getCosmeticRegistry();
        this.playerCache = plugin.getPlayerCache();
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ls", "l");
    }

    @Override
    public String getDescription() {
        return "List available cosmetics";
    }

    @Override
    public String getUsage() {
        return "/outfits list [category]";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.command.list";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData data = playerCache.getOrLoad(player.getUniqueId());

        if (args.length == 0) {
            listAllCategories(sender, player, data);
        } else {
            Optional<CosmeticCategory> categoryOpt = CosmeticCategory.fromString(args[0]);
            if (categoryOpt.isEmpty()) {
                localeManager.sendMessage(sender, MessageKey.INVALID_CATEGORY,
                        "category", args[0],
                        "valid", CosmeticCategory.getValidCategories());
                return;
            }
            listCategory(sender, player, data, categoryOpt.get());
        }
    }

    private void listAllCategories(CommandSender sender, Player player, PlayerData data) {
        localeManager.sendRawMessage(sender, MessageKey.LIST_HEADER_ALL);

        boolean hasAny = false;
        for (CosmeticCategory category : CosmeticCategory.values()) {
            List<Cosmetic> cosmetics = registry.getByCategory(category);
            if (!cosmetics.isEmpty()) {
                hasAny = true;
                sender.sendMessage(ColorUtil.processToComponent("&6" + category.name() + ":"));
                for (Cosmetic cosmetic : cosmetics) {
                    sendCosmeticLine(sender, player, data, cosmetic);
                }
                sender.sendMessage(ColorUtil.processToComponent(""));
            }
        }

        if (!hasAny) {
            localeManager.sendRawMessage(sender, MessageKey.LIST_EMPTY);
        }

        localeManager.sendRawMessage(sender, MessageKey.LIST_FOOTER);
    }

    private void listCategory(CommandSender sender, Player player, PlayerData data, CosmeticCategory category) {
        localeManager.sendRawMessage(sender, MessageKey.LIST_HEADER, "category", category.name());

        List<Cosmetic> cosmetics = registry.getByCategory(category);
        if (cosmetics.isEmpty()) {
            localeManager.sendRawMessage(sender, MessageKey.LIST_EMPTY);
        } else {
            for (Cosmetic cosmetic : cosmetics) {
                sendCosmeticLine(sender, player, data, cosmetic);
            }
        }

        localeManager.sendRawMessage(sender, MessageKey.LIST_FOOTER);
    }

    private void sendCosmeticLine(CommandSender sender, Player player, PlayerData data, Cosmetic cosmetic) {
        String name = localeManager.getCosmeticName(sender, cosmetic.id(), cosmetic.category().name());
        String description = localeManager.getCosmeticDescription(sender, cosmetic.id(), cosmetic.category().name());

        boolean isUnlocked = player.hasPermission("outfitsplus.bypass.unlock") ||
                cosmetic.defaultUnlocked() ||
                data.hasUnlocked(cosmetic.id());

        boolean isEquipped = data.getEquipped(cosmetic.category())
                .map(id -> id.equals(cosmetic.id()))
                .orElse(false);

        if (isEquipped) {
            localeManager.sendRawMessage(sender, MessageKey.LIST_ITEM_EQUIPPED, "name", name);
        } else if (isUnlocked) {
            localeManager.sendRawMessage(sender, MessageKey.LIST_ITEM_UNLOCKED,
                    "name", name,
                    "description", description);
        } else {
            localeManager.sendRawMessage(sender, MessageKey.LIST_ITEM_LOCKED,
                    "name", name,
                    "description", description);
        }
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
        return List.of();
    }
}
