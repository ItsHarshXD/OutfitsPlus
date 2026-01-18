package dev.harsh.plugin.outfitsplus.packet.sender;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import dev.harsh.plugin.outfitsplus.render.CosmeticRenderer;
import org.bukkit.entity.Player;

import java.util.List;

public final class CosmeticPacketSender {

    private final CosmeticRenderer renderer;

    public CosmeticPacketSender(CosmeticRenderer renderer) {
        this.renderer = renderer;
    }

    public void sendCosmeticUpdate(Player target) {
        List<Equipment> baseEquipment = renderer.buildBaseEquipment(target);

        for (Player viewer : target.getWorld().getPlayers()) {
            if (viewer.equals(target)) {
                continue;
            }
            if (!viewer.canSee(target)) {
                continue;
            }

            List<Equipment> equipment = renderer.renderCosmetics(
                    new dev.harsh.plugin.outfitsplus.render.RenderContext(
                            viewer.getUniqueId(),
                            target.getUniqueId(),
                            target.getEntityId()
                    ),
                    baseEquipment
            );

            WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(
                    target.getEntityId(),
                    equipment
            );

            sendPacket(viewer, packet);
        }
    }

    public void sendCosmeticUpdate(Player target, Player viewer) {
        if (viewer.equals(target)) {
            return;
        }
        if (!viewer.canSee(target)) {
            return;
        }

        List<Equipment> baseEquipment = renderer.buildBaseEquipment(target);
        List<Equipment> equipment = renderer.renderCosmetics(
            new dev.harsh.plugin.outfitsplus.render.RenderContext(
                viewer.getUniqueId(),
                target.getUniqueId(),
                target.getEntityId()
            ),
            baseEquipment
        );

        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(
            target.getEntityId(),
            equipment
        );

        sendPacket(viewer, packet);
    }

    public void resyncCosmetics(Player player) {
        sendCosmeticUpdate(player);

        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player) && player.canSee(other)) {
                sendCosmeticUpdate(other, player);
            }
        }
    }

    public void resyncAllPlayers() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            sendCosmeticUpdate(player);
        }
    }

    private void sendPacket(Player player, WrapperPlayServerEntityEquipment packet) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user != null) {
            user.sendPacket(packet);
        }
    }
}
