package kor.toxicity.furniture.api.event;

import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interaction between player and furniture.
 * @since 1.0
 */
public class FurnitureEvent extends PlayerEvent {

    static final HandlerList HANDLER_LIST = new HandlerList();

    private final @NotNull FurnitureEntity furniture;
    public FurnitureEvent(@NotNull Player who, @NotNull FurnitureEntity furniture) {
        super(who);
        this.furniture = furniture;
    }
    /**
     * Returns an entity of furniture.
     * @return furniture entity
     * @since 1.0
     */
    public @NotNull FurnitureEntity getFurniture() {
        return furniture;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
