package kor.toxicity.furniture.api.event;

import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FurnitureSpawnEvent extends AbstractEvent implements FurnitureEntityEvent {
    private final @NotNull FurnitureEntity entity;
    public FurnitureSpawnEvent(@NotNull FurnitureEntity entity) {
        this.entity = entity;
    }
    @NotNull
    @Override
    public FurnitureEntity getEntity() {
        return entity;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
