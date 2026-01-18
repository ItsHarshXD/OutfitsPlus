package dev.harsh.plugin.outfitsplus.render.slot;

import com.github.retrooper.packetevents.protocol.player.Equipment;
import dev.harsh.plugin.outfitsplus.player.PlayerData;
import dev.harsh.plugin.outfitsplus.render.RenderContext;

public interface SlotRenderer {

    Equipment render(RenderContext context, Equipment original, PlayerData targetData);
}
