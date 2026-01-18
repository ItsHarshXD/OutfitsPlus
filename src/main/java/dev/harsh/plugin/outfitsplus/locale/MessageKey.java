package dev.harsh.plugin.outfitsplus.locale;

public enum MessageKey {

    // General
    PREFIX("prefix"),
    RELOAD_SUCCESS("messages.reload-success"),
    RELOAD_FAILED("messages.reload-failed"),
    NO_PERMISSION("messages.no-permission"),
    PLAYER_NOT_FOUND("messages.player-not-found"),
    PLAYER_ONLY("messages.player-only"),
    INVALID_CATEGORY("messages.invalid-category"),
    INVALID_COSMETIC("messages.invalid-cosmetic"),
    INVALID_USAGE("messages.invalid-usage"),

    // Equip command
    EQUIP_SUCCESS("commands.equip.success"),
    EQUIP_ALREADY_EQUIPPED("commands.equip.already-equipped"),
    EQUIP_NOT_UNLOCKED("commands.equip.not-unlocked"),
    EQUIP_NO_PERMISSION("commands.equip.no-permission"),

    // Unequip command
    UNEQUIP_SUCCESS("commands.unequip.success"),
    UNEQUIP_SUCCESS_ALL("commands.unequip.success-all"),
    UNEQUIP_NOTHING_EQUIPPED("commands.unequip.nothing-equipped"),

    // List command
    LIST_HEADER("commands.list.header"),
    LIST_HEADER_ALL("commands.list.header-all"),
    LIST_ITEM_UNLOCKED("commands.list.item-unlocked"),
    LIST_ITEM_LOCKED("commands.list.item-locked"),
    LIST_ITEM_EQUIPPED("commands.list.item-equipped"),
    LIST_EMPTY("commands.list.empty"),
    LIST_FOOTER("commands.list.footer"),

    // Toggle command
    TOGGLE_OWN_ON("commands.toggle.own-on"),
    TOGGLE_OWN_OFF("commands.toggle.own-off"),
    TOGGLE_OTHERS_ON("commands.toggle.others-on"),
    TOGGLE_OTHERS_OFF("commands.toggle.others-off"),
    TOGGLE_INVALID("commands.toggle.invalid"),

    // Locale command
    LOCALE_CHANGED("commands.locale.changed"),
    LOCALE_INVALID("commands.locale.invalid"),
    LOCALE_CURRENT("commands.locale.current"),

    // Info command
    INFO_HEADER("commands.info.header"),
    INFO_NAME("commands.info.name"),
    INFO_CATEGORY("commands.info.category"),
    INFO_DESCRIPTION("commands.info.description"),
    INFO_UNLOCKED("commands.info.unlocked"),
    INFO_EQUIPPED("commands.info.equipped"),
    INFO_PERMISSION("commands.info.permission"),
    INFO_STATUS_YES("commands.info.status-yes"),
    INFO_STATUS_NO("commands.info.status-no"),

    // Admin commands
    ADMIN_GIVE_SUCCESS("admin.give.success"),
    ADMIN_GIVE_ALREADY_HAS("admin.give.already-has"),
    ADMIN_TAKE_SUCCESS("admin.take.success"),
    ADMIN_TAKE_NOT_UNLOCKED("admin.take.not-unlocked"),
    ADMIN_RESET_SUCCESS("admin.reset.success"),
    ADMIN_RESET_CONFIRM("admin.reset.confirm"),

    // Help
    HELP_HEADER("help.header"),
    HELP_FOOTER("help.footer");

    private final String path;

    MessageKey(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
