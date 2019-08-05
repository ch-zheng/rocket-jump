package com.afrikappakorps.rocketjump

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log

class GameLoop : Thread() {
    enum class What { GET_GAMESTATE, INPUT }
    private val gameModel = GameModel()
    val handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            What.INPUT.ordinal -> {
                //TODO: Handle touch input
                Log.i("Touch coordinates", "${it.arg1},${it.arg2}")
            }
            What.GET_GAMESTATE.ordinal -> {
                val message = Message.obtain()
                message.obj = gameModel
                it.replyTo.send(message)
            }
        }
        true
    }
    override fun run() {
        //Game loop proper
        while (!isInterrupted) {
            gameModel.lockInterruptibly()
            try { if (!gameModel.update()) interrupt() }
            finally { gameModel.unlock() }
            sleep(100)
        }
    }
}