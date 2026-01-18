package dev.harsh.plugin.outfitsplus.packet.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import dev.harsh.plugin.outfitsplus.player.PlayerDataCache;
import dev.harsh.plugin.outfitsplus.render.CosmeticRenderer;
import dev.harsh.plugin.outfitsplus.render.RenderContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public final class EquipmentPacketListener implements PacketListener {

    private final CosmeticRenderer renderer;
    private final PlayerDataCache playerCache;
    private final Map<Integer, UUID> entityIdCache = new ConcurrentHashMap<>();

    public EquipmentPacketListener(CosmeticRenderer renderer, PlayerDataCache playerCache) {
        this.renderer = renderer;
        this.playerCache = playerCache;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) {
            return;
        }

        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }

        WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
        int entityId = wrapper.getEntityId();

        Player target = findPlayerByEntityId(entityId);
        if (target == null) {
            return;
        }

        UUID viewerId = viewer.getUniqueId();
        UUID targetId = target.getUniqueId();

        if (!playerCache.isLoaded(targetId)) {
            return;
        }

        RenderContext context = new RenderContext(viewerId, targetId, entityId);

        List<Equipment> originalEquipment = wrapper.getEquipment();
        List<Equipment> modifiedEquipment = renderer.renderCosmetics(context, originalEquipment);

        if (!originalEquipment.equals(modifiedEquipment)) {
            wrapper.setEquipment(modifiedEquipment);
        }
    }

    private Player findPlayerByEntityId(int entityId) {
        UUID cachedId = entityIdCache.get(entityId);
        if (cachedId != null) {
            Player cached = Bukkit.getPlayer(cachedId);
            if (cached != null && cached.getEntityId() == entityId) {
                return cached;
            }
            entityIdCache.remove(entityId);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getEntityId() == entityId) {
                entityIdCache.put(entityId, player.getUniqueId());
                return player;
            }
        }
        return null;
    }
}
