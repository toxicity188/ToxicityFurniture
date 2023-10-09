package kor.toxicity.furniture.api.event;

import org.bukkit.event.HandlerList;

public interface FurniturePluginEvent {
    HandlerList HANDLER_LIST = new HandlerList();
    static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
