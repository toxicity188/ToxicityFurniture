package kor.toxicity.furniture.blueprint

import kor.toxicity.furniture.ToxicityFurniture
import kor.toxicity.furniture.entity.BaseFurnitureEntity
import kor.toxicity.furniture.extension.FURNITURE_ITEM_KEY
import kor.toxicity.furniture.extension.getAsVector
import kor.toxicity.furniture.manager.ResourcePackManager
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class FurnitureBlueprint(val furniture: ToxicityFurniture, val key: String, section: ConfigurationSection) {
    val hitBox = section.getConfigurationSection("hit-box")?.let {
        it.getKeys(false).mapNotNull { s ->
            it.getConfigurationSection(s)?.let { config ->
                HitBoxBlueprint(config)
            }
        }
    }?.apply {
        if (isEmpty()) throw RuntimeException("Hit box is empty.")
    } ?: throw RuntimeException("Hit box configuration doesn't exist.")

    val texts = section.getConfigurationSection("texts")?.let {
        it.getKeys(false).mapNotNull { s ->
            it.getConfigurationSection(s)?.let { config ->
                TextBlueprint(config)
            }
        }
    }

    val asset = (section.getString("asset") ?: throw RuntimeException("Asset value not found.")).let { str ->
        ItemStack(ToxicityFurniture.Config.furnitureMaterial).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(str)
                setCustomModelData(ResourcePackManager.getCustomModelData(str) ?: throw RuntimeException("The asset named \"$str\" doesn't exist."))
                persistentDataContainer.set(FURNITURE_ITEM_KEY, PersistentDataType.STRING, key)
            }
        }
    }
        get() = field.clone()

    val scale = section.getAsVector("scale")
    val offset = section.getAsVector("offset")


    fun place(location: Location): BaseFurnitureEntity {
        return BaseFurnitureEntity(furniture,this, location)
    }
}