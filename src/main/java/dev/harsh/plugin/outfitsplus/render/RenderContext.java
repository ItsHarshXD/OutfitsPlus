package dev.harsh.plugin.outfitsplus.render;

import java.util.UUID;

public record RenderContext(
        UUID viewer,
        UUID target,
        int entityId
) {

    public boolean isSelf() {
        return viewer.equals(target);
    }
}
