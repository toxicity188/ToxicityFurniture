package kor.toxicity.furniture.api.event;

import org.bukkit.event.HandlerList;

/**
 * Be called when reload task ends.
 */
public class FurnitureReloadEndEvent extends AbstractEvent {
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
