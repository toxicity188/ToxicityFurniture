package kor.toxicity.furniture.api.entity;

import org.jetbrains.annotations.NotNull;

public interface ModelEngineEntity extends FurnitureEntity {
    /**
     * Play ModelEngine's animation.
     * @param animation animation name
     * @param lerpIn in speed
     * @param lerpOut out speed
     * @param speed speed of animation
     * @since 1.0.4
     */
    default void playAnimation(@NotNull String animation, double lerpIn, double lerpOut, double speed) {
        playAnimation(animation, lerpIn, lerpOut, speed, true);
    }
    /**
     * Play ModelEngine's animation.
     * @param animation animation name
     * @param lerpIn in speed
     * @param lerpOut out speed
     * @param speed speed of animation
     * @param force whether to force
     * @since 1.0.4
     */
    void playAnimation(@NotNull String animation, double lerpIn, double lerpOut, double speed, boolean force);
}
