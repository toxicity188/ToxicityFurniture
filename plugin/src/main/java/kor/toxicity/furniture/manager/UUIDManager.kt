package kor.toxicity.furniture.manager

import kor.toxicity.furniture.ToxicityFurnitureImpl
import java.util.UUID

object UUIDManager: FurnitureManager {
    private val uuidSet = HashSet<UUID>()
    override fun start(furniture: ToxicityFurnitureImpl) {

    }

    override fun reload(furniture: ToxicityFurnitureImpl) {
        uuidSet.clear()
    }

    override fun end(furniture: ToxicityFurnitureImpl) {
    }

    fun getUUID(): UUID {
        var uuid = UUID.randomUUID()
        while (!uuidSet.add(uuid)) uuid = UUID.randomUUID()
        return uuid
    }
}