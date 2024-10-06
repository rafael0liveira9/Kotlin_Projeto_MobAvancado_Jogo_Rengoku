package com.kotlin.projetodemobavancado.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import java.io.IOException

class BattleView(context: Context) : View(context) {
    private val assetManager = context.assets
    private var backgroundBitmap: Bitmap? = null
    private val buttonJumpPaint = Paint()
    private val buttonAttackPaint = Paint()
    private val buttonRadius = 150f
    private val padding = 50f
    private val jumpIcon = BitmapFactory.decodeStream(context.assets.open("ic_arrow_up.png"))
    private val attackIcon = BitmapFactory.decodeStream(context.assets.open("explosion.png"))
    private val heroBitmap: Bitmap = BitmapFactory.decodeStream(context.assets.open("hero1.png"))
    private var isJumpButtonPressed = false
    private var isAttackButtonPressed = false
    private var initialX: Float = 0f
    private var HeroSpeed: Float = 20f
    private var isMovingRight: Boolean = false
    private var isMovingLeft: Boolean = false
    private var position = 100f;
    private val hero = Hero(context, position, (context.resources.displayMetrics.heightPixels - 730f).toFloat(), HeroSpeed)



    private fun scaleBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }


    init {
        try {
            val inputStream = assetManager.open("battle_background.png")
            backgroundBitmap = BitmapFactory.decodeStream(inputStream)


            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            println("Erro ao carregar o arquivo de áudio: ${e.message}")
        }

        buttonJumpPaint.color = Color.argb(150, 128, 128, 128)
        buttonAttackPaint.color = Color.argb(150, 128, 128, 128)
    }





    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y

        val jumpButtonX = width - buttonRadius * 3
        val jumpButtonY = height - buttonRadius
        val attackButtonX = width - buttonRadius * 2
        val attackButtonY = height - 3 * buttonRadius - 2 * padding


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isJumpButtonPressed = isPointInsideCircle(x, y, jumpButtonX, jumpButtonY, buttonRadius)
                isAttackButtonPressed = isPointInsideCircle(x, y, attackButtonX, attackButtonY, buttonRadius)


                if (isJumpButtonPressed) {
                    buttonJumpPaint.color = Color.argb(200, 255, 255, 255);
                    onJumpButtonClicked();
                }
                if (isAttackButtonPressed) {
                    buttonAttackPaint.color = Color.argb(200, 255, 255, 255);
                    onAttackButtonClicked();
                }

                invalidate()
                initialX = event.x
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                handleTouchMovement(event)
                return true
            }
            MotionEvent.ACTION_UP -> {
                stopMovement()
                hero.stopWalking()

                if (isJumpButtonPressed) {
                    buttonJumpPaint.color = Color.argb(150, 128, 128, 128)
                }
                if (isAttackButtonPressed) {
                    buttonAttackPaint.color = Color.argb(150, 128, 128, 128)
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun handleTouchMovement(event: MotionEvent) {
        val diffX = event.x - initialX

        if (Math.abs(diffX) > 100) {
            if (diffX > 0) {
                startMoving(Direction.RIGHT)
            } else {
                startMoving(Direction.LEFT)
            }
        }
    }

    fun startMoving(direction: Direction) {
        when (direction) {
            Direction.RIGHT -> {
                isMovingRight = true
                isMovingLeft = false
            }
            Direction.LEFT -> {
                if (position > 50f) {
                    isMovingLeft = true
                    isMovingRight = false
                }
            }
            else -> {
                isMovingRight = false
                isMovingLeft = false
            }
        }
    }

    fun stopMovement() {
        isMovingRight = false
        isMovingLeft = false
    }

    fun updateHeroMovement() {

        if (isMovingRight) {

            if (position + heroBitmap.width.toFloat() < context.resources.displayMetrics.widthPixels.toFloat()) {
                println("RIGHT")
                position += HeroSpeed
                hero.move(Direction.RIGHT)
            } else {
                stopMovement()
            }
        } else if (isMovingLeft) {

            if (position > 50f) {
                println("LEFT")
                position -= HeroSpeed
                hero.move(Direction.LEFT)
            } else {
                stopMovement()
            }
        }
    }

    private fun isPointInsideCircle(x: Float, y: Float, circleX: Float, circleY: Float, radius: Float): Boolean {
        val dx = x - circleX
        val dy = y - circleY
        return (dx * dx + dy * dy) <= (radius * radius)
    }

    private fun onJumpButtonClicked() {
        hero.jump();
    }

    private fun onAttackButtonClicked() {
        hero.attack();
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backgroundBitmap?.let {
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            val bitmapWidth = it.width.toFloat()
            val bitmapHeight = it.height.toFloat()

            val scaleX = viewWidth / bitmapWidth
            val scaleY = viewHeight / bitmapHeight
            val scale = Math.max(scaleX, scaleY)

            val scaledWidth = bitmapWidth * scale
            val scaledHeight = bitmapHeight * scale

            val left = (viewWidth - scaledWidth) / 2
            val top = (viewHeight - scaledHeight) / 2

            canvas.drawBitmap(it, null, android.graphics.Rect(left.toInt(), top.toInt(), (left + scaledWidth).toInt(), (top + scaledHeight).toInt()), null)
        }

        hero.update()

        hero.draw(canvas)

        updateHeroMovement()

        canvas.drawCircle(width - buttonRadius * 3 - padding, height - buttonRadius - padding, buttonRadius, buttonJumpPaint)
        jumpIcon?.let {
            val scaledJumpIcon = scaleBitmap(it, (buttonRadius * 0.8).toInt(), (buttonRadius * 0.8).toInt())
            canvas.drawBitmap(scaledJumpIcon, width - buttonRadius * 3 - padding - scaledJumpIcon.width / 2, height - buttonRadius - padding - scaledJumpIcon.height / 2, null)
        }

        canvas.drawCircle(width - buttonRadius * 2 - padding, height - 3 * buttonRadius - 2 * padding, buttonRadius, buttonAttackPaint)
        attackIcon?.let {
            val scaledAttackIcon = scaleBitmap(it, (buttonRadius * 0.8).toInt(), (buttonRadius * 0.8).toInt())
            canvas.drawBitmap(scaledAttackIcon, width - buttonRadius * 2 - padding - scaledAttackIcon.width / 2, height - 3 * buttonRadius - 2 * padding - scaledAttackIcon.height / 2, null)
        }

        invalidate()
    }
}
