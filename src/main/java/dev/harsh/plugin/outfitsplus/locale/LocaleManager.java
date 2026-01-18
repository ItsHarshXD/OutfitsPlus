package dev.harsh.plugin.outfitsplus.locale;

import dev.harsh.plugin.outfitsplus.OutfitsPlus;
import dev.harsh.plugin.outfitsplus.config.ReloadManager;
import dev.harsh.plugin.outfitsplus.cosmetic.CosmeticCategory;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class LocaleManager implements ReloadManager.Reloadable {

    private final OutfitsPlus plugin;
    private final File localesFolder;
    private final Map<String, YamlConfiguration> locales = new HashMap<>();
    private final Map<UUID, String> playerLocales = new ConcurrentHashMap<>();
    private String defaultLocale;

    public LocaleManager(OutfitsPlus plugin, String defaultLocale) {
        this.plugin = plugin;
        this.localesFolder = new File(plugin.getDataFolder(), "locales");
        this.defaultLocale = defaultLocale;
    }

    @Override
    public String getName() {
        return "Locales";
    }

    @Override
    public void reload() throws Exception {
        locales.clear();

        if (!localesFolder.exists()) {
            localesFolder.mkdirs();
        }

        saveDefaultLocale("en.yml");

        File[] files = localesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String localeName = file.getName().replace(".yml", "");
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    locales.put(localeName, config);
                    plugin.getLogger().info("Loaded locale: " + localeName);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load locale " + localeName + ": " + e.getMessage());
                }
            }
        }

        if (locales.isEmpty()) {
            throw new Exception("No locales loaded!");
        }

        if (!locales.containsKey(defaultLocale)) {
            defaultLocale = locales.keySet().iterator().next();
            plugin.getLogger().warning("Default locale not found, using: " + defaultLocale);
        }
    }

    private void saveDefaultLocale(String fileName) {
        File file = new File(localesFolder, fileName);
        if (!file.exists()) {
            try (InputStream in = plugin.getResource("locales/" + fileName)) {
                if (in != null) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(in, StandardCharsets.UTF_8));
                    defaultConfig.save(file);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save default locale " + fileName + ": " + e.getMessage());
            }
        }
    }

    public String getMessage(CommandSender sender, MessageKey key) {
        return getMessage(sender, key.getPath());
    }

    public String getMessage(CommandSender sender, String path) {
        String locale = getLocale(sender);
        return getMessage(locale, path);
    }

    public String getMessage(String locale, String path) {
        YamlConfiguration config = locales.getOrDefault(locale, locales.get(defaultLocale));
        if (config == null) {
            return path;
        }

        String message = config.getString(path);
        if (message == null) {
            YamlConfiguration fallback = locales.get(defaultLocale);
            if (fallback != null) {
                message = fallback.getString(path);
            }
        }

        return message != null ? colorize(message) : path;
    }

    public String getMessage(CommandSender sender, MessageKey key, Object... replacements) {
        return getMessage(sender, key.getPath(), replacements);
    }

    public String getMessage(CommandSender sender, String path, Object... replacements) {
        String message = getMessage(sender, path);
        return applyReplacements(message, replacements);
    }

    public String getMessageWithPrefix(CommandSender sender, MessageKey key) {
        String prefix = getMessage(sender, MessageKey.PREFIX);
        String message = getMessage(sender, key);
        return prefix + message;
    }

    public String getMessageWithPrefix(CommandSender sender, MessageKey key, Object... replacements) {
        String prefix = getMessage(sender, MessageKey.PREFIX);
        String message = getMessage(sender, key, replacements);
        return prefix + message;
    }

    public void sendMessage(CommandSender sender, MessageKey key) {
        sender.sendMessage(getMessageWithPrefix(sender, key));
    }

    public void sendMessage(CommandSender sender, MessageKey key, Object... replacements) {
        sender.sendMessage(getMessageWithPrefix(sender, key, replacements));
    }

    public void sendRawMessage(CommandSender sender, MessageKey key) {
        sender.sendMessage(getMessage(sender, key));
    }

    public void sendRawMessage(CommandSender sender, MessageKey key, Object... replacements) {
        sender.sendMessage(getMessage(sender, key, replacements));
    }

    private String applyReplacements(String message, Object... replacements) {
        if (replacements.length % 2 != 0) {
            return message;
        }

        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace("{" + placeholder + "}", value);
        }

        return message;
    }

    public String getLocale(CommandSender sender) {
        if (sender instanceof Player player) {
            return playerLocales.getOrDefault(player.getUniqueId(), defaultLocale);
        }
        return defaultLocale;
    }

    public void setPlayerLocale(UUID playerId, String locale) {
        if (locales.containsKey(locale)) {
            playerLocales.put(playerId, locale);
        }
    }

    public void removePlayerLocale(UUID playerId) {
        playerLocales.remove(playerId);
    }

    public boolean isValidLocale(String locale) {
        return locales.containsKey(locale);
    }

    public Set<String> getAvailableLocales() {
        return locales.keySet();
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String locale) {
        if (locales.containsKey(locale)) {
            this.defaultLocale = locale;
        }
    }

    public String getCosmeticName(CommandSender sender, String cosmeticId, String category) {
        String normalized = category.toLowerCase();
        String key = "cosmetic." + normalized + "." + cosmeticId + ".name";
        String name = getMessage(sender, key);

        if (name.equals(key)) {
            String alt = resolveAlternateCosmeticMessage(sender, cosmeticId, category, "name");
            if (alt != null) {
                return alt;
            }
            return cosmeticId;
        }

        return name;
    }

    public String getCosmeticDescription(CommandSender sender, String cosmeticId, String category) {
        String normalized = category.toLowerCase();
        String key = "cosmetic." + normalized + "." + cosmeticId + ".description";
        String desc = getMessage(sender, key);

        if (desc.equals(key)) {
            String alt = resolveAlternateCosmeticMessage(sender, cosmeticId, category, "description");
            if (alt != null) {
                return alt;
            }
            return "";
        }

        return desc;
    }

    private String resolveAlternateCosmeticMessage(CommandSender sender, String cosmeticId, String category, String field) {
        return CosmeticCategory.fromString(category)
                .map(CosmeticCategory::getConfigFolder)
                .map(folder -> "cosmetic." + folder + "." + cosmeticId + "." + field)
                .map(altKey -> {
                    String alt = getMessage(sender, altKey);
                    return alt.equals(altKey) ? null : alt;
                })
                .orElse(null);
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
