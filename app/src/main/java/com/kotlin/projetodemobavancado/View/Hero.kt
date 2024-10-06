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

enum class Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN
}

class Hero(context: Context, var x: Float, var y: Float, var HeroSpeed: Float) {
    private val soundPool: SoundPool;
    private var jumpSoundId: Int = 0;
    private var attackSoundId: Int = 0;
    private var heroBitmap: Bitmap = BitmapFactory.decodeStream(context.assets.open("hero1.png"));
    private var scaledHeroBitmap: Bitmap = Bitmap.createScaledBitmap(heroBitmap, 100*5, 150*5, false);
    private var heroBitmapAtack: Bitmap = BitmapFactory.decodeStream(context.assets.open("atk1.png"));
    private var scaledHeroBitmapAtack: Bitmap = Bitmap.createScaledBitmap(heroBitmapAtack, 100*5, 150*5, false);
    private var currentJumpFrame: Int = 0;
    private var currentWalkFrame: Int = 0;
    private var isJumping: Boolean = false
    private var isWalking: Boolean = false;
    private var isAtacking: Boolean = false;
    private var jumpHeight: Float = (scaledHeroBitmap.height.toFloat() / 3);
    private var initialY: Float = y;
    private var jumpSpeed: Float = 15f;
    private var jumpDirection: Int = 1;
    private var lastFrameChangeTime: Long = 0;
    private val frameDelay: Long = 170;
    private var jumpBitmaps: List<Bitmap> = listOf(
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("jump2.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("jump2.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("jump3.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("jump3.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("jump1.png")), 100 * 5, 150 * 5, false)
    );
    private var walkBitmaps: List<Bitmap> = listOf(
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("walk1.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("walk2.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("walk3.png")), 100 * 5, 150 * 5, false),
        Bitmap.createScaledBitmap(BitmapFactory.decodeStream(context.assets.open("walk4.png")), 100 * 5, 150 * 5, false),
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

    fun move(direction: Direction) {
        when (direction) {
            Direction.LEFT -> {
                x -= HeroSpeed
                isWalking = true
            }
            Direction.RIGHT -> {
                x += HeroSpeed
                isWalking = true
            }
            Direction.UP -> y = y
            Direction.DOWN -> y = y
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

    fun draw(canvas: Canvas) {
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
            else -> {
                canvas.drawBitmap(scaledHeroBitmap, x, y, null)
            }
        }
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
    }
}
