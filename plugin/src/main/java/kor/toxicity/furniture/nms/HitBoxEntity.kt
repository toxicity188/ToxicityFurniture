package kor.toxicity.furniture.nms

import org.bukkit.entity.Entity
import java.util.UUID

interface HitBoxEntity {
    fun spawn()
    fun remove()
    fun isSpawned(): Boolean
    fun getUUID(): UUID
    fun getBukkitEntity(): Entity
}