package dev.harsh.plugin.outfitsplus.api.event;

import dev.harsh.plugin.outfitsplus.cosmetic.Cosmetic;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class CosmeticRenderEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID viewerId;
    private final UUID targetId;
    private final Cosmetic cosmetic;
    private boolean cancelled;

    public CosmeticRenderEvent(UUID viewerId, UUID targetId, Cosmetic cosmetic) {
        this.viewerId = viewerId;
        this.targetId = targetId;
        this.cosmetic = cosmetic;
        this.cancelled = false;
    }

    public UUID getViewerId() {
        return viewerId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Cosmetic getCosmetic() {
        return cosmetic;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
