package com.sendbird.uikit.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.R;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.PermissionUtils;

import java.util.List;
import java.util.Locale;

public abstract class PermissionFragment extends Fragment {
    interface IPermissionHandler {
        String[] getPermissions(int requestCode);
        void onPermissionGranted(int requestCode);
    }

    private int requestCode;
    private IPermissionHandler handler;
    private final int PERMISSION_SETTINGS_REQUEST_ID = 100;

    void checkPermission(int requestCode, @NonNull IPermissionHandler handler) {
        if (getContext() == null || getActivity() == null) return;
        this.requestCode = requestCode;
        this.handler = handler;
        final String[] permissions = handler.getPermissions(requestCode);
        // 1. check permission
        final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), permissions);
        if (hasPermission) {
            notifyPermissionResult(handler);
            return;
        }

        // 2. determine whether rationale popup should show
        final List<String> deniedList = PermissionUtils.getExplicitDeniedPermissionList(getActivity(), permissions);
        if (!deniedList.isEmpty()) {
            showPermissionRationalePopup(deniedList.get(0));
            return;
        }

        requestPermissions(permissions, requestCode);
    }

    private void notifyPermissionResult(@Nullable IPermissionHandler handler) {
        if (handler != null) {
            handler.onPermissionGranted(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final String[] requested = handler.getPermissions(requestCode);
        if (requestCode == this.requestCode && grantResults.length == requested.length) {
            boolean isAllGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                notifyPermissionResult(handler);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == PERMISSION_SETTINGS_REQUEST_ID) {
            if (handler != null) {
                String[] permissions = handler.getPermissions(this.requestCode);
                final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), permissions);
                if (hasPermission) {
                    handler.onPermissionGranted(this.requestCode);
                }
            }
        }
    }

    private void showPermissionRationalePopup(@NonNull String permission) {
        Drawable icon = getPermissionDrawable(requireActivity(), permission);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(requireContext().getString(R.string.sb_text_dialog_permission_title));
        builder.setMessage(getPermissionGuideMessage(requireContext(), permission));
        if (icon != null) {
            builder.setIcon(icon);
        }
        builder.setPositiveButton(R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_ID);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_300));
    }

    @Nullable
    private static Drawable getPermissionDrawable(@NonNull Activity activity, @NonNull String permission) {
        Drawable drawable = null;
        try {
            PackageManager pm = activity.getPackageManager();
            PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
            if (permissionInfo.group == null) return null;
            PermissionGroupInfo groupInfo = pm.getPermissionGroupInfo(permissionInfo.group, 0);
            drawable = ResourcesCompat.getDrawable(pm.getResourcesForApplication("android"), groupInfo.icon, activity.getTheme());
        } catch (Exception e) {
            //Logger.w(e);
        }
        return drawable;
    }

    @NonNull
    private static String getPermissionGuideMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        if (Manifest.permission.CAMERA.equals(permission)) {
            textResId = R.string.sb_text_need_to_allow_permission_camera;
        } else {
            textResId = R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }
}
