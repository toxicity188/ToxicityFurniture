package kor.toxicity.furniture.entity

import kor.toxicity.furniture.ToxicityFurnitureImpl
import kor.toxicity.furniture.api.blueprint.FurnitureBlueprint
import kor.toxicity.furniture.api.entity.FurnitureEntity
import kor.toxicity.furniture.api.event.FurnitureDeSpawnEvent
import kor.toxicity.furniture.api.event.FurnitureSpawnEvent
import kor.toxicity.furniture.blueprint.BaseFurnitureBlueprint
import kor.toxicity.furniture.extension.call
import kor.toxicity.furniture.extension.parseYawToRadian
import kor.toxicity.furniture.extension.rotateYaw
import kor.toxicity.furniture.manager.UUIDManager
import kor.toxicity.furniture.nms.VirtualEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class BaseFurnitureEntity(
    val furniture: ToxicityFurnitureImpl,
    private val baseBlueprint: BaseFurnitureBlueprint,
    private val centerLocation: Location,
    removal: Boolean = false
): FurnitureEntityImpl(removal) {

    private val uniqueID = UUIDManager.getUUID()

    private val rotate = centerLocation.parseYawToRadian()
    private val playerMap = HashMap<UUID,Player>()

    private val hitBoxes = baseBlueprint.hitBox.map {
        ToxicityFurnitureImpl.nms.createHitBoxEntity(
            centerLocation.clone().add(it.relativeLocation.clone().rotateYaw(rotate)),
            it.size
        )
    }.toMutableList()
    private val displays = mutableListOf<VirtualEntity>(
        ToxicityFurnitureImpl.nms.createItemDisplay(centerLocation.clone().apply {
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
    ).apply {
        baseBlueprint.texts?.let {
            addAll(it.map { blueprint ->
                ToxicityFurnitureImpl.nms.createTextDisplay(
                    centerLocation.clone().add(blueprint.relativeLocation.clone().rotateYaw(rotate))
                ).apply {
                    setText(blueprint.text)
                    val vec = blueprint.scale
                    setScale(
                        vec.x.toFloat(),
                        vec.y.toFloat(),
                        vec.z.toFloat()
                    )
                }
            })
        }
    }

    override fun spawn(player: Player, sync: Boolean) {
        if (playerMap.put(player.uniqueId,player) == null) {
            hitBoxSpawn(sync)
            displays.forEach {
                it.show(player)
            }
        }
    }
    override fun deSpawn(player: Player, sync: Boolean) {
        if (playerMap.remove(player.uniqueId) != null) {
            displays.forEach {
                it.hide(player)
            }
            if (playerMap.isEmpty()) {
                if (sync) hitBoxes.forEach {
                    it.remove()
                }
                else Bukkit.getScheduler().runTask(furniture) { _ ->
                    hitBoxes.forEach {
                        it.remove()
                    }
                }
            }
        }
    }

    override fun getChunks() = displays.map {
        it.asBukkitEntity().run {
            location.chunk
        }
    }

    override fun getUUIDSet() = HashSet(hitBoxes.map {
        it.getUUID()
    })

    override fun deSpawn(sync: Boolean) {
        if (sync) {
            hitBoxes.forEach {
                it.remove()
            }
            FurnitureDeSpawnEvent(this).call()
        } else {
            Bukkit.getScheduler().runTask(furniture) { _ ->
                hitBoxes.forEach {
                    it.remove()
                }
                FurnitureDeSpawnEvent(this).call()
            }
        }
        displays.forEach {
            it.remove()
        }
        playerMap.clear()
    }
    fun isNotEmpty() = playerMap.isNotEmpty()

    override fun hitBoxSpawn(sync: Boolean) {
        val filter = hitBoxes.filter {
            !it.isSpawned()
        }
        if (filter.isEmpty()) return
        if (sync) {
            filter.forEach {
                it.spawn()
            }
            FurnitureSpawnEvent(this).call()
        }
        else Bukkit.getScheduler().runTask(furniture) { _ ->
            filter.forEach {
                it.spawn()
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
}