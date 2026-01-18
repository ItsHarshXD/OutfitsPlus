package dev.harsh.plugin.outfitsplus.command.subcommand.admin;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.command.subcommand.SubCommand;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.packet.sender.CosmeticPacketSender;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ResetCommand implements SubCommand {

    private final LocaleManager localeManager;
    private final PlayerDataCache playerCache;
    private final CosmeticPacketSender packetSender;
    private final Map<UUID, Long> confirmations = new HashMap<>();

    public ResetCommand(OutfitsPlus plugin) {
        this.localeManager = plugin.getLocaleManager();
        this.playerCache = plugin.getPlayerCache();
        this.packetSender = plugin.getPacketSender();
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public List<String> getAliases() {
        return List.of("clear");
    }

    @Override
    public String getDescription() {
        return "Reset a player's cosmetic data";
    }

    @Override
    public String getUsage() {
        return "/outfits reset <player>";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.admin.reset";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            localeManager.sendMessage(sender, MessageKey.INVALID_USAGE, "usage", getUsage());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            localeManager.sendMessage(sender, MessageKey.PLAYER_NOT_FOUND, "player", args[0]);
            return;
        }

        UUID senderId = sender instanceof Player player ? player.getUniqueId() : new UUID(0, 0);
        Long lastConfirm = confirmations.get(senderId);
        long now = System.currentTimeMillis();

        if (lastConfirm == null || now - lastConfirm > 10000) {
            confirmations.put(senderId, now);
            localeManager.sendMessage(sender, MessageKey.ADMIN_RESET_CONFIRM);
            return;
        }

        confirmations.remove(senderId);

        PlayerData data = playerCache.getOrLoad(target.getUniqueId());
        data.reset();
        packetSender.sendCosmeticUpdate(target);

        localeManager.sendMessage(sender, MessageKey.ADMIN_RESET_SUCCESS, "player", target.getName());
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
        return List.of();
    }
}
