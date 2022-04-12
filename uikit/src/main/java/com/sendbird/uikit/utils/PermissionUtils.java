package com.sendbird.uikit.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    private PermissionUtils() {
    }

    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @NonNull
    public static String[] getNotGrantedPermissions(@NonNull Context context, @NonNull String... permissions) {
        ArrayList<String> notGrantedPermissions = new ArrayList<>();
        for (String perm : permissions) {
            if (!PermissionUtils.hasPermissions(context, perm)) {
                notGrantedPermissions.add(perm);
            }
        }
        return notGrantedPermissions.toArray(new String[]{});
    }

    @NonNull
    public static List<String> getShowRequestPermissionRationale(@NonNull Activity activity, @NonNull String... permissions) {
        List<String> result = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    result.add(permission);
                }
            }
        }
        return result;
    }
}
