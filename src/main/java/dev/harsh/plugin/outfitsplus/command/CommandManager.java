package dev.harsh.plugin.outfitsplus.command;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.command.subcommand.*;
import dev.harsh.plugin.outfitsplus.command.subcommand.admin.*;
import dev.harsh.plugin.outfitsplus.locale.LocaleManager;
import dev.harsh.plugin.outfitsplus.locale.MessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public final class CommandManager implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final LocaleManager localeManager;
    private final OutfitsPlus plugin;

    public CommandManager(OutfitsPlus plugin) {
        this.plugin = plugin;
        this.localeManager = plugin.getLocaleManager();
        registerSubCommands();
    }

    private void registerSubCommands() {
        register(new HelpCommand(plugin));
        register(new EquipCommand(plugin));
        register(new UnequipCommand(plugin));
        register(new ListCommand(plugin));
        register(new ToggleCommand(plugin));
        register(new LocaleCommand(plugin));
        register(new InfoCommand(plugin));

        register(new ReloadCommand(plugin));
        register(new GiveCommand(plugin));
        register(new TakeCommand(plugin));
        register(new ResetCommand(plugin));
    }

    private void register(SubCommand cmd) {
        subCommands.put(cmd.getName().toLowerCase(), cmd);
        for (String alias : cmd.getAliases()) {
            subCommands.put(alias.toLowerCase(), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            subCommands.get("help").execute(sender, args);
            return true;
        }

        String subName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subName);

        if (subCommand == null) {
            localeManager.sendMessage(sender, MessageKey.INVALID_USAGE, "usage", "/outfits help");
            return true;
        }

        if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
            localeManager.sendMessage(sender, MessageKey.PLAYER_ONLY);
            return true;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            localeManager.sendMessage(sender, MessageKey.NO_PERMISSION);
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.values().stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .distinct()
                    .toList();
        }

        String subName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subName);
        if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subCommand.tabComplete(sender, subArgs);
        }

        return Collections.emptyList();
    }

    public Collection<SubCommand> getSubCommands() {
        return subCommands.values().stream().distinct().toList();
    }
}
