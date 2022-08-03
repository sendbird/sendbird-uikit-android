package com.sendbird.uikit.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PermissionUtils {

    public final static String[] CAMERA_PERMISSION = getCameraPermission();
    public final static String[] GET_CONTENT_PERMISSION = getGetContentPermission();

    private PermissionUtils() {
    }

    private static String[] getCameraPermission() {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            permissions = new String[]{Manifest.permission.CAMERA};
        }
        return permissions;
    }

    private static String[] getGetContentPermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[] {};
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        return permissions;
    }
    public static boolean hasPermissions(Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (PermissionChecker.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String[] getNotGrantedPermissions(Context context, @NonNull String... permissions) {
        ArrayList<String> notGrantedPermissions = new ArrayList<>();
        for (String perm : permissions) {
            if (!PermissionUtils.hasPermissions(context, perm)) {
                notGrantedPermissions.add(perm);
            }
        }
        return notGrantedPermissions.toArray(new String[]{});
    }

    public static List<String> getShowRequestPermissionRationale(Activity activity, @NonNull String... permissions) {
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

    @NonNull
    public static List<String> getExplicitDeniedPermissionList(@NonNull Activity activity, @NonNull Collection<String> permissions) {
        List<String> result = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                result.add(permission);
            }
        }
        return result;
    }

    @NonNull
    public static List<String> getExplicitDeniedPermissionList(@NonNull Activity activity, @NonNull String... permissions) {
        return getExplicitDeniedPermissionList(activity, Arrays.asList(permissions));
    }
}
