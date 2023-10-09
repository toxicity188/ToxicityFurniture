package kor.toxicity.furniture.blueprint

import kor.toxicity.furniture.extension.getAsVector
import org.bukkit.configuration.ConfigurationSection

class HitBoxBlueprint(section: ConfigurationSection) {
    val relativeLocation = section.getAsVector("relative-location")
    val size = section.getInt("size",1).coerceAtLeast(1)
}