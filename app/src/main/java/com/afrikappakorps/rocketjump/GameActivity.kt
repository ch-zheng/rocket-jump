package com.afrikappakorps.rocketjump

import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlin.math.PI
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
            //Canvas variables
            private val backgroundPaint = Paint()
            private val bitmapOptions = BitmapFactory.Options()
            init {
                backgroundPaint.color = Color.CYAN
                backgroundPaint.style = Paint.Style.FILL
                bitmapOptions.inScaled = false
            }
            val characterSprite: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.character, bitmapOptions)
            val blockSprite: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.metal_block, bitmapOptions)
            val explosiveSprite: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.explosive, bitmapOptions)
            val launcherSprite: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.launcher, bitmapOptions)
            //Drawing Handler
            private val handler: Handler = Handler {
                val canvas = holder.lockHardwareCanvas()
                gameLoop.gameModel.lock.readLock().lock()
                try {
                    canvas.drawPaint(backgroundPaint)
                    val bp: Int = canvas.width / gameLoop.gameModel.worldWidth
                    //Draw blocks
                    for (x in gameLoop.gameModel.blocks) {
                        val rect = Rect(
                            (x.x * bp).roundToInt(),
                            canvas.height - (x.y * bp + x.height * bp).roundToInt(),
                            (x.x * bp + x.width * bp).roundToInt(),
                            canvas.height - (x.y * bp).roundToInt()
                        )
                        canvas.drawBitmap(blockSprite, null, rect, null)
                    }
                    //Draw rockets
                    for (x in gameLoop.gameModel.rockets) {
                        canvas.save()
                        val rect = Rect(
                            (x.hitbox.x * bp - 0.7 * bp).roundToInt(),
                            canvas.height - (x.hitbox.y * bp + 0.25 * bp).roundToInt(),
                            (x.hitbox.x * bp + 0.7 * bp).roundToInt(),
                            canvas.height - (x.hitbox.y * bp - 0.25 * bp).roundToInt()
                        )
                        canvas.rotate(
                            (-x.velocity.direction * 180 / PI).toFloat(),
                            (bp * x.hitbox.x).toFloat(), canvas.height - (bp * x.hitbox.y).toFloat()
                        )
                        canvas.drawBitmap(explosiveSprite, null, rect, null)
                        canvas.restore()
                    }
                    //Draw player
                    val player = gameLoop.gameModel.player
                    canvas.drawBitmap(launcherSprite, null, Rect(
                        (player.hitbox.x * bp + player.hitbox.width / 2 * bp - 0.6 * bp).roundToInt(),
                        canvas.height - (player.hitbox.y * bp + player.hitbox.height / 2 * bp + 0.25 * bp).roundToInt(),
                        (player.hitbox.x * bp + player.hitbox.width / 2 * bp + 0.6 * bp).roundToInt(),
                        canvas.height - (player.hitbox.y * bp + player.hitbox.height / 2 * bp - 0.25 * bp).roundToInt()
                    ), null)
                    canvas.drawBitmap(characterSprite, null, Rect(
                        (player.hitbox.x * bp).roundToInt(),
                        canvas.height - (player.hitbox.y * bp + player.hitbox.height * bp).roundToInt(),
                        (player.hitbox.x * bp + player.hitbox.width * bp).roundToInt(),
                        canvas.height - (player.hitbox.y * bp).roundToInt()
                    ), null)
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
                message.obj = view
                message.sendToTarget()
            }
            return true
        }
    }
}