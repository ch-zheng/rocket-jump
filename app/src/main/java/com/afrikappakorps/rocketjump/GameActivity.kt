package com.afrikappakorps.rocketjump

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Messenger
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.roundToInt

class GameActivity : Activity() {
    private val gameLoop = GameLoop()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        gameLoop.start()
        val surfaceView: SurfaceView = findViewById(R.id.game_surface)
        val holder = surfaceView.holder
        holder.setKeepScreenOn(true)
        holder.addCallback(GameRenderer(holder))
        surfaceView.setOnTouchListener(PlayerTouchListener())
    }
    private inner class GameRenderer(val holder: SurfaceHolder) : SurfaceHolder.Callback {
        private val renderThread = RenderThread("Rendering")
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceCreated(holder: SurfaceHolder) { renderThread.start() }
        override fun surfaceDestroyed(holder: SurfaceHolder) { renderThread.quitSafely() }
        private inner class RenderThread(name: String) : HandlerThread(name), Choreographer.FrameCallback {
            private val handler = Handler {
                //Draw game state
                val canvas = holder.lockHardwareCanvas()
                //TODO: Draw game state
                holder.unlockCanvasAndPost(canvas)
                Choreographer.getInstance().postFrameCallback(this)
                true
            }
            override fun onLooperPrepared() {
                Choreographer.getInstance().postFrameCallback(this)
            }
            override fun doFrame(time: Long) {
                val message = Message.obtain(gameLoop.handler, GameLoop.What.GET_GAMESTATE.ordinal)
                message.replyTo = Messenger(handler)
                message.sendToTarget()
            }
        }
    }
    private inner class PlayerTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if(event.action == MotionEvent.ACTION_DOWN) {
                view.performClick()
                val message = Message.obtain(
                    gameLoop.handler,
                    GameLoop.What.INPUT.ordinal,
                    event.x.roundToInt(),
                    event.y.roundToInt()
                )
                message.sendToTarget()
            }
            return true
        }
    }
}