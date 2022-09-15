package com.sendbird.uikit_messaging_android.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit_messaging_android.R;

public class WaitingDialog {
    private final static String TAG = "logger";
    private final static Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final Object waitingDialogLock = new Object();
    private static Dialog waitingDialog;


    private static Dialog getWaitingDialog(@NonNull Context context) {
        synchronized (waitingDialogLock) {
            if (waitingDialog != null) {
                return waitingDialog;
            }

            waitingDialog = new Dialog(context, R.style.Widget_Sendbird_SendbirdProgressDialog);
            return waitingDialog;
        }
    }

    public static void show(@NonNull Context context) {
        show(context, false);
    }

    public static void show(@NonNull Context context, int layoutResId) {
        show(context, false, layoutResId, null);
    }

    public static void show(@NonNull Context context, final boolean cancelable) {
        show(context, cancelable, 0, null);
    }

    public static void show(@NonNull Context context, final boolean cancelable, final int layoutResId, @Nullable DialogInterface.OnCancelListener listener) {
        dismiss();

        mainHandler.post(() -> {
            Log.d(TAG, ">> WaitingDialog::show()");
            waitingDialog = getWaitingDialog(context);
            // here we set layout of progress dialog
            if (layoutResId <= 0) {
                waitingDialog.setContentView(R.layout.sb_view_waiting_dialog);
            } else {
                waitingDialog.setContentView(layoutResId);
            }
            waitingDialog.setCancelable(cancelable);
            if (listener != null) {
                waitingDialog.setOnCancelListener(listener);
            }
            waitingDialog.show();
        });
    }

    public static void dismiss() {
        mainHandler.post(() -> {
            try {
                Log.d(TAG, ">> WaitingDialog::cancel()");
                if (waitingDialog != null) {
                    synchronized (waitingDialogLock) {
                        waitingDialog.cancel();
                        waitingDialog = null;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        });
    }
}
