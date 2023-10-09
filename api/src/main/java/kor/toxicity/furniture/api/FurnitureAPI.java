package kor.toxicity.furniture.api;

import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint;
import kor.toxicity.furniture.api.entity.FurnitureEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author toxicity
 */
public abstract class FurnitureAPI extends JavaPlugin {
    private static FurnitureAPI api;

    @Override
    public void onEnable() {
        api = this;
    }

    /**
     * Gets an API of Furniture.
     * @return an instance of furniture
     * @since 1.0
     */
    public static @NotNull FurnitureAPI getApi() {
        return Objects.requireNonNull(api);
    }

    /**
     * Gets a blueprint of furniture.
     * @param name a YAML key of blueprint.
     * @return a blueprint or null if not exist
     * @since 1.0
     */
    public abstract @Nullable FurnitureBlueprint getBlueprint(@NotNull String name);

    /**
     * Gets an instance of spawned furniture entity.
     * Returns null if entity is not currently spawned.
     * @param world target entity's world
     * @param uuid bukkit entity's uuid
     * @return spawned furniture or null if not spawned
     * @since 1.0
     */
    public abstract @Nullable FurnitureEntity getByUUID(@NotNull World world, @NotNull UUID uuid);
    /**
     * Gets an instance of spawned furniture entity.
     * Returns null if entity is not currently spawned.
     * @param entity an instance of bukkit entity
     * @return spawned furniture or null if not spawned
     * @since 1.0
     */
    public @Nullable FurnitureEntity getByEntity(@NotNull Entity entity) {
        Objects.requireNonNull(entity);
        return getByUUID(entity.getWorld(), entity.getUniqueId());
    }
    /**
     * Gets an instance of spawned furniture entity.
     * Returns null if entity is not currently spawned.
     * @param uuid bukkit entity's uuid
     * @return spawned furniture or null if not spawned
     * @since 1.0
     */
    public @Nullable FurnitureEntity getByUUID(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);
        return Bukkit.getWorlds().stream()
                .map(w -> getByUUID(w, uuid))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets an unmodifiable list of spawned furniture entity nearby some location.
     * @param location a center location
     * @param radius a radius
     * @return unmodifiable list of furniture entity
     */
    public @Unmodifiable @NotNull List<FurnitureEntity> getNearbyEntity(@NotNull Location location, double radius) {
        return getNearbyEntity(Objects.requireNonNull(location), radius, radius, radius);
    }

    /**
     * Gets an unmodifiable list of spawned furniture entity nearby some location.
     * @param location a center location
     * @param x a x-axis radius
     * @param y a y-axis radius
     * @param z a z-axis radius
     * @return unmodifiable list of furniture entity
     */
    public @Unmodifiable @NotNull List<FurnitureEntity> getNearbyEntity(@NotNull Location location, double x, double y, double z) {
        var world = Objects.requireNonNull(Objects.requireNonNull(location).getWorld());
        return world.getNearbyEntities(location, x, y, z).stream()
                .map(e -> getByUUID(world, e.getUniqueId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * Spawns a furniture entity by given blueprint and location.
     * @param blueprint target blueprint
     * @param location target location
     * @return a created entity
     * @since 1.0
     */
    public abstract @NotNull FurnitureEntity create(@NotNull FurnitureBlueprint blueprint, @NotNull Location location);

    /**
     * Remove entity from registry.
     * @param entity target entity
     * @since 1.0
     */
    public abstract void remove(@NotNull FurnitureEntity entity);
}
