package kor.toxicity.furniture.entity

import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.api.event.FurnitureDeSpawnEvent
import kor.toxicity.furniture.api.event.FurnitureSpawnEvent
import kor.toxicity.furniture.blueprint.BaseFurnitureBlueprint
import kor.toxicity.furniture.extension.*
import kor.toxicity.furniture.manager.UUIDManager
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*

class BaseFurnitureEntity(
    val furniture: ToxicityFurnitureImpl,
    private val baseBlueprint: BaseFurnitureBlueprint,
    private var centerLocation: Location,
    removal: Boolean = false
): FurnitureEntityImpl(removal) {

    private val uniqueID = UUIDManager.getUUID()

    private val playerMap = HashMap<UUID,Player>()

    private val hitBoxes = HashMap<UUID, HitBoxState>().apply {
        baseBlueprint.hitBox.map {
            val entity = ToxicityFurnitureImpl.nms.createHitBoxEntity(
                centerLocation.clone().add(it.relativeLocation.clone().rotateYaw(centerLocation.parseYawToRadian())),
                it.size
            )
            put(entity.getUUID(), HitBoxState(it, entity))
        }
    }
    private val display = ToxicityFurnitureImpl.nms.createItemDisplay(centerLocation.clone().apply {
        y += 0.5
    }.add(baseBlueprint.offset.clone())).apply {
        setItem(baseBlueprint.asset)
        val vec = baseBlueprint.scale
        setScale(
            vec.x.toFloat(),
            vec.y.toFloat(),
            vec.z.toFloat()
        )
    }
    private val texts = baseBlueprint.texts?.map {
        it to ToxicityFurnitureImpl.nms.createTextDisplay(
            centerLocation.clone().add(it.relativeLocation.clone().rotateYaw(centerLocation.parseYawToRadian()))
        ).apply {
            setText(it.text)
            val vec = it.scale
            setScale(
                vec.x.toFloat(),
                vec.y.toFloat(),
                vec.z.toFloat()
            )
        }
    }

    fun sit(player: Player, uuid: UUID) {
        hitBoxes[uuid]?.sit(player)
    }

    override fun teleport(location: Location) {
        val yaw = location.parseYawToRadian()
        val pitch = location.parsePitchToRadian()
        hitBoxes.values.forEach {
            it.entity.teleport(
                location.clone().add(it.blueprint.relativeLocation.clone().rotateYaw(yaw).rotatePitch(pitch))
            )
        }
        display.teleport(location.clone().apply {
            y += 0.5
        }.add(baseBlueprint.offset.clone()))
        texts?.forEach {
            it.second.teleport(
                location.clone().add(it.first.relativeLocation.clone().rotateYaw(yaw).rotatePitch(pitch))
            )
        }
        centerLocation = location
    }

    override fun spawn(player: Player, sync: Boolean) {
        if (playerMap.put(player.uniqueId,player) == null) {
            hitBoxSpawn(sync)
            display.show(player)
            texts?.forEach {
                it.second.show(player)
            }
        }
    }
    override fun deSpawn(player: Player, sync: Boolean) {
        if (playerMap.remove(player.uniqueId) != null) {
            display.hide(player)
            texts?.forEach {
                it.second.hide(player)
            }
            if (playerMap.isEmpty()) {
                if (sync) hitBoxes.values.forEach {
                    it.entity.remove()
                }
                else Bukkit.getScheduler().runTask(furniture) { _ ->
                    hitBoxes.values.forEach {
                        it.entity.remove()
                    }
                }
            }
        }
    }

    override fun getChunks(): List<Chunk> {
        val list = ArrayList<Chunk>()
        list.add(display.asBukkitEntity().location.chunk)
        texts?.forEach {
            list.add(it.second.asBukkitEntity().location.chunk)
        }
        return list
    }

    override fun getUUIDSet() = HashSet(hitBoxes.values.map {
        it.entity.getUUID()
    })

    override fun deSpawn(sync: Boolean) {
        if (sync) {
            hitBoxes.values.forEach {
                it.entity.remove()
            }
            FurnitureDeSpawnEvent(this).call()
        } else {
            Bukkit.getScheduler().runTask(furniture) { _ ->
                hitBoxes.values.forEach {
                    it.entity.remove()
                }
                FurnitureDeSpawnEvent(this).call()
            }
        }
        display.remove()
        texts?.forEach {
            it.second.remove()
        }
        playerMap.clear()
    }
    fun isNotEmpty() = playerMap.isNotEmpty()

    override fun hitBoxSpawn(sync: Boolean) {
        val filter = hitBoxes.values.filter {
            !it.entity.isSpawned()
        }
        if (filter.isEmpty()) return
        if (sync) {
            filter.forEach {
                it.entity.spawn()
            }
            FurnitureSpawnEvent(this).call()
        }
        else Bukkit.getScheduler().runTask(furniture) { _ ->
            filter.forEach {
                it.entity.spawn()
            }
            FurnitureSpawnEvent(this).call()
        }
    }

    override fun getViewer(): Collection<Player> {
        return playerMap.values
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseFurnitureEntity

        return uuid == other.uuid
    }

    override fun getUUID(): UUID {
        return uniqueID
    }

    override fun hashCode(): Int {
        return uniqueID.hashCode()
    }

    override fun getBlueprint(): FurnitureBlueprint {
        return baseBlueprint
    }

    override fun getLocation(): Location {
        return centerLocation.clone()
    }

    override fun compareTo(other: FurnitureEntity): Int {
        return uuid.compareTo(other.uuid)
    }

    override fun getHitboxEntity(uuid: UUID): Entity? {
        return hitBoxes[uuid]?.entity?.getBukkitEntity()
    }
}