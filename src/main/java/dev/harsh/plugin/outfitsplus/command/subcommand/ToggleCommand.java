package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

public final class ToggleCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;

    public ToggleCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.playerCache = plugin.getPlayerCache();
        this.packetSender = plugin.getPacketSender();
    }

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public List<String> getAliases() {
        return List.of("t");
    }

    @Override
    public String getDescription() {
        return "Toggle cosmetic visibility";
    }

    @Override
    public String getUsage() {
        return "/outfits toggle <own|others>";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.command.toggle";
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
        String option = args[0].toLowerCase();

        switch (option) {
            case "own", "self", "me" -> {
                data.getVisibility().toggleOwn();
                data.markDirty();
                if (data.getVisibility().isShowOwnCosmetics()) {
                    localeManager.sendMessage(sender, MessageKey.TOGGLE_OWN_ON);
                } else {
                    localeManager.sendMessage(sender, MessageKey.TOGGLE_OWN_OFF);
                }
                packetSender.resyncCosmetics(player);
            }
            case "others", "other", "players" -> {
                data.getVisibility().toggleOthers();
                data.markDirty();
                if (data.getVisibility().isShowOthersCosmetics()) {
                    localeManager.sendMessage(sender, MessageKey.TOGGLE_OTHERS_ON);
                } else {
                    localeManager.sendMessage(sender, MessageKey.TOGGLE_OTHERS_OFF);
                }
                packetSender.resyncCosmetics(player);
            }
            default -> localeManager.sendMessage(sender, MessageKey.TOGGLE_INVALID);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Stream.of("own", "others")
                    .filter(s -> s.startsWith(input))
                    .toList();
        }
        return List.of();
    }
}
