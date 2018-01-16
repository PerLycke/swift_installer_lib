package com.enrico.colorpicker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class CircleDrawable extends Drawable {

    private final Paint mInnerPaint;
    private final Paint mOuterPaint;
    private int mRadius = 0;

    public CircleDrawable(final int color) {
        mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerPaint.setColor(color);
        mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterPaint.setColor(shiftColorDown(color));
    }

    @ColorInt
    public static int shiftColor(@ColorInt int color,
                                 @FloatRange(from = 0.0f, to = 2.0f) float by) {
        if (by == 1f) {
            return color;
        }
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= by; // value component
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int shiftColorDown(@ColorInt int color) {
        return shiftColor(color, 0.9f);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Rect bounds = getBounds();
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), mRadius, mOuterPaint);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), mRadius - 5, mInnerPaint);
    }

    @Override
    protected void onBoundsChange(final Rect bounds) {
        super.onBoundsChange(bounds);
        mRadius = Math.min(bounds.width(), bounds.height()) / 2;
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        mInnerPaint.setAlpha(alpha);
        mOuterPaint.setAlpha(alpha);
    }

    public void setColor(int color) {
        if (color != mInnerPaint.getColor()) {
            mInnerPaint.setColor(color);
            mOuterPaint.setColor(shiftColorDown(color));
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
