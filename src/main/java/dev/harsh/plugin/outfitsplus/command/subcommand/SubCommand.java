package dev.harsh.plugin.outfitsplus.command.subcommand;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface SubCommand {

    String getName();

    default List<String> getAliases() {
        return Collections.emptyList();
    }

    String getDescription();

    String getUsage();

    String getPermission();

    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    default boolean isPlayerOnly() {
        return false;
    }
}
