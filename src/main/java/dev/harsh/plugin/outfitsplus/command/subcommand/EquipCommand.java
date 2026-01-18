package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.api.event.CosmeticEquipEvent;
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

public final class EquipCommand implements SubCommand {

    private final OutfitsPlus plugin;
    private final LocaleManager localeManager;
    private final CosmeticRegistry registry;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;

    public EquipCommand(OutfitsPlus plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
        this.registry = plugin.getCosmeticRegistry();
        this.playerCache = plugin.getPlayerCache();
        this.packetSender = plugin.getPacketSender();
    }

    @Override
    public String getName() {
        return "equip";
    }

    @Override
    public List<String> getAliases() {
        return List.of("wear", "e");
    }

    @Override
    public String getDescription() {
        return "Equip a cosmetic";
    }

    @Override
    public String getUsage() {
        return "/outfits equip <category> <id>";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.command.equip";
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

        if (!player.hasPermission("outfitsplus.bypass.unlock")) {
            if (!cosmetic.defaultUnlocked() && !data.hasUnlocked(cosmeticId)) {
                localeManager.sendMessage(sender, MessageKey.EQUIP_NOT_UNLOCKED,
                        "cosmetic", localeManager.getCosmeticName(sender, cosmeticId, category.name()));
                return;
            }

            if (!player.hasPermission(cosmetic.getEffectivePermission())) {
                localeManager.sendMessage(sender, MessageKey.EQUIP_NO_PERMISSION);
                return;
            }
        }

        Optional<String> currentEquipped = data.getEquipped(category);
        if (currentEquipped.isPresent() && currentEquipped.get().equals(cosmeticId)) {
            localeManager.sendMessage(sender, MessageKey.EQUIP_ALREADY_EQUIPPED,
                    "cosmetic", localeManager.getCosmeticName(sender, cosmeticId, category.name()));
            return;
        }

        CosmeticEquipEvent event = new CosmeticEquipEvent(player.getUniqueId(), cosmetic);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        data.equip(category, cosmeticId);
        packetSender.sendCosmeticUpdate(player);

        localeManager.sendMessage(sender, MessageKey.EQUIP_SUCCESS,
                "cosmetic", localeManager.getCosmeticName(sender, cosmeticId, category.name()),
                "category", category.name().toLowerCase());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

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
            PlayerData data = playerCache.getOrLoad(player.getUniqueId());

            List<String> suggestions = new ArrayList<>();
            for (Cosmetic cosmetic : registry.getByCategory(category)) {
                if (cosmetic.id().toLowerCase().startsWith(input)) {
                    if (player.hasPermission("outfitsplus.bypass.unlock") ||
                            cosmetic.defaultUnlocked() ||
                            data.hasUnlocked(cosmetic.id())) {
                        suggestions.add(cosmetic.id());
                    }
                }
            }
            return suggestions;
        }

        return List.of();
    }
}
