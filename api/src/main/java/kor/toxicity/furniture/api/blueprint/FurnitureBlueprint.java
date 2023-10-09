package kor.toxicity.furniture.api.blueprint;

import kor.toxicity.furniture.api.FurnitureAPI;
import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A blueprint of furniture.
 */
public interface FurnitureBlueprint {
    /**
     * Returns a YAML key of this blueprint.
     * @return key
     * @since 1.0
     */
    @NotNull String getKey();

    /**
     * Returns whether this furniture entity will be saved.
     * @return save option
     */
    boolean isSaved();

    /**
     * Spawns a furniture entity by location.
     * @param location target location
     * @return a created entity
     * @since 1.0
     */
    default @NotNull FurnitureEntity spawn(@NotNull Location location) {
        return FurnitureAPI.getApi().create(this, Objects.requireNonNull(location));
    }
}
