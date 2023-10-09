package kor.toxicity.furniture.extension

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

fun ConfigurationSection.getAsLocation(key: String): Location {
    return getConfigurationSection(key)?.let {
        val world = (it.getString("world") ?: throw RuntimeException("World value doesn't exist.")).run {
            Bukkit.getWorld(this) ?: throw RuntimeException("The world \"$this\" doesn't exist.")
        }
        Location(
            world,
            it.getDouble("x"),
            it.getDouble("y"),
            it.getDouble("z"),
            it.getDouble("yaw").toFloat(),
            it.getDouble("pitch").toFloat()
        )
    } ?: throw RuntimeException("Syntax error - the key \"$key\" is not a configuration section.")
}

fun ConfigurationSection.getAsVector(key: String): Vector {
    return getConfigurationSection(key)?.let {
        Vector(
            it.getDouble("x"),
            it.getDouble("y"),
            it.getDouble("z")
        )
    } ?: throw RuntimeException("Syntax error - the key \"$key\" is not a configuration section.")
}