package kor.toxicity.furniture.entity

import kor.toxicity.furniture.blueprint.HitBoxBlueprint
import kor.toxicity.furniture.nms.HitBoxEntity
import org.bukkit.entity.Player

class HitBoxState(val blueprint: HitBoxBlueprint, val entity: HitBoxEntity) {
    fun sit(player: Player) {
        if (!blueprint.sit) return
        entity.getBukkitEntity().addPassenger(player)
    }
}