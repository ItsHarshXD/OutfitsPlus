package dev.harsh.plugin.outfitsplus.command.subcommand;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import dev.harsh.plugin.outfitsplus.util.ColorUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class HelpCommand implements SubCommand {

    private final OutfitsPlus plugin;
    private final LocaleManager localeManager;

    public HelpCommand(OutfitsPlus plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public List<String> getAliases() {
        return List.of("?");
    }

    @Override
    public String getDescription() {
        return "Show help information";
    }

    @Override
    public String getUsage() {
        return "/outfits help";
    }

    @Override
    public String getPermission() {
        return "outfitsplus.use";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        localeManager.sendRawMessage(sender, MessageKey.HELP_HEADER);

        sender.sendMessage(ColorUtil.processToComponent("&e/outfits help &7- Show this help menu"));
        sender.sendMessage(ColorUtil.processToComponent("&e/outfits equip <category> <id> &7- Equip a cosmetic"));
        sender.sendMessage(ColorUtil.processToComponent("&e/outfits unequip <category|all> &7- Unequip cosmetic(s)"));
        sender.sendMessage(ColorUtil.processToComponent("&e/outfits list [category] &7- List your cosmetics"));
        sender.sendMessage(ColorUtil.processToComponent("&e/outfits toggle <own|others> &7- Toggle visibility"));
        sender.sendMessage(ColorUtil.processToComponent("&e/outfits locale [locale] &7- View/change language"));
        sender.sendMessage(ColorUtil.processToComponent("&e/outfits info [category] [id] &7- View cosmetic info"));

        if (sender.hasPermission("outfitsplus.admin")) {
            sender.sendMessage(ColorUtil.processToComponent(""));
            sender.sendMessage(ColorUtil.processToComponent("&cAdmin Commands:"));
            sender.sendMessage(ColorUtil.processToComponent("&e/outfits reload &7- Reload configuration"));
            sender.sendMessage(
                    ColorUtil.processToComponent("&e/outfits give <player> <category> <id> &7- Give cosmetic"));
            sender.sendMessage(
                    ColorUtil.processToComponent("&e/outfits take <player> <category> <id> &7- Take cosmetic"));
            sender.sendMessage(ColorUtil.processToComponent("&e/outfits reset <player> &7- Reset player data"));
        }

        localeManager.sendRawMessage(sender, MessageKey.HELP_FOOTER);
    }
}
