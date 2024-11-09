package com.kotlin.projetodemobavancado.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.media.SoundPool
import android.media.AudioAttributes
import java.io.FileNotFoundException

enum class DirectionBoss {
    LEFT,
    RIGHT,
    UP,
    DOWN
}

class Boss(context: Context, var x: Float, var y: Float, var HeroSpeed: Float) {

    var stunned: Boolean = false
    var vital: Float = 2000f
    var streght: Int = 50

    private val soundPool: SoundPool;
    private var jumpSoundId: Int = 0;
    private var attackSoundId: Int = 0;
    private var specialSoundId: Int = 0;
    private var heroBitmap: Bitmap = BitmapFactory.decodeStream(context.assets.open("boss1.png"));
    private var scaledHeroBitmap: Bitmap = Bitmap.createScaledBitmap(heroBitmap, 100*5, 150*5, false);
    private var heroBitmapAtack: Bitmap = BitmapFactory.decodeStream(context.assets.open("batk1.png"));
    private var heroBitmapStun: Bitmap = BitmapFactory.decodeStream(context.assets.open("stun-boss.png"));
    private var scaledHeroBitmapAtack: Bitmap = Bitmap.createScaledBitmap(heroBitmapAtack, 100*5, 150*5, false);
    private var scaledHeroBitmapStun: Bitmap = Bitmap.createScaledBitmap(heroBitmapStun, 100*5, 150*5, false);
    private var currentJumpFrame: Int = 0;
    private var currentWalkFrame: Int = 0;
    private var currentSpecialFrame: Int = 0;
    var isJumping: Boolean = false
    var isWalking: Boolean = false;
    private var isAtacking: Boolean = false;
    private var isSpecial: Boolean = false;
    private var jumpHeight: Float = (scaledHeroBitmap.height.toFloat() / 3);
    private var initialY: Float = y;
    private var jumpSpeed: Float = 15f;
    private var jumpDirection: Int = 1;
    private var lastFrameChangeTime: Long = 0;
    private val frameDelay: Long = 170;
    private val specialDelay: Long = 100;
    private var jumpBitmaps: List<Bitmap> = listOf(
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bjump2.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bjump2.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bjump3.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bjump3.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bjump1.png")), 100 * 5, 150 * 5, false)
    );
    private var walkBitmaps: List<Bitmap> = listOf(
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bwalk1.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bwalk.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bwalk3.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bwalk4.png")), 100 * 5, 150 * 5, false),
    );
    private var specialBitmaps: List<Bitmap> = listOf(
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bsp1.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bsp1.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("bsp2.png")), 100 * 5, 150 * 5, false),

    );

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        jumpSoundId = loadSoundFromAssets(context, "jump.mp3")
        attackSoundId = loadSoundFromAssets(context, "punch.mp3")
        specialSoundId = loadSoundFromAssets(context, "tree-sound.mp3")
    }

    private fun loadSoundFromAssets(context: Context, fileName: String): Int {
        return try {
            val assetManager = context.assets
            val afd = assetManager.openFd(fileName)
            soundPool.load(afd, 1)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            println(e)
            0
        }
    }

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)  // play(soundId, leftVolume, rightVolume, priority, loop, rate)
    }

    fun releaseSoundPool() {
        soundPool.release()
    }

    fun jump() {
        if (!isJumping) {
            playSound(jumpSoundId)
            isJumping = true
            currentJumpFrame = 0
            lastFrameChangeTime = System.currentTimeMillis()
        }
    }

    fun move(direction: DirectionBoss) {
        when (direction) {
            DirectionBoss.LEFT -> {
                x -= HeroSpeed
                isWalking = true
            }
            DirectionBoss.RIGHT -> {
                x += HeroSpeed
                isWalking = true
            }
            DirectionBoss.UP -> {
                y = y
            }
            DirectionBoss.DOWN -> {
                y = y
            }
        }

        if (direction != DirectionBoss.LEFT && direction != DirectionBoss.RIGHT) {
            isWalking = false
        }
    }


    fun stopWalking() {
        isWalking = false
        currentWalkFrame = 0
    }

    fun attack() {
        if (!isAtacking) {
            isAtacking = true

            playSound(attackSoundId)
            Handler(Looper.getMainLooper()).postDelayed({
                isAtacking = false
            }, 150)
        }
    }

    fun attack2() {
        if (!isSpecial) {
            isSpecial = true
            playSound(specialSoundId)
            currentSpecialFrame = 0
            lastFrameChangeTime = System.currentTimeMillis()
            Handler(Looper.getMainLooper()).postDelayed({
                isSpecial = false
            }, 900)
        }
    }

    fun draw(canvas: Canvas, isFacingLeft: Boolean) {
        canvas.save()

        if (isFacingLeft) {
            canvas.scale(-1f, 1f, x + (scaledHeroBitmap.width / 2), y + (scaledHeroBitmap.height / 2))
        }

        when {
            isJumping -> {
                canvas.drawBitmap(jumpBitmaps[currentJumpFrame], x, y, null)
            }
            isWalking -> {
                canvas.drawBitmap(walkBitmaps[currentWalkFrame], x, y, null)
            }
            isAtacking -> {
                canvas.drawBitmap(scaledHeroBitmapAtack, x, y, null)
            }
            stunned -> {
                canvas.drawBitmap(scaledHeroBitmapStun, x, y, null)
            }
            isSpecial -> {
                canvas.drawBitmap(specialBitmaps[currentSpecialFrame], x, y, null)
            }
            else -> {
                canvas.drawBitmap(scaledHeroBitmap, x, y, null)
            }
        }

        canvas.restore()
    }

    fun update() {
        if (isJumping) {
            y -= jumpSpeed * jumpDirection

            if (System.currentTimeMillis() - lastFrameChangeTime > frameDelay) {
                if (currentJumpFrame < jumpBitmaps.size - 1) {
                    currentJumpFrame++
                } else {
                    currentJumpFrame = 0
                }
                lastFrameChangeTime = System.currentTimeMillis()
            }

            if (y <= initialY - jumpHeight) {
                jumpDirection = -1
            }

            if (y >= initialY) {
                y = initialY
                isJumping = false
                currentJumpFrame = 0
                jumpDirection = 1
            }
        }
        if (isWalking) {
            if (System.currentTimeMillis() - lastFrameChangeTime > frameDelay) {
                if (currentWalkFrame < walkBitmaps.size - 1) {
                    currentWalkFrame++
                } else {
                    currentWalkFrame = 0
                }
                lastFrameChangeTime = System.currentTimeMillis()
            }
        }
        if (isSpecial) {
            if (System.currentTimeMillis() - lastFrameChangeTime > specialDelay) {
                if (currentSpecialFrame < specialBitmaps.size - 1) {
                    currentSpecialFrame++
                } else {
                    currentSpecialFrame = 0
                }
                lastFrameChangeTime = System.currentTimeMillis()
            }
        }
    }
}