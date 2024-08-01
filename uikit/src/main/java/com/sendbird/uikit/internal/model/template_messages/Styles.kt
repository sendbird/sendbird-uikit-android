package com.sendbird.uikit.internal.model.template_messages

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.extensions.loadToBackground
import com.sendbird.uikit.internal.extensions.setTypeface
import com.sendbird.uikit.internal.interfaces.ViewRoundable
import com.sendbird.uikit.internal.model.serializer.ColorIntAsStringSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class TextStyle(
    val size: Int? = null,
    @ColorInt
    @Serializable(with = ColorIntAsStringSerializer::class)
    val color: Int? = null,
    val weight: Weight? = null
) {
    fun apply(view: TextView): TextStyle {
        size?.let { view.setTextSize(TypedValue.COMPLEX_UNIT_SP, it.toFloat()) }
        color?.let { view.setTextColor(it) }
        weight?.let { view.setTypeface(it.value) }
        return this
    }
}

@Serializable
internal data class ImageStyle(
    val contentMode: ContentMode? = null,
    @Serializable(with = ColorIntAsStringSerializer::class)
    val tintColor: Int? = null,
) {
    fun apply(view: ImageView): ImageStyle {
        contentMode?.let { view.scaleType = it.scaleType }
        tintColor?.let { view.setColorFilter(it, PorterDuff.Mode.SRC_ATOP) }
        return this
    }
}

@Serializable
internal data class ViewStyle(
    @ColorInt
    @Serializable(with = ColorIntAsStringSerializer::class)
    val backgroundColor: Int? = null,
    val backgroundImageUrl: String? = null,
    val borderWidth: Int? = null,
    @ColorInt
    @Serializable(with = ColorIntAsStringSerializer::class)
    val borderColor: Int? = null,
    val radius: Int? = null,
    val margin: Margin? = null,
    val padding: Padding? = null
) {
    fun apply(view: View, useRipple: Boolean = false): ViewStyle {
        if (backgroundColor != null || (borderWidth != null && borderWidth > 0)) {
            view.setBackgroundColor(backgroundColor ?: Color.TRANSPARENT)
        }

        // backgroundImageUrl has higher priority than backgroundColor (platform synced)
        backgroundImageUrl?.let { view.loadToBackground(it, radius ?: 0, useRipple) }

        margin?.apply(view)
        padding?.apply(view)

        if (view is ViewRoundable) {
            radius?.let { view.setRadiusIntSize(it) }
            borderWidth?.let { view.setBorder(borderWidth, borderColor ?: Color.TRANSPARENT) }
        }
        return this
    }
}

@Serializable
internal data class Margin(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
) {
    fun apply(view: View) {
        val resources = view.context.resources
        val layoutParams = view.layoutParams as LinearLayout.LayoutParams
        view.layoutParams = layoutParams.also {
            it.topMargin = resources.intToDp(top)
            it.bottomMargin = resources.intToDp(bottom)
            it.marginStart = resources.intToDp(left)
            it.marginEnd = resources.intToDp(right)
        }
    }
}

@Serializable
internal data class Padding(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
) {
    fun apply(view: View) {
        val resources = view.context.resources
        view.setPaddingRelative(
            resources.intToDp(left),
            resources.intToDp(top),
            resources.intToDp(right),
            resources.intToDp(bottom)
        )
    }
}
