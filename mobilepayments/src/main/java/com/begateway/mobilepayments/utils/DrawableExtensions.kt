package com.begateway.mobilepayments.utils

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

internal fun Drawable.applyTintColorOnDrawable(
    color: Int,
    mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
): Drawable {
    val wrappedDrawable = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(wrappedDrawable, color)
    DrawableCompat.setTintMode(wrappedDrawable, mode)
    return wrappedDrawable
}