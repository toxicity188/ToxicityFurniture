package kor.toxicity.furniture.entity

import kor.toxicity.furniture.api.entity.FurnitureEntity

sealed class FurnitureEntityImpl(val removal: Boolean = false): FurnitureEntity {
    abstract fun hitBoxSpawn(sync: Boolean = true)
    override fun isMarkedToDeSpawn(): Boolean {
        return removal
    }
}