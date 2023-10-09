package kor.toxicity.furniture.manager

import kor.toxicity.furniture.ToxicityFurnitureImpl

interface FurnitureManager {
    fun start(furniture: ToxicityFurnitureImpl)
    fun reload(furniture: ToxicityFurnitureImpl)
    fun end(furniture: ToxicityFurnitureImpl)
}