package com.afrikappakorps.rocketjump

import java.util.concurrent.locks.ReentrantReadWriteLock

class GameModel {
    val lock = ReentrantReadWriteLock()
    //Floating-point variables must be Doubles, not Floats
    fun update(delta: Double): Boolean {
        //TODO: Obviously
        return true
    }
}