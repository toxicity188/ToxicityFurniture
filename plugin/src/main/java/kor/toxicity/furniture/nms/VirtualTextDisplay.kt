package kor.toxicity.furniture.nms

import net.kyori.adventure.text.Component

interface VirtualTextDisplay: VirtualDisplay {
    fun setText(component: Component)
}