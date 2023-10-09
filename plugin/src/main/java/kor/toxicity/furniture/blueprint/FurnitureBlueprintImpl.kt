package kor.toxicity.furniture.blueprint

import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint
import org.bukkit.configuration.ConfigurationSection

sealed class FurnitureBlueprintImpl(section: ConfigurationSection): FurnitureBlueprint {
    private val save = section.getBoolean("save", true)
    override fun isSaved(): Boolean {
        return save
    }
}