package com.afrikappakorps.rocketjump

import android.util.Log
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin

class GameModel {
    val lock = ReentrantReadWriteLock()
    val worldWidth = 10
    val player = Entity(1.0, 10.0, 1.0, 2.0)
    val blocks = mutableSetOf<Hitbox>()
    val rockets = mutableSetOf<Entity>()
    init {
        for (i in 0..9) blocks.add(Hitbox(i.toDouble(),0.0,1.0,1.0))
    }
    fun update(delta: Double): Boolean {
        //Physics
        player.velocity.y -= 0.003 * delta
        player.move(delta)
        for (x in rockets) x.move(delta)
        //Collisions
        if (player.hitbox.x < 0) {
            player.hitbox.x = 0.0
            player.velocity.x = 0.0
        } else if (player.hitbox.x + player.hitbox.width > worldWidth) {
            player.hitbox.x = worldWidth - player.hitbox.width
            player.velocity.x = 0.0
        }
        for (x in blocks) {
            //Collision => Position reversion, velocity reset
            when (player.hitbox.collidesWith(x)) {
                Hitbox.Edge.LEFT -> {
                    player.hitbox.x -= player.velocity.x * delta
                    player.velocity.x = 0.0
                }
                Hitbox.Edge.RIGHT -> {
                    player.hitbox.x -= player.velocity.x * delta
                    player.velocity.x = 0.0
                }
                Hitbox.Edge.TOP -> {
                    player.hitbox.y -= player.velocity.y * delta
                    player.velocity.y = 0.0
                }
                Hitbox.Edge.BOTTOM -> {
                    player.hitbox.y -= player.velocity.y * delta
                    player.velocity.y = 0.0
                }
            }
        }
        val rocketsToRemove = mutableSetOf<Entity>()
        for (x in rockets) {
            if (x.hitbox.x < 0 || x.hitbox.x > worldWidth) {
                rocketsToRemove.add(x)
            } else {
                for (y in blocks) {
                    if (y.contains(x.hitbox.x, x.hitbox.y)) {
                        rocketsToRemove.add(x)
                        player.velocity.y += 0.2 * delta
                    }
                }
            }
        }
        for (x in rocketsToRemove) rockets.remove(x)
        return true
    }
    fun touch(x: Double, y: Double) {
        Log.d("Touch","${x},${y}")
        val rocket = Entity(
            player.hitbox.x + player.hitbox.width / 2,
            player.hitbox.y + player.hitbox.height / 2,
            0.0, 0.0
        )
        rocket.velocity = Vector(x - rocket.hitbox.x, y - rocket.hitbox.y)
        rocket.velocity.magnitude = 0.2
        rockets.add(rocket)
    }
}

class Vector(var x: Double = 0.0, var y: Double = 0.0) {
    var magnitude: Double
        get() = hypot(x,y)
        set(value) {
            val xSign = sign(x); val ySign = sign(y)
            x = value / hypot(y/x, 1.0)
            y = value / hypot(x/y, 1.0)
            x *= xSign; y *= ySign
        }
    var direction: Double
        get() = atan2(y,x)
        set(value) {
            val r = magnitude
            x = r * sin(value); y = r * cos(value)
        }
    operator fun plus(other: Vector): Vector = Vector(x + other.x, y + other.y)
    operator fun minus(other: Vector): Vector = Vector(x - other.x, y - other.y)
}

class Hitbox(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0
    ) {
    enum class Edge { LEFT, RIGHT, TOP, BOTTOM, NONE }
    fun collidesWith(other: Hitbox): Edge {
        /*
        Collision detection algorithm (thanks MDN)
        this.x < other.x + other.width &&
        other.x < this.x + this.width &&
        this.y < other.y + other.height &&
        other.y < this.y + this.height
        */
        //Not colliding
        if (
            x >= other.x + other.width ||
            other.x >= x + width ||
            y >= other.y + other.height ||
            other.y >= y + height
        ) return Edge.NONE
        if (
            min(x + width - other.x, other.x + other.width - x)
            > min(y + height - other.y, other.y + other.height - y)
        ) {
            //Horizontal axis collision
            if (y > other.y) return Edge.BOTTOM
            else if (y < other.y) return Edge.TOP
        } else {
            //Vertical axis collision
            if (x > other.x) return Edge.LEFT
            else if (x < other.x) return Edge.RIGHT
        }
        return Edge.NONE
    }
    fun contains(a: Double, b: Double) = a > x && a < x + width && b > y && b < y + height
}

class Entity(x: Double, y: Double, width: Double, height: Double) {
    val hitbox = Hitbox(x, y, width, height)
    var velocity = Vector()
    fun move(delta: Double) {
        hitbox.x += velocity.x * delta
        hitbox.y += velocity.y * delta
    }
}