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
                    //canvas.translate(0f, canvas.height.toFloat())
                    //canvas.scale(1f,-1f)
                    val bp: Int = canvas.width / gameLoop.gameModel.worldWidth
                    //Draw blocks
                    for (x in gameLoop.gameModel.blocks) {
                        val rect = Rect(
                            bp * x.x.roundToInt(),
                            canvas.height - bp * (x.y + x.height).roundToInt(),
                            bp * (x.x + x.width).roundToInt(),
                            canvas.height - bp * x.y.roundToInt()
                        )
                        canvas.drawBitmap(blockSprite, null, rect, null)
                    }
                    //Draw rockets
                    for (x in gameLoop.gameModel.rockets) {
                        //canvas.save()
                        val rect = Rect(
                            bp * (x.hitbox.x - 0.5).roundToInt(),
                            canvas.height - bp * (x.hitbox.y + 0.25).roundToInt(),
                            bp * (x.hitbox.x + 0.5).roundToInt(),
                            canvas.height - bp * (x.hitbox.y - 0.25).roundToInt()
                        )
                        //TODO: Rotate by proper angle
                        //canvas.rotate(0f, bp * x.hitbox.x.toFloat(), bp * x.hitbox.y.toFloat())
                        canvas.drawBitmap(explosiveSprite, null, rect, null)
                        //canvas.restore()
                    }
                    //Draw player
                    val player = gameLoop.gameModel.player
                    canvas.drawBitmap(launcherSprite, null, Rect(
                        bp * (player.hitbox.x - 0.5).roundToInt(),
                        canvas.height - bp * (player.hitbox.y + 0.25).roundToInt(),
                        bp * (player.hitbox.x + 0.5).roundToInt(),
                        canvas.height - bp * (player.hitbox.y - 0.25).roundToInt()
                    ), null)
                    canvas.drawBitmap(characterSprite, null, Rect(
                        bp * player.hitbox.x.roundToInt(),
                        canvas.height - bp * (player.hitbox.y + player.hitbox.height).roundToInt(),
                        bp * (player.hitbox.x + player.hitbox.width).roundToInt(),
                        canvas.height - bp * player.hitbox.y.roundToInt()
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