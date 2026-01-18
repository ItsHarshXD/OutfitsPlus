package dev.harsh.plugin.outfitsplus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class ColorUtil {

    // Regex pattern to find hex codes in the format &#rrggbb
    private static final Pattern HEX_PATTERN = Pattern.compile(
        "&#([A-Fa-f0-9]{6})"
    );

    // Regex pattern to find center tags
    private static final Pattern CENTER_PATTERN = Pattern.compile(
        "<center>(.*?)</center>",
        Pattern.CASE_INSENSITIVE
    );

    // Standard Minecraft chat width in pixels (default GUI scale)
    private static final int CHAT_WIDTH = 320;

    // Minecraft character pixel widths (default font)
    private static final Map<Character, Integer> CHAR_WIDTHS = new HashMap<>();

    // Map for legacy char to MiniMessage tag conversion
    private static final Map<Character, String> LEGACY_TO_MINI =
        new HashMap<>();

    static {
        // Initialize character pixel widths for Minecraft's default font
        // Most characters are 6 pixels wide, with some exceptions
        String width2Chars = "!.,:;i|'";
        String width3Chars = "`l";
        String width4Chars = " It[]";
        String width5Chars = "\"()*<>fk{}";
        String width7Chars = "@~";

        for (char c : width2Chars.toCharArray()) CHAR_WIDTHS.put(c, 2);
        for (char c : width3Chars.toCharArray()) CHAR_WIDTHS.put(c, 3);
        for (char c : width4Chars.toCharArray()) CHAR_WIDTHS.put(c, 4);
        for (char c : width5Chars.toCharArray()) CHAR_WIDTHS.put(c, 5);
        for (char c : width7Chars.toCharArray()) CHAR_WIDTHS.put(c, 7);
        // Default width for all other characters is 6 (handled in getCharPixelWidth)

        // Colors
        LEGACY_TO_MINI.put('0', "<black>");
        LEGACY_TO_MINI.put('1', "<dark_blue>");
        LEGACY_TO_MINI.put('2', "<dark_green>");
        LEGACY_TO_MINI.put('3', "<dark_aqua>");
        LEGACY_TO_MINI.put('4', "<dark_red>");
        LEGACY_TO_MINI.put('5', "<dark_purple>");
        LEGACY_TO_MINI.put('6', "<gold>");
        LEGACY_TO_MINI.put('7', "<gray>");
        LEGACY_TO_MINI.put('8', "<dark_gray>");
        LEGACY_TO_MINI.put('9', "<blue>");
        LEGACY_TO_MINI.put('a', "<green>");
        LEGACY_TO_MINI.put('b', "<aqua>");
        LEGACY_TO_MINI.put('c', "<red>");
        LEGACY_TO_MINI.put('d', "<light_purple>");
        LEGACY_TO_MINI.put('e', "<yellow>");
        LEGACY_TO_MINI.put('f', "<white>");

        // Decorations
        LEGACY_TO_MINI.put('k', "<obfuscated>");
        LEGACY_TO_MINI.put('l', "<bold>");
        LEGACY_TO_MINI.put('m', "<strikethrough>");
        LEGACY_TO_MINI.put('n', "<underlined>");
        LEGACY_TO_MINI.put('o', "<italic>");
        LEGACY_TO_MINI.put('r', "<reset>");
    }

    /**
     * Processes a string containing &#rrggbb hex codes and & legacy codes
     * into a standard legacy string (using section symbols §).
     *
     * @param content The raw string with & codes
     * @return The colored string using internal Bukkit/Spigot format
     */
    public static String process(String content) {
        if (content == null) return "";

        // 1. Handle Hex Codes (&#rrggbb)
        Matcher matcher = HEX_PATTERN.matcher(content);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            try {
                String hexCode = matcher.group(1);
                // Convert to Bungee ChatColor object which handles the complex §x§r... encoding
                matcher.appendReplacement(
                    buffer,
                    ChatColor.of("#" + hexCode).toString()
                );
            } catch (IllegalArgumentException ignored) {
                // Fallback if hex is invalid
                matcher.appendReplacement(buffer, matcher.group());
            }
        }
        matcher.appendTail(buffer);

        // 2. Handle Standard Legacy Codes (&c, &l, etc.)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Processes a list of strings containing color codes.
     *
     * @param content List of raw strings
     * @return List of processed legacy strings
     */
    public static List<String> process(List<String> content) {
        if (content == null) return new ArrayList<>();
        return content
            .stream()
            .map(ColorUtil::process)
            .collect(Collectors.toList());
    }

    /**
     * Converts a string containing &#rrggbb hex codes and & legacy codes
     * into an Adventure Component using MiniMessage format.
     * Automatically disables italics to fix Lore/Name defaults.
     *
     * @param content The raw string with & codes
     * @return The Adventure Component (never null)
     */
    public static Component processToComponent(String content) {
        if (content == null || content.isEmpty()) return Component.empty();

        // 1. Replace Hex Codes (&#rrggbb) with MiniMessage format <#rrggbb>
        Matcher matcher = HEX_PATTERN.matcher(content);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, "<#" + hexCode + ">");
        }
        matcher.appendTail(buffer);
        String text = buffer.toString();

        // 2. Replace Legacy Codes (&c and §c) with MiniMessage tags (<red>)
        // Handle both & prefix (config format) and § prefix (already-processed format)
        StringBuilder finalBuilder = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];

            // Check for both & and § prefixes
            if ((current == '&' || current == '§') && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);

                // Handle §x§R§R§G§G§B§B hex format (already processed by ChatColor)
                if (current == '§' && code == 'x' && i + 13 < chars.length) {
                    // Try to parse the hex color: §x§R§R§G§G§B§B
                    StringBuilder hexBuilder = new StringBuilder();
                    boolean validHex = true;
                    int hexIndex = i + 2; // Start after §x

                    for (int j = 0; j < 6; j++) {
                        if (hexIndex < chars.length && chars[hexIndex] == '§') {
                            hexIndex++;
                            if (hexIndex < chars.length) {
                                char hexChar = chars[hexIndex];
                                if (
                                    "0123456789abcdefABCDEF".indexOf(hexChar) >=
                                    0
                                ) {
                                    hexBuilder.append(hexChar);
                                    hexIndex++;
                                } else {
                                    validHex = false;
                                    break;
                                }
                            } else {
                                validHex = false;
                                break;
                            }
                        } else {
                            validHex = false;
                            break;
                        }
                    }

                    if (validHex && hexBuilder.length() == 6) {
                        finalBuilder
                            .append("<#")
                            .append(hexBuilder)
                            .append(">");
                        i = hexIndex - 1; // -1 because the loop will increment
                        continue;
                    }
                }

                if (LEGACY_TO_MINI.containsKey(code)) {
                    finalBuilder.append(LEGACY_TO_MINI.get(code));
                    i++; // Skip the code character
                } else {
                    // Not a valid color code, just append the character
                    finalBuilder.append(current);
                }
            } else {
                finalBuilder.append(current);
            }
        }

        // 3. Deserialize using MiniMessage and Force No-Italics
        // This is crucial for Items/Lore where the default is Italic.
        return MiniMessage.miniMessage()
            .deserialize(finalBuilder.toString())
            .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Converts a list of strings containing color codes into Adventure Components.
     *
     * @param content List of raw strings
     * @return List of Adventure Components
     */
    public static List<Component> processToComponents(List<String> content) {
        if (content == null) return new ArrayList<>();
        return content
            .stream()
            .map(ColorUtil::processToComponent)
            .collect(Collectors.toList());
    }

    /**
     * Runs a visual test of various color patterns and logs the output to the
     * console.
     * Call this method in your plugin's onEnable to verify color parsing.
     */
    public static void runTests() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        List<String> testCases = new ArrayList<>();
        testCases.add("&cStandard Legacy Red");
        testCases.add("&a&lLegacy Green Bold");
        testCases.add("&#FF5555Hex Red");
        testCases.add("&#55FF55&lHex Green Bold");
        testCases.add("&x&yInvalid Codes");
        testCases.add("&#ZZZZZZInvalid Hex");
        // The complex gradient example
        testCases.add(
            "&#C2D7D5&l&o&n&mE&#BCD3D1&l&o&n&mx&#B6CFCD&l&o&n&ma&#B0CBC9&l&o&n&mm&#A9C7C4&l&o&n&mp&#A3C3C0&l&o&n&ml&#9DBFBC&l&o&n&me"
        );

        console.sendMessage(
            process("&8[&eColorUtil&8] &7Running Legacy String Tests:")
        );
        for (String test : testCases) {
            // Log using the legacy string processor
            console.sendMessage(process("&8- &r" + test));
        }

        console.sendMessage(
            process("&8[&eColorUtil&8] &7Running Adventure Component Tests:")
        );
        for (String test : testCases) {
            // Log using the component processor
            // We append the test component to a label to distinguish it in console
            Component testComponent = processToComponent(test);
            console.sendMessage(Component.text("- ").append(testComponent));
        }

        console.sendMessage(process("&8[&eColorUtil&8] &aTests Complete."));
    }

    /**
     * Processes center tags in a message, replacing them with space-padded text for
     * centering.
     * Must be called BEFORE color processing.
     *
     * @param content The raw string that may contain center tags
     * @return The string with center tags replaced by centered text
     */
    public static String processCenterTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        Matcher matcher = CENTER_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String innerContent = matcher.group(1);
            String centered = centerText(innerContent);
            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(centered)
            );
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Centers text by adding space padding on the left.
     *
     * @param text The text to center (may contain color codes)
     * @return The text with space padding for centering
     */
    private static String centerText(String text) {
        int textWidth = getTextPixelWidth(text);
        int padding = (CHAT_WIDTH - textWidth) / 2;

        if (padding <= 0) {
            return text; // Text is already wider than or equal to chat width
        }

        // Space is 4 pixels wide
        int spaceCount = padding / 4;
        return " ".repeat(spaceCount) + text;
    }

    /**
     * Calculates the pixel width of text, ignoring color codes.
     *
     * @param text The text to measure
     * @return The pixel width of the visible text
     */
    private static int getTextPixelWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int width = 0;
        boolean isBold = false;
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // Skip color codes (&#RRGGBB or &x)
            if (c == '&') {
                if (i + 1 < chars.length) {
                    char next = chars[i + 1];
                    if (next == '#' && i + 7 < chars.length) {
                        // Hex code: &#RRGGBB (8 characters total)
                        i += 7;
                        continue;
                    } else if (Character.toLowerCase(next) == 'l') {
                        // Bold formatting
                        isBold = true;
                        i++;
                        continue;
                    } else if (Character.toLowerCase(next) == 'r') {
                        // Reset formatting
                        isBold = false;
                        i++;
                        continue;
                    } else if (
                        "0123456789abcdefkmno".indexOf(
                            Character.toLowerCase(next)
                        ) >=
                        0
                    ) {
                        // Other color/format codes
                        i++;
                        continue;
                    }
                }
            }

            // Calculate character width
            int charWidth = getCharPixelWidth(c);
            if (isBold) {
                charWidth++; // Bold adds 1 pixel to each character
            }
            width += charWidth + 1; // +1 for spacing between characters
        }

        return width;
    }

    /**
     * Gets the pixel width of a single character in Minecraft's default font.
     *
     * @param c The character
     * @return The pixel width (default 6 for most characters)
     */
    private static int getCharPixelWidth(char c) {
        return CHAR_WIDTHS.getOrDefault(c, 6);
    }
}
