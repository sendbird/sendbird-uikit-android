package com.sendbird.uikit.internal

import android.graphics.Color
import androidx.annotation.ColorInt

internal interface RoundableView {
    var radius: Float
    fun setRadiusIntSize(radius: Int)
    fun setBorder(borderWidth: Int = 0, @ColorInt borderColor: Int = Color.TRANSPARENT)
}
