package com.sendbird.uikit.utils;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.util.List;

public class IntentUtils {
    public static boolean hasIntent(@NonNull Context context, @NonNull Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    public static Intent getCameraIntent(@NonNull Context context, @NonNull Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        grantWritePermission(context, intent, uri);
        if ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ) {
            intent.setClipData(ClipData.newRawUri("", uri));
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent getGalleryIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ) {
            intent.setType("*/*");
            String[] mimetypes = {"image/*", "video/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        } else {
            intent.setType("image/*|video/*");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return Intent.createChooser(intent, null);
    }

    public static Intent getFileChooserIntent() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent getFileViewerIntent(@NonNull Uri uri, @NonNull String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return Intent.createChooser(intent, null);
    }

    private static void grantWritePermission(Context context, Intent intent, Uri uri) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static Intent getWebViewerIntent(@NonNull String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
