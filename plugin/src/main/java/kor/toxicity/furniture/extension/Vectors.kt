package kor.toxicity.furniture.extension

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.*

fun Vector.rotateYaw(yaw: Double): Vector {
    val getX = x
    val getZ = z
    return setX(getX * cos(yaw) - getZ * sin(yaw)).setZ(getX * sin(yaw) + getZ * cos(yaw))
}
fun Vector.rotatePitch(pitch: Double): Vector {
    val getX = x
    val getY = y
    return setX(getX * cos(pitch) - getY * sin(pitch)).setY(getX * sin(pitch) + getY * cos(pitch))
}
fun Vector.rotateRoll(roll: Double): Vector {
    val getZ = z
    val getY = y
    return setZ(getZ * cos(roll) - getY * sin(roll)).setY(getZ * sin(roll) + getY * cos(roll))
}

fun Vector.rotate(pitch: Double, yaw: Double, roll: Double): Vector {
    return rotatePitch(pitch).rotateRoll(roll).rotateYaw(yaw)
}

fun Location.parseYawToRadian(): Double {
    return Math.toRadians(((if (yaw < 0) 360 + yaw else yaw) + 90).toDouble())
}