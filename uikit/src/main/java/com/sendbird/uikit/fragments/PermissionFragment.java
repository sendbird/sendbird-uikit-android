package com.sendbird.uikit.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.R;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.PermissionUtils;

import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public abstract class PermissionFragment extends Fragment {
    interface IPermissionHandler {
        String[] getPermissions(int requestCode);
        void onPermissionGranted(int requestCode);
    }

    private int requestCode;
    private IPermissionHandler handler;
    private final int PERMISSION_SETTINGS_REQUEST_ID = 100;

    void checkPermission(int requestCode, @NonNull IPermissionHandler handler) {
        this.requestCode = requestCode;
        this.handler = handler;
        final String[] permissions = handler.getPermissions(requestCode);
        final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), permissions);
        if (hasPermission) {
            handler.onPermissionGranted(requestCode);
            return;
        }

        requestPermissions(permissions, requestCode);
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
                handler.onPermissionGranted(requestCode);
            } else {
                String[] notGranted = PermissionUtils.getNotGrantedPermissions(getContext(), permissions);
                List<String> deniedList = PermissionUtils.getShowRequestPermissionRationale(getActivity(), permissions);
                if (deniedList != null && deniedList.size() == 0) {
                    Drawable icon = getPermissionDrawable(getActivity(), notGranted[0]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getActivity().getString(R.string.sb_text_dialog_permission_title));
                    builder.setMessage(getPermissionGuideMessage(getActivity(), notGranted[0]));
                    if (icon != null) {
                        builder.setIcon(icon);
                    }
                    builder.setPositiveButton(R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_ID);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.secondary_300));
                }
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
                    handler.onPermissionGranted(requestCode);
                }
            }
        }
    }

    private static Drawable getPermissionDrawable(Activity activity, String permission) {
        Drawable drawable = null;
        try {
            PackageManager pm = activity.getPackageManager();
            PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
            PermissionGroupInfo groupInfo = pm.getPermissionGroupInfo(permissionInfo.group, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawable = pm.getResourcesForApplication("android").getDrawable(groupInfo.icon, activity.getTheme());
            } else {
                drawable = pm.getResourcesForApplication("android").getDrawable(groupInfo.icon);
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
        }
        return drawable;
    }

    private static String getPermissionGuideMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        switch (permission) {
            case Manifest.permission.CAMERA:
                textResId = R.string.sb_text_need_to_allow_permission_camera;
                break;
            default:
                textResId = R.string.sb_text_need_to_allow_permission_storage;
                break;

        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }
}
