package kor.toxicity.furniture.api.event;

import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Be called when player right clicks some furniture.
 */
public class FurnitureInteractEvent extends AbstractEvent implements FurnitureEntityEvent, FurniturePlayerEvent {
    private final @NotNull PlayerInteractAtEntityEvent event;
    private final @NotNull Player player;
    private final @NotNull FurnitureEntity entity;
    public FurnitureInteractEvent(@NotNull Player player, @NotNull FurnitureEntity entity, @NotNull PlayerInteractAtEntityEvent event) {
        this.player = player;
        this.entity = entity;
        this.event = event;
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public FurnitureEntity getEntity() {
        return entity;
    }

    /**
     * Returns an original event that calling this event.
     * @return an original event
     * @since 1.0
     */
    public @NotNull PlayerInteractAtEntityEvent getEvent() {
        return event;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
