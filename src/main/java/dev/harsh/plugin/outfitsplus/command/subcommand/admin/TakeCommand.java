package dev.harsh.plugin.outfitsplus.command.subcommand.admin;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.command.subcommand.SubCommand;
import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import dev.harsh.plugin.outfitsplus.cosmetic.registry.CosmeticRegistry;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TakeCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final CosmeticRegistry registry;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;

    public TakeCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.registry = plugin.getCosmeticRegistry();
        this.playerCache = plugin.getPlayerCache();
        this.packetSender = plugin.getPacketSender();
    }

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public List<String> getAliases() {
        return List.of("revoke", "lock");
    }

    @Override
    public String getDescription() {
        return "Take a cosmetic from a player";
    }

    @Override
    public String getUsage() {
        return "/outfits take <player> <category> <id>";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.admin.take";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            localeManager.sendMessage(sender, MessageKey.INVALID_USAGE, "usage", getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            localeManager.sendMessage(sender, MessageKey.PLAYER_NOT_FOUND, "player", args[0]);
            return;
        }

        Optional<CosmeticCategory> categoryOpt = CosmeticCategory.fromString(args[1]);
        if (categoryOpt.isEmpty()) {
            localeManager.sendMessage(sender, MessageKey.INVALID_CATEGORY,
                    "category", args[1],
                    "valid", CosmeticCategory.getValidCategories());
            return;
        }

        CosmeticCategory category = categoryOpt.get();
        String cosmeticId = args[2].toLowerCase();

        Optional<Cosmetic> cosmeticOpt = registry.get(cosmeticId);
        if (cosmeticOpt.isEmpty() || cosmeticOpt.get().category() != category) {
            localeManager.sendMessage(sender, MessageKey.INVALID_COSMETIC,
                    "cosmetic", cosmeticId,
                    "category", category.name().toLowerCase());
            return;
        }

        PlayerData data = playerCache.getOrLoad(target.getUniqueId());

        if (!data.hasUnlocked(cosmeticId)) {
            String name = localeManager.getCosmeticName(sender, cosmeticId, category.name());
            localeManager.sendMessage(sender, MessageKey.ADMIN_TAKE_NOT_UNLOCKED,
                    "player", target.getName(),
                    "cosmetic", name);
            return;
        }

        data.lock(cosmeticId);

        if (data.getEquipped(category).map(id -> id.equals(cosmeticId)).orElse(false)) {
            data.unequip(category);
            packetSender.sendCosmeticUpdate(target);
        }

        String name = localeManager.getCosmeticName(sender, cosmeticId, category.name());
        localeManager.sendMessage(sender, MessageKey.ADMIN_TAKE_SUCCESS,
                "player", target.getName(),
                "cosmetic", name);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList();
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (CosmeticCategory category : CosmeticCategory.values()) {
                if (category.name().toLowerCase().startsWith(input)) {
                    suggestions.add(category.name().toLowerCase());
                }
            }
            return suggestions;
        }

        if (args.length == 3) {
            Optional<CosmeticCategory> categoryOpt = CosmeticCategory.fromString(args[1]);
            if (categoryOpt.isEmpty()) {
                return List.of();
            }

            CosmeticCategory category = categoryOpt.get();
            String input = args[2].toLowerCase();

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
