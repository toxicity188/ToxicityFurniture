package kor.toxicity.furniture.api.entity;

import kor.toxicity.furniture.api.FurnitureAPI;
import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * An entity of furniture.
 */
public interface FurnitureEntity {
    /**
     * Shows this entity to some player.
     * @param player target player
     * @since 1.0
     */
    default void spawn(@NotNull Player player) {
        spawn(player, true);
    }
    /**
     * Hides this entity to some player.
     * @param player target player
     * @since 1.0
     */
    default void deSpawn(@NotNull Player player) {
        deSpawn(player, true);
    }
    /**
     * Hides this entity to all player.
     * @since 1.0
     */
    default void deSpawn() {
        deSpawn(true);
    }

    /**
     * Returns whether any player currently seeing this entity.
     * @return whether this entity is viewed or not.
     * @since 1.0
     */
    default boolean isViewed() {
        return !getViewer().isEmpty();
    }

    /**
     * Remove entity from registry.
     * @since 1.0
     */
    default void remove() {
        FurnitureAPI.getApi().remove(this);
    }

    /**
     * Shows this entity to some player.
     * @param player target player
     * @param sync whether this method is called synchronously
     * @since 1.0
     */
    void spawn(@NotNull Player player, boolean sync);
    /**
     * Hides this entity to some player.
     * @param player target player
     * @param sync whether this method is called synchronously
     * @since 1.0
     */
    void deSpawn(@NotNull Player player, boolean sync);

    /**
     * Gets a chunk list of all hit box entity.
     * @return All chunks of hit box entity
     * @since 1.0
     */
    @NotNull List<Chunk> getChunks();
    /**
     * Gets an uuid list of all hit box entity.
     * @return All uuid of hit box entity
     * @since 1.0
     */
    @NotNull Set<UUID> getUUIDSet();
    /**
     * Hides this entity to all player.
     * @param sync whether this method is called synchronously
     * @since 1.0
     */
    void deSpawn(boolean sync);

    /**
     * Gets a unique id of this entity.
     * @return uuid
     * @since 1.0
     */
    @NotNull UUID getUUID();
    /**
     * Gets a location of this entity.
     * @return location
     * @since 1.0
     */
    @NotNull Location getLocation();
    /**
     * Gets a blueprint of this entity.
     * @return blueprint
     * @since 1.0
     */
    @NotNull FurnitureBlueprint getBlueprint();

    /**
     * Gets the viewer of this entity.
     * @return all of viewer
     */
    @Unmodifiable @NotNull Collection<Player> getViewer();
}
