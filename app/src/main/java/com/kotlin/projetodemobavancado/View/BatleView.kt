package com.kotlin.projetodemobavancado.View

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import kotlin.random.Random

class BattleView(context: Context) : View(context) {

    private val assetManager = context.assets
    private var backgroundBitmap: Bitmap? = null
    private val buttonJumpPaint = Paint()
    private val buttonAttackPaint = Paint()
    private val buttonSpecialPaint = Paint()
    private val buttonRadius = 150f
    private val padding = 50f
    private val jumpIcon = BitmapFactory.decodeStream(context.assets.open("ic_arrow_up.png"))
    private val attackIcon = BitmapFactory.decodeStream(context.assets.open("punch.png"))
    private val specialIcon = BitmapFactory.decodeStream(context.assets.open("explosion.png"))
    private val heroBitmap: Bitmap = BitmapFactory.decodeStream(context.assets.open("hero1.png"))
    private val bossBitmap: Bitmap = BitmapFactory.decodeStream(context.assets.open("hero1.png"))
    private val bossSpecial: Bitmap = BitmapFactory.decodeStream(context.assets.open("special-plants.png"))
    private val heroSpecial: Bitmap = BitmapFactory.decodeStream(context.assets.open("special-fire.png"))
    private var isPositionChanged: Boolean = false
    private var isJumpButtonPressed = false
    private var isAttackButtonPressed = false
    private var isSpecialButtonPressed = false
    private var initialX: Float = 0f
    private var HeroSpeed: Float = 20f
    private var BossSpeed: Float = 10f
    private var isMovingRight: Boolean = false
    private var isMovingLeft: Boolean = false
    private var position = 100f;
    private var positionBoss = context.resources.displayMetrics.widthPixels.toFloat() - 600;
    private val hero = Hero(context, position, (context.resources.displayMetrics.heightPixels - 730f).toFloat(), HeroSpeed)
    private val boss = Boss(context, positionBoss, (context.resources.displayMetrics.heightPixels - 730f).toFloat(), BossSpeed)
    private var heroHp = hero.vital
    private var bossHp = boss.vital
    private val redPaint = Paint().apply { color = Color.RED }
    private val greenPaint = Paint().apply { color = Color.GREEN }
    private var battleResult: Boolean? = null
    private var lastActionTime: Long = 0
    private val actionCooldown: Long = 1000
    private var lastSpecialTime: Long = 0
    private val specialCooldown: Long = 5000
    private var lastHeroActionTime: Long = 0
    private val actionHeroCooldown: Long = 1000
    private var lastHeroSpecialTime: Long = 0
    private val specialHeroCooldown: Long = 3000
    private var bossDistance: Float = 0f
    private val resultTextPaint = Paint().apply {
        color = Color.RED
        textSize = 100f
        textAlign = Paint.Align.CENTER
    }
    var isSpecialActive: Boolean = false
    var specialX: Float = 0f
    var specialY: Float = 0f
    val specialDuration: Long = 1500
    var specialStartTime: Long = 0
    var isBossSpecialActive: Boolean = false
    var specialBossX: Float = 0f
    var specialBossY: Float = 0f
    val specialBossDuration: Long = 2000
    var specialBossStartTime: Long = 0
    var isFightStart: Boolean = false


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

        resultTextPaint.style = Paint.Style.FILL
        buttonJumpPaint.color = Color.argb(150, 128, 128, 128)
        buttonSpecialPaint.color = Color.argb(150, 128, 128, 128)
        buttonAttackPaint.color = Color.argb(150, 128, 128, 128)
    }



    private fun checkBattleStatus() {

        if (heroHp <= 0) {
            battleResult = false
            resultTextPaint.color = Color.RED
        } else if (bossHp <= 0) {
            battleResult = true
            resultTextPaint.color = Color.GREEN
        } else {
            battleResult = null
            resultTextPaint.color = Color.GREEN
        }

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y


        val specialButtonX = width - buttonRadius * 4 - padding
        val specialButtonY = height - buttonRadius - padding
        val jumpButtonX = width - buttonRadius * 1 - padding * 2
        val jumpButtonY = height - buttonRadius - padding
        val attackButtonX = width - buttonRadius * 1 - padding * 2
        val attackButtonY = height - 3 * buttonRadius - 2 * padding



        if (battleResult != null && event.action == MotionEvent.ACTION_DOWN) {
            (context as? AppCompatActivity)?.let { activity ->
                activity.setContentView(StartView(activity))
            }
            return true
        }


        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                isJumpButtonPressed = isPointInsideCircle(x, y, jumpButtonX, jumpButtonY, buttonRadius)
                isAttackButtonPressed = isPointInsideCircle(x, y, attackButtonX, attackButtonY, buttonRadius)
                isSpecialButtonPressed = isPointInsideCircle(x, y, specialButtonX, specialButtonY, buttonRadius)

                if (isJumpButtonPressed) {
                    buttonJumpPaint.color = Color.argb(200, 255, 255, 255);
                    onJumpButtonClicked();
                }
                if (isAttackButtonPressed) {
                    buttonAttackPaint.color = Color.argb(200, 255, 255, 255);
                    onAttackButtonClicked();
                }
                if (isSpecialButtonPressed) {
                    buttonSpecialPaint.color = Color.argb(200, 255, 255, 255);
                    onSpecialButtonClicked();
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
                if (isSpecialButtonPressed) {
                    buttonSpecialPaint.color = Color.argb(150, 128, 128, 128)
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
                isFightStart == true
            }
            Direction.LEFT -> {
                if (position > 50f) {
                    isMovingLeft = true
                    isMovingRight = false
                    isFightStart == true
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
            isFightStart = true
            if (position + heroBitmap.width.toFloat() < context.resources.displayMetrics.widthPixels.toFloat()) {
                position += HeroSpeed
                hero.move(Direction.RIGHT)
            } else {
                stopMovement()
            }
        } else if (isMovingLeft) {
            isFightStart = true
            if (position > 50f) {
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

    private fun Boss.moveTowardsHero(heroX: Float, distanceThreshold: Float) {

        if (bossDistance > distanceThreshold) {
            if (heroX > x) {
                move(DirectionBoss.RIGHT)
            } else {
                move(DirectionBoss.LEFT)
            }
        }
    }

    private fun Boss.moveBackFromHero(heroX: Float, distanceThreshold: Float) {
        val distance = Math.abs(heroX - x)
        if (distance > distanceThreshold) {
            if (heroX > x) {
                if (x > heroX + 360) {
                    move(DirectionBoss.LEFT)
                }
            } else {
                if (x + bossBitmap.width.toFloat() < context.resources.displayMetrics.widthPixels.toFloat() - 100) {
                    move(DirectionBoss.RIGHT)
                }
            }
        }
    }

    fun Boss.meleeAttack() {
        if (System.currentTimeMillis() >= lastSpecialTime + specialCooldown) {
            lastSpecialTime = System.currentTimeMillis()
            lastActionTime = System.currentTimeMillis()
            specialAttack()
            postInvalidate()
        }else {
            if (System.currentTimeMillis() >= lastActionTime + actionCooldown) {
                attack()
                if (!hero.isJumping) {
                    if (heroHp > boss.streght * 1) {
                        heroHp -= (boss.streght * 1)
                        hero.stunned = true
                    } else {
                        heroHp = 0f
                        hero.stunned = true
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        hero.stunned = false
                    }, 1000)
                } else {
                    if (heroHp > boss.streght * 1) {
                        heroHp -= (boss.streght * 1)
                        hero.stunned = true
                    } else {
                        heroHp = 0f
                        hero.stunned = true
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        hero.stunned = false
                    }, 1000)
                }
                lastActionTime = System.currentTimeMillis()

            }
            Handler(Looper.getMainLooper()).postDelayed({
                hero.stunned = false
            }, 1000)

            postInvalidate()
        }
    }



    private fun randomMeleeAction(x: Float) {
        boss.isWalking = false
        boss.meleeAttack();
    }

    private fun randomRangeAction(heroX: Float) {
        if (System.currentTimeMillis() - lastSpecialTime > specialCooldown) {
            specialAttack()
            lastSpecialTime = System.currentTimeMillis()
        } else {
            if (bossDistance >= 350f) {
                if (System.currentTimeMillis() - lastActionTime > 100) {
                    val action = Random.nextInt(1, 21)
                    when (action) {
                        1 -> boss.moveTowardsHero(heroX, 50f)
                        2 -> boss.moveTowardsHero(heroX, 50f)
                        3 -> boss.jump()
                        4 -> boss.moveTowardsHero(heroX, 50f)
                        5 -> boss.moveTowardsHero(heroX, 50f)
                        6 -> boss.moveTowardsHero(heroX, 50f)
                        7 -> boss.moveTowardsHero(heroX, 50f)
                        8 -> boss.moveTowardsHero(heroX, 50f)
                        9 -> boss.moveTowardsHero(heroX, 50f)
                        10 -> boss.moveTowardsHero(heroX, 50f)
                        11 -> boss.moveTowardsHero(heroX, 50f)
                        12 -> boss.moveTowardsHero(heroX, 50f)
                        13 -> boss.moveTowardsHero(heroX, 50f)
                        14 -> boss.moveTowardsHero(heroX, 50f)
                        15 -> boss.moveTowardsHero(heroX, 50f)
                        16 -> boss.moveTowardsHero(heroX, 50f)
                        17 -> boss.moveTowardsHero(heroX, 50f)
                        18 -> boss.moveTowardsHero(heroX, 50f)
                        19 -> boss.moveTowardsHero(heroX, 50f)
                        20 -> boss.moveTowardsHero(heroX, 50f)
                    }

                    lastActionTime = System.currentTimeMillis()
                }

            }

        }
    }

    private fun ramdomLongAction(heroX: Float) {
        if (System.currentTimeMillis() - lastSpecialTime > specialCooldown) {
            specialAttack()
            lastSpecialTime = System.currentTimeMillis()
        } else {
            if (bossDistance >= 1199) {
                val action = Random.nextInt(1, 21)
                when (action) {
                    1 -> boss.moveTowardsHero(heroX, 50f)
                    2 -> boss.moveTowardsHero(heroX, 50f)
                    3 -> boss.jump()
                    4 -> boss.moveTowardsHero(heroX, 50f)
                    5 -> boss.moveTowardsHero(heroX, 50f)
                    6 -> boss.moveTowardsHero(heroX, 50f)
                    7 -> boss.moveTowardsHero(heroX, 50f)
                    8 -> boss.moveTowardsHero(heroX, 50f)
                    9 -> boss.moveTowardsHero(heroX, 50f)
                    10 -> boss.moveTowardsHero(heroX, 50f)
                    11 -> boss.moveTowardsHero(heroX, 50f)
                    12 -> boss.moveTowardsHero(heroX, 50f)
                    13 -> boss.moveTowardsHero(heroX, 50f)
                    14 -> boss.moveTowardsHero(heroX, 50f)
                    15 -> boss.moveTowardsHero(heroX, 50f)
                    16 -> boss.moveTowardsHero(heroX, 50f)
                    17 -> boss.moveTowardsHero(heroX, 50f)
                    18 -> boss.moveTowardsHero(heroX, 50f)
                    19 -> boss.moveTowardsHero(heroX, 50f)
                    20 -> boss.moveTowardsHero(heroX, 50f)
                }
                lastActionTime = System.currentTimeMillis()
            }else{
                if (System.currentTimeMillis() - lastActionTime > actionCooldown) {
                    val action = Random.nextInt(1, 6)
                    when (action) {
                        1 -> boss.moveBackFromHero(heroX, 50f)
                        2 -> boss.moveBackFromHero(heroX, 50f)
                        3 -> boss.jump()
                        4 -> boss.moveBackFromHero(heroX, 50f)
                        5 -> boss.moveBackFromHero(heroX, 50f)
                    }
                    lastActionTime = System.currentTimeMillis()
                }
            }
        }
    }

    fun activateBossSpecial() {
        specialBossX = boss.x + 360
        specialBossY = boss.y + 350
        specialBossStartTime = System.currentTimeMillis()
        isBossSpecialActive = true
    }

    private fun specialAttack() {
        boss.attack2()

        activateBossSpecial()
        if (bossDistance < 1700) {

            if (!hero.isJumping) {
                if (heroHp > boss.streght * 3) {
                    heroHp -= (boss.streght * 3)
                    hero.stunned = true
                } else {
                    heroHp = 0f
                    hero.stunned = true
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    hero.stunned = false
                }, 1000)
            } else {
                if (heroHp > boss.streght * 1) {
                    heroHp -= (boss.streght * 1)
                    hero.stunned = true
                } else {
                    heroHp = 0f
                    hero.stunned = true
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    hero.stunned = false
                }, 1000)
            }

            postInvalidate()
        }
    }

    fun distanceAtualizate () {
        bossDistance = Math.abs(hero.x - boss.x)
    }

    fun manageBossAI(heroX: Float) {

            when {
                bossDistance <= 360 -> {
                    randomMeleeAction(heroX)
                }
                bossDistance > 360 && bossDistance < 1199 -> {
                    randomRangeAction(heroX)
                }
                else -> {
                    ramdomLongAction(heroX)
                }
            }
    }

    private fun onJumpButtonClicked() {

        if (hero.stunned == false) {
            hero.jump();
        }
    }

    private fun onAttackButtonClicked() {
        isFightStart = true
        if (!hero.stunned) {
            if (System.currentTimeMillis() >= lastHeroActionTime + actionHeroCooldown) {
            lastHeroActionTime = System.currentTimeMillis();
            hero.attack();
            if (bossDistance < 380) {
                if (!boss.isJumping) {
                    if (bossHp > hero.streght * 1) {
                        bossHp -= hero.streght * 1
                        boss.stunned = true
                    } else {
                        bossHp = 0f
                        boss.stunned = true
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        boss.stunned = false
                    }, 1000)
                } else {
                    if (bossHp > hero.streght * 1) {
                        bossHp -= (hero.streght * 1)
                        boss.stunned = true
                    } else {
                        bossHp = 0f
                        boss.stunned = true
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        boss.stunned = false
                    }, 1000)
                }

                postInvalidate()
            }
            }
        }
        return

    }

    fun activateSpecial() {
        specialX = hero.x + 360
        specialY = hero.y + 350
        specialStartTime = System.currentTimeMillis()
        isSpecialActive = true
    }
    private fun onSpecialButtonClicked() {
        isFightStart = true
        if (!hero.stunned) {
            if (System.currentTimeMillis() >= lastHeroSpecialTime + specialHeroCooldown) {
            lastHeroSpecialTime = System.currentTimeMillis();
            activateSpecial()
            println("isSpecialActive: $isSpecialActive")

            hero.attack2()

            if (bossDistance < 830) {
                if (!boss.isJumping) {
                    if (bossHp > hero.streght * 3) {
                        bossHp -= (hero.streght * 3)
                        boss.stunned = true
                        println("Boss HP: $bossHp")
                    } else {
                        bossHp = 0f
                        boss.stunned = true
                        println("Boss HP: $bossHp")
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        boss.stunned = false
                    }, 1000)
                }

                postInvalidate()

                Handler(Looper.getMainLooper()).postDelayed({
                    isSpecialActive = false
                    postInvalidate()
                }, 500)
            }
            }
        }
    }


    private fun positionCheck () {
        if (hero.x > boss.x) {
            isPositionChanged = true
        }else{
            isPositionChanged= false
        }
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

        boss.update()
        hero.update()

        boss.draw(canvas, !isPositionChanged)
        hero.draw(canvas, isPositionChanged)


        distanceAtualizate()

        drawHealthBars(canvas, bossHp, heroHp, boss.vital, hero.vital)


        checkBattleStatus()

        positionCheck()

        val currentTime = System.currentTimeMillis()
        if (isSpecialActive && currentTime - specialStartTime > 1100) {
            isSpecialActive = false
        }
        if (isBossSpecialActive && currentTime - specialBossStartTime > 1000) {
            isBossSpecialActive = false
        }

        if (isSpecialActive) {
            val currentTime = System.currentTimeMillis()
            if (currentTime < specialStartTime + specialDuration) {
                heroSpecial?.let { special ->
                    var left = 0;
                    val scaleFactor = 1.5f
                    val scaledWidth = (special.width * scaleFactor).toInt()
                    val scaledHeight = (special.height * scaleFactor).toInt()
                    var top = (specialY - scaledHeight / 2).toInt() + 100;
                    if (!isPositionChanged) {
                         left = (specialX - scaledWidth / 2).toInt() + 300
                    }else{
                         left = (specialX - scaledWidth).toInt() -220

                    }

                    val right = left + scaledWidth
                    val bottom = top + scaledHeight

                    canvas.drawBitmap(special, null, Rect(left, top, right, bottom), null)
                }
            } else {
                isSpecialActive = false
            }
        }

        if (isBossSpecialActive) {
            val currentTime = System.currentTimeMillis()
            if (currentTime < specialBossStartTime + specialBossDuration) {
                bossSpecial?.let { special ->
                    var left = 0;
                    val scaleFactor = 2f
                    val scaledWidth = (special.width * scaleFactor).toInt()
                    val scaledHeight = (special.height * scaleFactor).toInt()
                    if (!isPositionChanged) {
                        left = (specialBossX - scaledWidth).toInt() -200
                    }else{
                        left = (specialBossX).toInt() + 100
                    }
                    val top = (specialBossY - scaledHeight / 2).toInt() +50
                    val right = left + scaledWidth
                    val bottom = top + scaledHeight
                    println(scaledWidth)

                    canvas.drawBitmap(special, null, Rect(left, top, right, bottom), null)
                }
            } else {
                isBossSpecialActive = false
            }
        }

        if (battleResult != null) {
            drawResultModal(canvas)
        } else {
            drawActionButtons(canvas)
            updateHeroMovement()
            if (isFightStart == true) {
                manageBossAI(hero.x)
            }
        }


        if (lastHeroSpecialTime > 0 &&  System.currentTimeMillis() < lastHeroSpecialTime + specialHeroCooldown) {
            buttonSpecialPaint.color = Color.argb(50, 228, 113, 122)
        }else{
            buttonSpecialPaint.color = Color.argb(150, 128, 128, 128)
        }

        invalidate()
    }

    private fun drawHealthBars(canvas: Canvas, bossHp: Float, heroHp: Float, maxBossHp: Float, maxHeroHp: Float) {
        val barHeight = 60f
        val barWidth = (width.toFloat() - 120f) / 2f
        val topPadding = 40f
        val bossLeft = 60f + barWidth
        val normalizedHeroHp = (heroHp / maxHeroHp) * 100f
        val normalizedBossHp = (bossHp / maxBossHp) * 100f
        val clampedHeroHp = normalizedHeroHp.coerceIn(0f, 100f)
        val clampedBossHp = normalizedBossHp.coerceIn(0f, 100f)
        val heroHealthWidth = barWidth * (clampedHeroHp / 100f)
        val bossHealthWidth = barWidth * (clampedBossHp / 100f)

        canvas.drawRect(40f, topPadding, 40f + barWidth, topPadding + barHeight, redPaint)
        canvas.drawRect(bossLeft, topPadding, bossLeft + barWidth, topPadding + barHeight, redPaint)
        canvas.drawRect(barWidth - heroHealthWidth + 40f, topPadding, 40f + barWidth, topPadding + barHeight, greenPaint)
        canvas.drawRect(bossLeft, topPadding, bossLeft + bossHealthWidth, topPadding + barHeight, greenPaint)
    }



    private fun drawActionButtons(canvas: Canvas) {
        canvas.drawCircle(width - buttonRadius * 4 - padding, height - buttonRadius - padding, buttonRadius, buttonSpecialPaint)
        specialIcon?.let {
            val scaledJumpIcon = scaleBitmap(it, (buttonRadius * 0.8).toInt(), (buttonRadius * 0.8).toInt())
            canvas.drawBitmap(scaledJumpIcon, width - buttonRadius * 4 - padding - scaledJumpIcon.width / 2, height - buttonRadius - padding - scaledJumpIcon.height / 2, null)
        }
        canvas.drawCircle(width - buttonRadius * 1 - padding * 2, height - buttonRadius - padding, buttonRadius, buttonJumpPaint)
        jumpIcon?.let {
            val scaledJumpIcon = scaleBitmap(it, (buttonRadius * 0.8).toInt(), (buttonRadius * 0.8).toInt())
            canvas.drawBitmap(scaledJumpIcon, width - buttonRadius * 1 - padding * 2 - scaledJumpIcon.width / 2, height - buttonRadius - padding - scaledJumpIcon.height / 2, null)
        }
        canvas.drawCircle(width - buttonRadius * 1 - padding * 2, height - 3 * buttonRadius - 2 * padding, buttonRadius, buttonAttackPaint)
        attackIcon?.let {
            val scaledAttackIcon = scaleBitmap(it, (buttonRadius * 0.8).toInt(), (buttonRadius * 0.8).toInt())
            canvas.drawBitmap(scaledAttackIcon, width - buttonRadius * 1 - padding * 2 - scaledAttackIcon.width / 2, height - 3 * buttonRadius - 2 * padding - scaledAttackIcon.height / 2, null)
        }
    }

    private fun drawResultModal(canvas: Canvas) {
        val modalWidth = width * 0.4f
        val modalHeight = height * 0.2f
        val left = (width - modalWidth) / 2
        val top = (height - modalHeight) / 2
        val modalPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(left, top, left + modalWidth, top + modalHeight, modalPaint)
        val resultText = if (battleResult == true) "YOU WIN" else "YOU LOSE"
        val textColor = if (battleResult == true) Color.GREEN else Color.RED
        resultTextPaint.color = textColor
        canvas.drawText(resultText, width / 2f, height / 1.9f, resultTextPaint)
    }
}
