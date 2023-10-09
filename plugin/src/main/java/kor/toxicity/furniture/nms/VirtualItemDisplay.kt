package kor.toxicity.furniture.nms

import org.bukkit.inventory.ItemStack

interface VirtualItemDisplay: VirtualDisplay {
    fun setItem(itemStack: ItemStack)
}