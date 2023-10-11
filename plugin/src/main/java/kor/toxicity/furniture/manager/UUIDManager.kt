package kor.toxicity.furniture.manager

import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.registry.EntityRegistry
import org.bukkit.Bukkit
import java.util.*

object UUIDManager: FurnitureManager {
    private val uuidSet = HashSet<UUID>()
    override fun start(furniture: ToxicityFurnitureImpl) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(furniture, Runnable {
            @Synchronized
            fun clear() {
                uuidSet.removeIf { uuid ->
                    !EntityRegistry.globalEntityMap.containsKey(uuid)
                }
            }
            clear()
        }, 20, 20)
    }

    override fun reload(furniture: ToxicityFurnitureImpl) {
        uuidSet.clear()
    }

    override fun end(furniture: ToxicityFurnitureImpl) {
    }

    fun getUUID(): UUID {
        var uuid = UUID.randomUUID()
        while (!uuidSet.add(uuid)) uuid = UUID.randomUUID()
        return uuid
    }
}