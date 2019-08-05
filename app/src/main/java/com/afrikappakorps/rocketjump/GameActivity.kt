package com.afrikappakorps.rocketjump

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
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
        private inner class RenderThread(name: String) : HandlerThread(name) {
            private val backgroundPaint = Paint()
            init {
                backgroundPaint.color = Color.WHITE
                backgroundPaint.style = Paint.Style.FILL
            }
            private val handler: Handler = Handler {
                val canvas = holder.lockHardwareCanvas()
                gameLoop.gameModel.lock.readLock().lock()
                try {
                    canvas.drawPaint(backgroundPaint)
                    //TODO: Draw game state
                } finally { gameLoop.gameModel.lock.readLock().unlock() }
                holder.unlockCanvasAndPost(canvas)
                Choreographer.getInstance().postFrameCallback(trigger)
                true
            }
            private val trigger = Choreographer.FrameCallback { handler.sendEmptyMessage(0) }
            override fun onLooperPrepared() { Choreographer.getInstance().postFrameCallback(trigger) }
        }
    }
    private inner class PlayerTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if(event.action == MotionEvent.ACTION_DOWN) {
                view.performClick()
                val message = Message.obtain(gameLoop.handler)
                message.arg1 = event.x.roundToInt()
                message.arg2 = event.y.roundToInt()
                message.sendToTarget()
            }
            return true
        }
    }
}