package com.afrikappakorps.rocketjump

import android.os.Handler
import android.os.Looper

class GameLoop : Thread() {
    val gameModel = GameModel()
    private var lastTick: Long = 0
    val handler = Handler(Looper.getMainLooper()) {
        //TODO: Handle touch input
        true
    }
    override fun run() {
        //Game loop proper
        lastTick = System.currentTimeMillis()
        while (!isInterrupted) {
            gameModel.lock.writeLock().lock()
            try {
                if (!gameModel.update((System.currentTimeMillis() - lastTick) / 10.0)) interrupt()
                lastTick = System.currentTimeMillis()
            } finally { gameModel.lock.writeLock().unlock() }
            sleep(10)
        }
    }
}