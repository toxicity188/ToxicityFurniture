package kor.toxicity.furniture.blueprint

import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.configuration.ConfigurationSection

class ModelEngineFurnitureBlueprint(private val baseKey: String, section: ConfigurationSection): FurnitureBlueprintImpl(section) {
    val model = (section.getString("model") ?: throw RuntimeException("Model value not found.")).let {
        ModelEngineAPI.getBlueprint(it) ?: throw RuntimeException("This model doesn't exist: $it")
    }

    override fun getKey(): String {
        return baseKey
    }
}