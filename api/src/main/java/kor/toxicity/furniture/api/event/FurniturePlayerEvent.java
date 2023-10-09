package kor.toxicity.furniture.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface FurniturePlayerEvent extends FurniturePluginEvent {
    @NotNull Player getPlayer();
    static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
