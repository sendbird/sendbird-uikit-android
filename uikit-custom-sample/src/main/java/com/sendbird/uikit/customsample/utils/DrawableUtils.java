package com.sendbird.uikit.customsample.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.sendbird.uikit.R;

/**
 * This provides methods to draw icon and color.
 */
public class DrawableUtils {
    @Nullable
    public static Drawable setTintList(@NonNull Context context, int resId, int colorRes) {
        if (colorRes == 0) {
            return AppCompatResources.getDrawable(context, resId);
        }
        return setTintList(AppCompatResources.getDrawable(context, resId), AppCompatResources.getColorStateList(context, colorRes));
    }

    @Nullable
    public static Drawable setTintList(@Nullable Drawable drawable, @Nullable ColorStateList colorStateList) {
        if (drawable == null || colorStateList == null) {
            return drawable;
        }
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, colorStateList);
        return drawable.mutate();
    }

    @NonNull
    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor,
                                          @DrawableRes int iconRes, @ColorRes int iconTint) {
        return createOvalIcon(context, backgroundColor, 255, iconRes, iconTint);
    }

    @NonNull
    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor, int backgroundAlpha,
                                          @DrawableRes int iconRes, @ColorRes int iconTint) {
        ShapeDrawable ovalBackground = new ShapeDrawable(new OvalShape());
        ovalBackground.getPaint().setColor(context.getResources().getColor(backgroundColor));
        ovalBackground.getPaint().setAlpha(backgroundAlpha);
        Drawable icon = setTintList(context, iconRes, iconTint);
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_24);
        return createLayerIcon(ovalBackground, icon, inset);
    }

    @NonNull
    public static Drawable createLayerIcon(@NonNull Drawable background, @Nullable Drawable icon, int inset) {
        Drawable[] layer = {background, icon};
        LayerDrawable layerDrawable = new LayerDrawable(layer);
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }
}
