package kor.toxicity.furniture.entity

import kor.toxicity.furniture.ToxicityFurniture
import kor.toxicity.furniture.api.Furniture
import kor.toxicity.furniture.blueprint.FurnitureBlueprint
import kor.toxicity.furniture.extension.parseYawToRadian
import kor.toxicity.furniture.extension.rotateYaw
import kor.toxicity.furniture.manager.UUIDManager
import kor.toxicity.furniture.nms.VirtualEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

class FurnitureEntity(
    val furniture: ToxicityFurniture,
    val blueprint: FurnitureBlueprint,
    val centerLocation: Location
): Furniture {

    val uuid = UUIDManager.getUUID()

    private val rotate = centerLocation.parseYawToRadian()
    private val playerMap = HashMap<UUID,Player>()

    private val hitBoxes = blueprint.hitBox.map {
        ToxicityFurniture.nms.createHitBoxEntity(
            centerLocation.clone().add(it.relativeLocation.clone().rotateYaw(rotate)),
            it.size
        )
    }.toMutableList()
    private val displays = mutableListOf<VirtualEntity>(
        ToxicityFurniture.nms.createItemDisplay(centerLocation.clone().add(blueprint.offset.clone())).apply {
            setItem(blueprint.asset)
            val vec = blueprint.scale
            setScale(
                vec.x.toFloat(),
                vec.y.toFloat(),
                vec.z.toFloat()
            )
        }
    ).apply {
        blueprint.texts?.let {
            addAll(it.map { blueprint ->
                ToxicityFurniture.nms.createTextDisplay(
                    centerLocation.clone().add(blueprint.relativeLocation.rotateYaw(rotate))
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

    fun spawn(player: Player, sync: Boolean = true) {
        if (playerMap.put(player.uniqueId,player) == null) {
            hitBoxSpawn(sync)
            displays.forEach {
                it.show(player)
            }
        }
    }
    fun remove(player: Player, sync: Boolean = true) {
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

    fun getChunks() = displays.map {
        it.asBukkitEntity().run {
            location.chunk
        }
    }

    fun getUUIDSet() = hitBoxes.map {
        it.getUUID()
    }.distinct()

    fun remove(sync: Boolean = true) {
        if (sync) hitBoxes.forEach {
            it.remove()
        } else {
            Bukkit.getScheduler().runTask(furniture) { _ ->
                hitBoxes.forEach {
                    it.remove()
                }
            }
        }
        displays.forEach {
            it.remove()
        }
        playerMap.clear()
    }
    fun isNotEmpty() = playerMap.isNotEmpty()

    fun hitBoxSpawn(sync: Boolean) {
        val filter = hitBoxes.filter {
            !it.isSpawned()
        }
        if (filter.isEmpty()) return
        if (sync) filter.forEach {
            it.spawn()
        }
        else Bukkit.getScheduler().runTask(furniture) { _ ->
            filter.forEach {
                it.spawn()
            }
        }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FurnitureEntity

        return uuid == other.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }


}