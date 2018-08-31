package com.brit.swiftinstaller.ui

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

class CircleDrawable(color: Int) : Drawable() {

    private val mInnerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mOuterPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRadius = 0

    init {
        mInnerPaint.color = color
        mOuterPaint.color = shiftColorDown(color)
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), mRadius.toFloat(), mOuterPaint)
        canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), (mRadius - 5).toFloat(), mInnerPaint)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mRadius = Math.min(bounds.width(), bounds.height()) / 2
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        mInnerPaint.alpha = alpha
        mOuterPaint.alpha = alpha
    }

    @Suppress("unused")
    fun setColor(color: Int) {
        if (color != mInnerPaint.color) {
            mInnerPaint.color = color
            mOuterPaint.color = shiftColorDown(color)
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    companion object {

        @Suppress("MemberVisibilityCanBePrivate")
        @ColorInt
        fun shiftColor(@ColorInt color: Int,
                       @FloatRange(from = 0.0, to = 2.0) by: Float): Int {
            if (by == 1f) {
                return color
            }
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= by // value component
            return Color.HSVToColor(hsv)
        }

        @ColorInt
        fun shiftColorDown(@ColorInt color: Int): Int {
            return shiftColor(color, 0.9f)
        }
    }
}
