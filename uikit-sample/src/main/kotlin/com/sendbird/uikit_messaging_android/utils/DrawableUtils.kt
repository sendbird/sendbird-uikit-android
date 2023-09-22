package com.sendbird.uikit_messaging_android.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.sendbird.uikit.R

/**
 * This provides methods to draw icon and color.
 */
object DrawableUtils {
    fun setTintList(context: Context, resId: Int, colorRes: Int): Drawable? {
        return if (colorRes == 0) {
            AppCompatResources.getDrawable(context, resId)
        } else setTintList(
            AppCompatResources.getDrawable(
                context,
                resId
            ) ?: return null, AppCompatResources.getColorStateList(context, colorRes)
        )
    }

    private fun setTintList(drawable: Drawable, colorStateList: ColorStateList?): Drawable? {
        var result = drawable
        result = DrawableCompat.wrap(result)
        DrawableCompat.setTintList(result, colorStateList)
        return result.mutate()
    }

    fun createOvalIcon(
        context: Context, @ColorRes backgroundColor: Int,
        @DrawableRes iconRes: Int, @ColorRes iconTint: Int
    ): Drawable {
        return createOvalIcon(context, backgroundColor, 255, iconRes, iconTint)
    }

    private fun createOvalIcon(
        context: Context, @ColorRes backgroundColor: Int, backgroundAlpha: Int,
        @DrawableRes iconRes: Int, @ColorRes iconTint: Int
    ): Drawable {
        val ovalBackground = ShapeDrawable(OvalShape())
        ovalBackground.paint.color = context.resources.getColor(backgroundColor)
        ovalBackground.paint.alpha = backgroundAlpha
        val icon = setTintList(context, iconRes, iconTint)
        val inset = context.resources.getDimension(R.dimen.sb_size_24).toInt()
        return createLayerIcon(ovalBackground, icon, inset)
    }

    private fun createLayerIcon(background: Drawable, icon: Drawable?, inset: Int): Drawable {
        val layer = arrayOf(background, icon)
        val layerDrawable = LayerDrawable(layer)
        layerDrawable.setLayerInset(1, inset, inset, inset, inset)
        return layerDrawable
    }
}
