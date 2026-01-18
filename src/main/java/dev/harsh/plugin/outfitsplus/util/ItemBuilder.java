package dev.harsh.plugin.outfitsplus.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;

public final class ItemBuilder {

    private final ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Sets the display name using a Component (preferred for modern Paper).
     */
    public ItemBuilder name(Component name) {
        if (itemMeta != null) {
            itemMeta.displayName(name);
        }
        return this;
    }

    /**
     * Sets the display name from a raw string with color codes.
     * Uses ColorUtil.processToComponent() for proper formatting.
     */
    public ItemBuilder name(String name) {
        if (itemMeta != null) {
            itemMeta.displayName(ColorUtil.processToComponent(name));
        }
        return this;
    }

    /**
     * Sets the lore using Components (preferred for modern Paper).
     */
    public ItemBuilder lore(List<Component> lore) {
        if (itemMeta != null) {
            itemMeta.lore(lore);
        }
        return this;
    }

    /**
     * Sets the lore from raw strings with color codes.
     * Uses ColorUtil.processToComponents() for proper formatting.
     */
    public ItemBuilder loreStrings(List<String> lore) {
        if (itemMeta != null) {
            itemMeta.lore(ColorUtil.processToComponents(lore));
        }
        return this;
    }

    /**
     * Sets the lore from raw strings with color codes.
     */
    public ItemBuilder loreStrings(String... lore) {
        if (itemMeta != null) {
            itemMeta.lore(ColorUtil.processToComponents(List.of(lore)));
        }
        return this;
    }

    public ItemBuilder customModelData(int data) {
        if (itemMeta != null) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder leatherColor(Color color) {
        if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
            this.itemMeta = leatherMeta;
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
        }
        return this;
    }

    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder from(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }
}
