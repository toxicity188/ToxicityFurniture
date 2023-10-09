package kor.toxicity.furniture.api.event;

import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface FurnitureEntityEvent extends FurniturePluginEvent {
    @NotNull FurnitureEntity getEntity();
    static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
