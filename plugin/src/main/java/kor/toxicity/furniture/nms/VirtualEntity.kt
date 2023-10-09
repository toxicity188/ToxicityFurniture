package kor.toxicity.furniture.nms

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface VirtualEntity {
    fun remove()
    fun teleport(location: Location)
    fun asBukkitEntity(): Entity

    fun show(player: Player)
    fun hide(player: Player)
}