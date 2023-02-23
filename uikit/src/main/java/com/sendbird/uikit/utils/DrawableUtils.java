package com.sendbird.uikit.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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

@SuppressWarnings("unused")
public class DrawableUtils {
    @NonNull
    public static Drawable createDividerDrawable(int height, int color) {
        GradientDrawable divider = new GradientDrawable();
        divider.setShape(GradientDrawable.RECTANGLE);
        divider.setSize(0, height);
        divider.setColor(color);
        return divider;
    }

    @NonNull
    public static Drawable createRoundedRectangle(float radius, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        drawable.setColor(color);
        return drawable;
    }

    @Nullable
    public static Drawable setTintList(@NonNull Context context, int resId, int colorRes) {
        if (colorRes == 0) {
            return AppCompatResources.getDrawable(context, resId);
        }
        return setTintList(AppCompatResources.getDrawable(context, resId), AppCompatResources.getColorStateList(context, colorRes));
    }

    @Nullable
    public static Drawable setTintList(@NonNull Context context, @NonNull Drawable drawable, int colorRes) {
        if (colorRes == 0) {
            return drawable;
        }
        return setTintList(drawable, AppCompatResources.getColorStateList(context, colorRes));
    }

    @Nullable
    public static Drawable setTintList(@NonNull Context context, int resId, @Nullable ColorStateList colorStateList) {
        return setTintList(AppCompatResources.getDrawable(context, resId), colorStateList);
    }

    @Nullable
    public static Drawable setTintList(@Nullable Drawable drawable, @Nullable ColorStateList colorStateList) {
        if (drawable == null || colorStateList == null) {
            return drawable;
        }
        drawable = DrawableCompat.wrap(drawable);
        Drawable mutated = drawable.mutate();
        DrawableCompat.setTintList(mutated, colorStateList);
        return mutated;
    }

    @NonNull
    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor,
                                          @DrawableRes int iconRes, @ColorRes int iconTint) {
        return createOvalIcon(context, backgroundColor, 255, iconRes, iconTint);
    }

    @NonNull
    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor, int backgroundAlpha,
                                          @DrawableRes int iconRes, @ColorRes int iconTint) {
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_24);
        return createOvalIcon(context, backgroundColor, 255, iconRes, iconTint, inset);
    }

    @NonNull
    public static Drawable createOvalIcon(@NonNull Context context, @ColorRes int backgroundColor, int backgroundAlpha,
                                          @DrawableRes int iconRes, @ColorRes int iconTint, int inset) {
        ShapeDrawable ovalBackground = new ShapeDrawable(new OvalShape());
        ovalBackground.getPaint().setColor(context.getResources().getColor(backgroundColor));
        ovalBackground.getPaint().setAlpha(backgroundAlpha);
        Drawable icon = setTintList(context, iconRes, iconTint);
        return createLayerIcon(ovalBackground, icon, inset);
    }

    @NonNull
    public static Drawable createLayerIcon(@Nullable Drawable background, @Nullable Drawable icon, int inset) {
        Drawable[] layer = {background, icon};
        LayerDrawable layerDrawable = new LayerDrawable(layer);
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);
        return layerDrawable;
    }

    @Nullable
    public static Bitmap toBitmap(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width == -1 || height == -1) return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
