package dev.harsh.plugin.outfitsplus.cosmetic;

import java.util.Arrays;
import java.util.Optional;

public enum MaskType {

    FULL("full"),
    HALF("half"),
    FACE_COVER("face_cover"),
    VISOR("visor");

    private final String configName;

    MaskType(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public static Optional<MaskType> fromString(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name) || type.configName.equalsIgnoreCase(name))
                .findFirst();
    }
}
