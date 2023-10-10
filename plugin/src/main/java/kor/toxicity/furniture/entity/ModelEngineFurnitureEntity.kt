package kor.toxicity.furniture.entity

import com.ticxo.modelengine.api.ModelEngineAPI
import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.api.event.FurnitureDeSpawnEvent
import kor.toxicity.furniture.api.event.FurnitureSpawnEvent
import kor.toxicity.furniture.blueprint.ModelEngineFurnitureBlueprint
import kor.toxicity.furniture.extension.call
import kor.toxicity.furniture.manager.UUIDManager
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ModelEngineFurnitureEntity(
    private val furniture: ToxicityFurnitureImpl,
    private val baseBlueprint: ModelEngineFurnitureBlueprint,
    private val centerLocation: Location,
    removal: Boolean = false,
): FurnitureEntityImpl(removal) {
    private val uniqueID = UUIDManager.getUUID()

    private val hitBoxEntity = ToxicityFurnitureImpl.nms.createHitBoxEntity(centerLocation, 1)
    private val viewerMap = HashMap<UUID, Player>()
    override fun spawn(player: Player, sync: Boolean) {
        val isEmpty = viewerMap.isEmpty()
        if (viewerMap.put(player.uniqueId, player) == null && isEmpty) hitBoxSpawn(sync)
    }

    override fun deSpawn(player: Player, sync: Boolean) {
        if (viewerMap.remove(player.uniqueId) != null && viewerMap.isEmpty()) deSpawn(sync)
    }

    override fun deSpawn(sync: Boolean) {
        if (!hitBoxEntity.isSpawned()) return
        fun deSpawn() {
            hitBoxEntity.remove()
            FurnitureDeSpawnEvent(this).call()
        }
        if (sync) deSpawn()
        else Bukkit.getScheduler().runTask(furniture) { _ ->
            deSpawn()
        }
    }

    override fun hitBoxSpawn(sync: Boolean) {
        if (hitBoxEntity.isSpawned()) return
        fun spawn() {
            hitBoxEntity.spawn()
            ModelEngineAPI.createModeledEntity(hitBoxEntity.getBukkitEntity()).addModel(ModelEngineAPI.createActiveModel(baseBlueprint.model), true)
            FurnitureSpawnEvent(this).call()
        }
        if (sync) spawn()
        else Bukkit.getScheduler().runTask(furniture) { _ ->
            spawn()
        }
    }

    override fun getChunks(): List<Chunk> {
        return ArrayList<Chunk>().apply {
            add(centerLocation.chunk)
        }
    }

    override fun getUUIDSet(): Set<UUID> {
        return HashSet<UUID>().apply {
            add(hitBoxEntity.getUUID())
        }
    }

    override fun getUUID(): UUID {
        return uniqueID
    }

    override fun getBlueprint(): FurnitureBlueprint {
        return baseBlueprint
    }

    override fun getLocation(): Location {
        return centerLocation.clone()
    }

    override fun getViewer(): Collection<Player> {
        return viewerMap.values
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelEngineFurnitureEntity

        return uniqueID == other.uniqueID
    }

    override fun hashCode(): Int {
        return uniqueID.hashCode()
    }
    override fun compareTo(other: FurnitureEntity): Int {
        return uuid.compareTo(other.uuid)
    }

    override fun getHitboxEntity(uuid: UUID): Entity {
        return hitBoxEntity.getBukkitEntity()
    }

    override fun teleport(location: Location) {
        hitBoxEntity.teleport(location)
    }
}