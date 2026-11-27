package com.wavez.base.extension

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Sets the height of a view as a percentage of the screen height using LayoutParams.
 *
 * @param heightPercent An integer representing the desired height as a percentage of the screen height.
 *                      For example, passing 60 will set the view's height to 60% of the screen height.
 *
 * This function calculates the desired height based on the screen's total height and modifies
 * the view's LayoutParams accordingly. It is useful for dynamically resizing views based on
 * screen size while maintaining responsive UI design.
 *
 * Usage example:
 * ```kotlin
 * val layoutTheme = findViewById<View>(R.id.layout_theme)
 * layoutTheme.setLayoutThemeHeightUsingLayoutParams(60) // Sets height to 60% of screen height
 * ```
 *
 * Notes:
 * - Ensure this function is called after the view has been initialized (e.g., in `onCreate` or `onViewCreated`).
 * - This function only modifies the `height` property of the LayoutParams; other properties remain unchanged.
 */
fun View.setLayoutThemeHeightUsingLayoutParams(heightPercent: Int) {
    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
    val newHeight = (screenHeight * (heightPercent / 100f)).toInt()
    val layoutParams = this.layoutParams
    layoutParams.height = newHeight
    this.layoutParams = layoutParams
}


fun View.onAvoidDoubleClick(
    throttleDelay: Long = 600L,
    onClick: (View) -> Unit,
) {
    setOnClickListener {
        onClick(this)
        isClickable = false
        postDelayed({ isClickable = true }, throttleDelay)
    }
}

fun View.setShaderGradient(colors: IntArray, isVertical: Boolean = false) {
    post {
        if (colors.size == 1) {
            setBackgroundColor(colors[0])
        } else {
            val shader = LinearGradient(
                0f,
                0f,
                if (isVertical) 0f else width.toFloat(),
                if (isVertical) height.toFloat() else 0f,
                colors,
                null,
                Shader.TileMode.CLAMP
            )

            val paint = Paint().apply { this.shader = shader }

            background = object : Drawable() {
                override fun draw(canvas: Canvas) {
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                }

                override fun setAlpha(alpha: Int) {}
                override fun setColorFilter(colorFilter: ColorFilter?) {}
                override fun getOpacity(): Int = PixelFormat.OPAQUE
            }
        }
    }
}
