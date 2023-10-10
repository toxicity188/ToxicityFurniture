package kor.toxicity.furniture.nms

import org.bukkit.Location
import org.bukkit.entity.Player

interface NMS {
    fun createHitBoxEntity(location: Location, size: Int): HitBoxEntity
    fun createItemDisplay(location: Location): VirtualItemDisplay
    fun createTextDisplay(location: Location): VirtualTextDisplay
}