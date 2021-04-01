package com.sendbird.uikit.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.sendbird.uikit.widgets.ToastView;

public class ContextUtils {
    public static String getApplicationName(@NonNull Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public static void toastSuccess(Context context, @StringRes int text) {
        if (context == null) {
            return;
        }
        ToastView toastView = new ToastView(context);
        toastView.setStatus(ToastView.ToastStatus.SUCCESS);
        toastView.setText(text);
        showToast(context, toastView);
    }

    public static void toastSuccess(Context context, CharSequence text) {
        if (context == null) {
            return;
        }
        ToastView toastView = new ToastView(context);
        toastView.setStatus(ToastView.ToastStatus.SUCCESS);
        toastView.setText(text);
        showToast(context, toastView);
    }

    public static void toastError(Context context, @StringRes int resId) {
        if (context == null) {
            return;
        }
        ToastView toastView = new ToastView(context);
        toastView.setStatus(ToastView.ToastStatus.ERROR);
        toastView.setText(resId);
        showToast(context, toastView);
    }

    public static void toastError(Context context, String message) {
        if (context == null) {
            return;
        }
        ToastView toastView = new ToastView(context);
        toastView.setStatus(ToastView.ToastStatus.ERROR);
        toastView.setText(message);
        showToast(context, toastView);
    }

    private static void showToast(Context context, View toastView) {
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastView);
        toast.show();
    }
}
