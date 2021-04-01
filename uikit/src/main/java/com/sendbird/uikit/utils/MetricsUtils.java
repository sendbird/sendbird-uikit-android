
package com.sendbird.uikit.utils;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class MetricsUtils {

    private final static int DEFAULT_DENSITY_DPI = 240;
    private final static float DEFAULT_DENSITY = 1.5f;

    private MetricsUtils() {
    }

    public static int getDensityDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getDefaultDensity() {
        return DEFAULT_DENSITY_DPI;
    }

    public static int dipToPixel(Context context, float dip) {
        return (int) (dip * getDensity(context));
    }

    public static float pixelInDensityF(Context context, int pixel) {
        return pixel / DEFAULT_DENSITY * getDensity(context);
    }

    public static int pixelInDensity(Context context, int pixel) {
        return (int) pixelInDensityF(context, pixel);
    }

    public static float spToPixel(Context context, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static RectF rotateRect(RectF originRect, float pivotX, float pivotY, double degree) {
        double angle = Math.toRadians(degree);

        float pointLeftTopX = (originRect.left-pivotX) * (float)Math.cos(angle) - (originRect.top-pivotY) * (float)Math.sin(angle) + pivotX;
        float pointLeftTopY = (originRect.left-pivotX) * (float)Math.sin(angle) + (originRect.top-pivotY) * (float)Math.cos(angle) + pivotY;

        float pointRightBottomX = (originRect.right-pivotX) * (float)Math.cos(angle)
                - (originRect.bottom-pivotY) * (float)Math.sin(angle) + pivotX;
        float pointRightBottomY = (originRect.right-pivotX) * (float)Math.sin(angle)
                + (originRect.bottom-pivotY) * (float)Math.cos(angle) + pivotY;

        return new RectF( Math.min(pointLeftTopX, pointRightBottomX),
                Math.min(pointLeftTopY, pointRightBottomY),
                Math.max(pointLeftTopX, pointRightBottomX),
                Math.max(pointLeftTopY, pointRightBottomY));
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static Pair<Integer, Integer> getScreenSize(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return new Pair(point.x, point.y);
    }
}
