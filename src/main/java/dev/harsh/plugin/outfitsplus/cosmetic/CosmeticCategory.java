package dev.harsh.plugin.outfitsplus.cosmetic;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Arrays;
import java.util.Optional;

public enum CosmeticCategory {

    HAT("hats", EquipmentSlot.HEAD, Material.LEATHER_HELMET),
    MASK("masks", EquipmentSlot.HEAD, Material.LEATHER_HELMET),
    WINGS("wings", EquipmentSlot.CHEST, Material.ELYTRA),
    TOP("tops", EquipmentSlot.CHEST, Material.LEATHER_CHESTPLATE),
    PANTS("pants", EquipmentSlot.LEGS, Material.LEATHER_LEGGINGS),
    SHOES("shoes", EquipmentSlot.FEET, Material.LEATHER_BOOTS);

    private final String configFolder;
    private final EquipmentSlot equipmentSlot;
    private final Material defaultMaterial;

    CosmeticCategory(String configFolder, EquipmentSlot equipmentSlot, Material defaultMaterial) {
        this.configFolder = configFolder;
        this.equipmentSlot = equipmentSlot;
        this.defaultMaterial = defaultMaterial;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
    }

    public Material getDefaultMaterial() {
        return defaultMaterial;
    }

    public boolean conflictsWith(CosmeticCategory other) {
        return this != other && this.equipmentSlot == other.equipmentSlot;
    }

    public static Optional<CosmeticCategory> fromString(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Arrays.stream(values())
                    .filter(cat -> cat.configFolder.equalsIgnoreCase(name))
                    .findFirst();
        }
    }

    public static Optional<CosmeticCategory> fromEquipmentSlot(EquipmentSlot slot) {
        return Arrays.stream(values())
                .filter(cat -> cat.equipmentSlot == slot)
                .findFirst();
    }

    public static String getValidCategories() {
        return String.join(", ", Arrays.stream(values())
                .map(cat -> cat.name().toLowerCase())
                .toList());
    }
}
