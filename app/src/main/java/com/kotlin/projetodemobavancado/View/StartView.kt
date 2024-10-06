package com.kotlin.projetodemobavancado.View

import android.content.Context
import android.graphics.*
import android.widget.Button
import android.widget.FrameLayout
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import com.kotlin.projetodemobavancado.R

class StartView(context: Context) : ConstraintLayout(context) {

    init {

        setWillNotDraw(false)

        val startButton = Button(context).apply {
            text = "Start Game"
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF6200EE"))
            setPadding(40, 20, 40, 20)
            background = context.getDrawable(R.drawable.rounded_button)

            setOnClickListener {
                (context as AppCompatActivity).setContentView(BattleView(context))
            }
        }


        startButton.id = ViewCompat.generateViewId()
        addView(startButton)

        val constraintSet = ConstraintSet().apply {
            clone(this@StartView)
            connect(startButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            connect(startButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
            connect(startButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            connect(startButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        }
        constraintSet.applyTo(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.parseColor("#000000"), Color.parseColor("#0F2027"),
                Shader.TileMode.CLAMP
            )
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
