package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.api.event.CosmeticUnequipEvent;
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

public final class UnequipCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;
    private final CosmeticRegistry registry;

    public UnequipCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.playerCache = plugin.getPlayerCache();
        this.packetSender = plugin.getPacketSender();
        this.registry = plugin.getCosmeticRegistry();
    }

    @Override
    public String getName() {
        return "unequip";
    }

    @Override
    public List<String> getAliases() {
        return List.of("remove", "u");
    }

    @Override
    public String getDescription() {
        return "Unequip a cosmetic";
    }

    @Override
    public String getUsage() {
        return "/outfits unequip <category|all>";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.command.unequip";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            localeManager.sendMessage(sender, MessageKey.INVALID_USAGE, "usage", getUsage());
            return;
        }

        PlayerData data = playerCache.getOrLoad(player.getUniqueId());

        if (args[0].equalsIgnoreCase("all")) {
            if (!data.hasAnythingEquipped()) {
                localeManager.sendMessage(sender, MessageKey.UNEQUIP_NOTHING_EQUIPPED, "category", "any");
                return;
            }

            boolean unequippedAny = false;
            for (CosmeticCategory category : CosmeticCategory.values()) {
                Optional<String> equipped = data.getEquipped(category);
                if (equipped.isPresent()) {
                    boolean cancelled = registry.get(equipped.get())
                            .map(cosmetic -> {
                                CosmeticUnequipEvent event = new CosmeticUnequipEvent(player.getUniqueId(), cosmetic);
                                Bukkit.getPluginManager().callEvent(event);
                                return event.isCancelled();
                            })
                            .orElse(false);

                    if (!cancelled) {
                        data.unequip(category);
                        unequippedAny = true;
                    }
                }
            }

            if (unequippedAny) {
                packetSender.sendCosmeticUpdate(player);
                localeManager.sendMessage(sender, MessageKey.UNEQUIP_SUCCESS_ALL);
            } else {
                localeManager.sendMessage(sender, MessageKey.UNEQUIP_NOTHING_EQUIPPED, "category", "any");
            }
            return;
        }

        Optional<CosmeticCategory> categoryOpt = CosmeticCategory.fromString(args[0]);
        if (categoryOpt.isEmpty()) {
            localeManager.sendMessage(sender, MessageKey.INVALID_CATEGORY,
                    "category", args[0],
                    "valid", CosmeticCategory.getValidCategories() + ", all");
            return;
        }

        CosmeticCategory category = categoryOpt.get();

        if (!data.hasEquipped(category)) {
            localeManager.sendMessage(sender, MessageKey.UNEQUIP_NOTHING_EQUIPPED,
                    "category", category.name().toLowerCase());
            return;
        }

        Optional<String> equipped = data.getEquipped(category);
        if (equipped.isPresent()) {
            boolean cancelled = registry.get(equipped.get())
                    .map(cosmetic -> {
                        CosmeticUnequipEvent event = new CosmeticUnequipEvent(player.getUniqueId(), cosmetic);
                        Bukkit.getPluginManager().callEvent(event);
                        return event.isCancelled();
                    })
                    .orElse(false);

            if (cancelled) {
                return;
            }
        }

        data.unequip(category);
        packetSender.sendCosmeticUpdate(player);
        localeManager.sendMessage(sender, MessageKey.UNEQUIP_SUCCESS,
                "category", category.name().toLowerCase());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            if ("all".startsWith(input)) {
                suggestions.add("all");
            }

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
