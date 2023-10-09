package kor.toxicity.furniture.api.event;

import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FurnitureDeSpawnEvent extends AbstractEvent implements FurnitureEntityEvent {
    private final @NotNull FurnitureEntity entity;
    public FurnitureDeSpawnEvent(@NotNull FurnitureEntity entity) {
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
