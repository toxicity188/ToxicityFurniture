package kor.toxicity.furniture.blueprint

import com.ticxo.modelengine.api.ModelEngineAPI
import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.entity.ModelEngineFurnitureEntity
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class ModelEngineFurnitureBlueprint(private val baseKey: String, section: ConfigurationSection): FurnitureBlueprintImpl(section) {
    val model = (section.getString("model") ?: throw RuntimeException("Model value not found.")).let {
        ModelEngineAPI.getBlueprint(it) ?: throw RuntimeException("This model doesn't exist: $it")
    }

    override fun getKey(): String {
        return baseKey
    }
}