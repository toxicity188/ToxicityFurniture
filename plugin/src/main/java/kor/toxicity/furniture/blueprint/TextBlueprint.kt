package kor.toxicity.furniture.blueprint

import kor.toxicity.furniture.extension.getAsVector
import kor.toxicity.toxicitylibs.api.ReaderBuilder
import org.bukkit.configuration.ConfigurationSection

class TextBlueprint(section: ConfigurationSection) {
    val relativeLocation = section.getAsVector("relative-location")
    val text = section.getString("text")?.let {
        ReaderBuilder.simple(it).build().result
    } ?: throw RuntimeException("Text value not found.")
    val scale = section.getAsVector("scale")
}