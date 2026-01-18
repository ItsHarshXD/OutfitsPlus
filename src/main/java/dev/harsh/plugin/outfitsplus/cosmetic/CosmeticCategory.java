package dev.harsh.plugin.outfitsplus.cosmetic;

import org.bukkit.inventory.EquipmentSlot;

import java.util.Arrays;
import java.util.Optional;

public enum CosmeticCategory {

    HAT("hats", EquipmentSlot.HEAD),
    MASK("masks", EquipmentSlot.HEAD),
    WINGS("wings", EquipmentSlot.CHEST),
    TOP("tops", EquipmentSlot.CHEST),
    PANTS("pants", EquipmentSlot.LEGS),
    SHOES("shoes", EquipmentSlot.FEET);

    private final String configFolder;
    private final EquipmentSlot equipmentSlot;

    CosmeticCategory(String configFolder, EquipmentSlot equipmentSlot) {
        this.configFolder = configFolder;
        this.equipmentSlot = equipmentSlot;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public EquipmentSlot getEquipmentSlot() {
        return equipmentSlot;
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
