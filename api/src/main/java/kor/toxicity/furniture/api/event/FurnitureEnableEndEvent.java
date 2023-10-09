package kor.toxicity.furniture.api.event;

import org.bukkit.event.HandlerList;

/**
 * Be called when plugin is completely enabled.
 */
public class FurnitureEnableEndEvent extends AbstractEvent {
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
