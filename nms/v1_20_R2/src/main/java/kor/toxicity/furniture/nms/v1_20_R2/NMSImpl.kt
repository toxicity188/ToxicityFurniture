package kor.toxicity.furniture.nms.v1_20_R2

import com.mojang.math.Transformation
import kor.toxicity.furniture.nms.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Entity.RemovalReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Slime
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NMSImpl: NMS {
    private val reasonField = Entity::class.java.declaredFields.first {
        it.type == RemovalReason::class.java
    }
    override fun createHitBoxEntity(location: Location, size: Int): HitBoxEntity {
        return HitBoxEntityImpl(location, size)
    }

    override fun createItemDisplay(location: Location): VirtualItemDisplay {
        return VirtualItemDisplayImpl(location)
    }

    override fun createTextDisplay(location: Location): VirtualTextDisplay {
        return VirtualTextDisplayImpl(location)
    }

    private abstract class VirtualEntityImpl<T: Entity>(
        protected val entity: T,
        location: Location
    ): VirtualEntity {

        protected val connectionMap = ConcurrentHashMap<UUID, ServerGamePacketListenerImpl>()

        init {
            entity.moveTo(location.x, location.y, location.z, location.yaw, location.pitch)
        }

        override fun show(player: Player) {
            connectionMap[player.uniqueId] = (player as CraftPlayer).handle.connection.apply {
                send(ClientboundAddEntityPacket(entity))
                entity.entityData.nonDefaultValues?.let {
                    send(ClientboundSetEntityDataPacket(entity.id, it))
                }
            }
        }

        override fun hide(player: Player) {
            connectionMap.remove(player.uniqueId)?.send(ClientboundRemoveEntitiesPacket(entity.id))
        }

        override fun remove() {
            val packet = ClientboundRemoveEntitiesPacket(entity.id)
            connectionMap.values.forEach {
                it.send(packet)
            }
            connectionMap.clear()
        }

        override fun teleport(location: Location) {
            entity.moveTo(location.x, location.y, location.z, location.yaw, location.pitch)
            val packet = ClientboundTeleportEntityPacket(entity)
            connectionMap.values.forEach {
                it.send(packet)
            }
        }

        override fun asBukkitEntity(): org.bukkit.entity.Entity {
            return entity.bukkitEntity
        }
    }
    private abstract class VirtualDisplayImpl<T: Display>(
        t: T,
        location: Location
    ): VirtualEntityImpl<T>(t, location), VirtualDisplay {
        override fun setScale(x: Float, y: Float, z: Float) {
            entity.setTransformation(Transformation(null,null,Vector3f(x,y,z),null))
        }
    }

    private class VirtualItemDisplayImpl(location: Location): VirtualDisplayImpl<Display.ItemDisplay>(
        Display.ItemDisplay(EntityType.ITEM_DISPLAY, (location.world as CraftWorld).handle),
        location
    ), VirtualItemDisplay {
        override fun setItem(itemStack: ItemStack) {
            entity.itemStack = CraftItemStack.asNMSCopy(itemStack)
            entity.entityData.nonDefaultValues?.let {
                if (connectionMap.isEmpty()) return@let
                val packet = ClientboundSetEntityDataPacket(entity.id, it)
                connectionMap.values.forEach { connection ->
                    connection.send(packet)
                }
            }
        }
    }
    private class VirtualTextDisplayImpl(location: Location): VirtualDisplayImpl<Display.TextDisplay>(
        Display.TextDisplay(EntityType.TEXT_DISPLAY, (location.world as CraftWorld).handle).apply {
            billboardConstraints = Display.BillboardConstraints.CENTER
        },
        location
    ), VirtualTextDisplay {
        override fun setText(component: Component) {
            entity.text = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component))
            entity.entityData.nonDefaultValues?.let {
                if (connectionMap.isEmpty()) return@let
                val packet = ClientboundSetEntityDataPacket(entity.id, it)
                connectionMap.values.forEach { connection ->
                    connection.send(packet)
                }
            }
        }
    }
    private inner class HitBoxEntityImpl(private val location: Location, private val internalSize: Int): HitBoxEntity {
        private var spawn = false

        private var entity = Slime(EntityType.SLIME, (location.world as CraftWorld).handle).apply {
            moveTo(location.x,location.y,location.z,location.yaw,location.pitch)
            isNoAi = true
            isNoGravity = true
            isCustomNameVisible = false
            isInvulnerable = true
            isInvisible = true
            persist = false
            persistentInvisibility = true
            setSize(internalSize, true)
        }

        override fun spawn() {
            if (!isSpawned()) {
                reasonField.run {
                    isAccessible = true
                    set(entity, null)
                    isAccessible = false
                }
                spawn = (location.world as CraftWorld).handle.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.NATURAL)
            }
        }

        override fun isSpawned(): Boolean {
            return spawn && entity.removalReason == null
        }

        override fun getUUID(): UUID {
            return entity.uuid
        }

        override fun remove() {
            if (isSpawned()) {
                entity.remove(RemovalReason.UNLOADED_TO_CHUNK)
                entity.valid = false
                spawn = false
            }
        }
        override fun getBukkitEntity(): org.bukkit.entity.Entity {
            return entity.bukkitEntity
        }
    }
}