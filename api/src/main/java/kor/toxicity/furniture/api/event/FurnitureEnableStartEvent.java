package kor.toxicity.furniture.api.event;

import org.bukkit.event.HandlerList;

/**
 * Be called when plugin starts enabling.
 */
public class FurnitureEnableStartEvent extends AbstractEvent {
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
