package kor.toxicity.furniture.entity

import kor.toxicity.furniture.api.entity.FurnitureEntity

interface FurnitureEntityImpl: FurnitureEntity {
    fun hitBoxSpawn(sync: Boolean = true)
}