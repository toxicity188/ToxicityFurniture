package kor.toxicity.furniture.registry

import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.chunk.ChunkLoc
import kor.toxicity.furniture.entity.FurnitureEntityImpl
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class EntityRegistry(private val furniture: ToxicityFurnitureImpl, private val world: World): Iterable<FurnitureEntity> {
    companion object {
        val globalEntityMap = HashMap<UUID, FurnitureEntityImpl>()
    }


    private val entityMap = ConcurrentHashMap<ChunkLoc, ChunkData>()
    private val uuidMap = HashMap<UUID, FurnitureEntityImpl>()
    fun getByUUID(uuid: UUID) = uuidMap[uuid]

    fun enableChunk(chunk: ChunkLoc): Boolean {
        return entityMap[chunk]?.let {
            val b = !it.enabled
            if (b) it.runTask()
            b
        } ?: false
    }
    fun addEntity(furnitureEntity: FurnitureEntityImpl) {
        furnitureEntity.getChunks().forEach {
            val data = entityMap.getOrPut(ChunkLoc(it.x,it.z)) {
                ChunkData()
            }
            data.entity[furnitureEntity.uuid] = furnitureEntity
        }
        furnitureEntity.getUUIDSet().forEach {
            uuidMap[it] = furnitureEntity
        }
        globalEntityMap[furnitureEntity.uuid] = furnitureEntity
    }

    fun removeEntity(furnitureEntity: FurnitureEntityImpl, sync: Boolean = true) {
        for (value in entityMap.values) {
            if (value.entity.remove(furnitureEntity.uuid) != null) break
        }
        furnitureEntity.getUUIDSet().forEach {
            uuidMap.remove(it)
        }
        furnitureEntity.deSpawn(sync)
        globalEntityMap.remove(furnitureEntity.uuid)
    }

    fun removeEntity(chunk: ChunkLoc, sync: Boolean = true) {
        entityMap[chunk]?.let {
            it.entity.forEach { entity ->
                entity.value.deSpawn(sync)
            }
            it.stopTask(sync)
        }
    }
    fun add(chunk: ChunkLoc, sync: Boolean = true) {
        entityMap[chunk]?.let {
            it.runTask()
            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.world.uid == world.uid) it.entity.forEach { entity ->
                    entity.value.spawn(player, sync)
                }
            }
        }
    }
    fun removeEntity(sync: Boolean = true) {
        entityMap.values.forEach {
            it.entity.forEach { entity ->
                entity.value.deSpawn(sync)
            }
            it.stopTask(sync)
        }
        entityMap.clear()
        uuidMap.clear()
    }
    fun add(player: Player, sync: Boolean = true) {
        entityMap.values.forEach {
            if (it.enabled && it.playerMap.put(player.uniqueId,player) == null) {
                it.entity.forEach { entity ->
                    entity.value.spawn(player, sync)
                }
            }
        }
    }
    fun removeEntity(player: Player, sync: Boolean = true) {
        entityMap.values.forEach {
            if (it.enabled && it.playerMap.remove(player.uniqueId) != null) {
                var enabled = false
                it.entity.forEach { entity ->
                    entity.value.deSpawn(player, sync)
                    if (entity.value.isViewed) enabled = true
                }
                if (enabled && !it.enabled) it.runTask()
                else if (!enabled && it.enabled) it.stopTask(sync)
            }
        }
    }

    override fun iterator(): Iterator<FurnitureEntity> {
        val array = ArrayList<FurnitureEntity>()
        entityMap.values.forEach {
            it.entity.forEach { entry ->
                array.add(entry.value)
            }
        }
        return array.iterator()
    }
    fun reload(chunk: ChunkLoc, sync: Boolean = true) {
        entityMap[chunk]?.reload(sync)
    }

    private inner class ChunkData {
        val entity = ConcurrentHashMap<UUID,FurnitureEntityImpl>()
        var enabled = false
            private set
        var task: BukkitTask? = null
        val playerMap = HashMap<UUID, Player>()

        fun reload(sync: Boolean = true) {
            entity.values.forEach { entity ->
                playerMap.values.forEach {
                    entity.deSpawn(it, sync)
                    entity.spawn(it, sync)
                }
            }
        }
        fun runTask() {
            enabled = true
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(furniture, Runnable {
                entity.forEach { entry ->
                    entry.value.hitBoxSpawn(false)
                }
            },5,5)
        }
        fun stopTask(sync: Boolean) {
            enabled = false
            task?.cancel()
            task = null
            ArrayList(entity.values).forEach {
                if (it.removal) {
                    entity.remove(it.uuid)
                    it.getUUIDSet().forEach { uuid ->
                        uuidMap.remove(uuid)
                    }
                    globalEntityMap.remove(it.uuid)
                    it.deSpawn(sync)
                }
            }
        }
    }
}