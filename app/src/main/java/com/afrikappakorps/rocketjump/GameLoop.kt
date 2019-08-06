package com.afrikappakorps.rocketjump

import android.os.Handler
import android.os.Looper
import android.view.View

class GameLoop : Thread() {
    val gameModel = GameModel()
    private var lastTick: Long = 0
    val handler = Handler(Looper.getMainLooper()) {
        gameModel.lock.writeLock().lock()
        try {
            val view = it.obj as View
            val blockPixels: Double = view.width.toDouble() / gameModel.worldWidth.toDouble()
            gameModel.touch(
                it.arg1 / blockPixels,
                (view.height - it.arg2) / blockPixels
            )
        }
        finally {gameModel.lock.writeLock().unlock() }
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