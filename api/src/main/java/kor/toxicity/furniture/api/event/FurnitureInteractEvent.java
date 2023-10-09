package kor.toxicity.furniture.api.event;

import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Be called when player right clicks some furniture.
 */
public class FurnitureInteractEvent extends FurnitureEvent {
    private final @NotNull PlayerInteractAtEntityEvent event;
    public FurnitureInteractEvent(@NotNull Player who, @NotNull FurnitureEntity furniture, @NotNull PlayerInteractAtEntityEvent event) {
        super(who, furniture);
        this.event = event;
    }

    /**
     * Returns an original event that calling this event.
     * @return an original event
     * @since 1.0
     */
    public @NotNull PlayerInteractAtEntityEvent getEvent() {
        return event;
    }
}
