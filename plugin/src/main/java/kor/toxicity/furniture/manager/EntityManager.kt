package kor.toxicity.furniture.manager

import kor.toxicity.furniture.ToxicityFurniture
import kor.toxicity.furniture.registry.EntityRegistry
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object ChunkManager: FurnitureManager {

    private val entityRegistry = EntityRegistry()

    override fun start(furniture: ToxicityFurniture) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                entityRegistry.getByChunk(player.location.chunk).forEach {
                    it.spawn(player)
                }
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                entityRegistry.getByChunk(player.location.chunk).forEach {
                    it.remove(player)
                }
            }
        },furniture)
    }

    override fun reload(furniture: ToxicityFurniture) {

    }

    override fun end(furniture: ToxicityFurniture) {
    }
}