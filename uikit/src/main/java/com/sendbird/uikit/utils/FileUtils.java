package com.sendbird.uikit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.sendbird.android.FileMessage;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.log.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), "documents");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File createDownloadFile(@NonNull String fileName) {
        String imageFileName = "Downloaded_file_" + System.currentTimeMillis() + "_" + fileName;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(dir, imageFileName);
    }

    public static File getDownloadFileFromMessage(@NonNull FileMessage message) {
        String newFileName = "Downloaded_file_" + message.getMessageId() + "_" + message.getName();
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(dir, newFileName);
    }

    public static boolean hasDownloadFileFromMessage(@NonNull FileMessage message) {
        File file = getDownloadFileFromMessage(message);
        return file.exists() && file.length() > 0;
    }

    public static Uri fileToUri(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    public static String extractExtension(@NonNull Context context, @NonNull Uri uri) {
        String extension;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            extension = extractExtension(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    public static String extractExtension(@NonNull String mimeType) {
        final MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(mimeType);
    }

    public static Bitmap.CompressFormat extractBitmapFormat(@NonNull String mimeType) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        if (mimeType.endsWith(StringSet.jpeg) || mimeType.endsWith(StringSet.jpg)) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (mimeType.endsWith(StringSet.png)) {
            format = Bitmap.CompressFormat.PNG;
        } else if (mimeType.endsWith(StringSet.webp)) {
            format = Bitmap.CompressFormat.WEBP;
        }
        return format;
    }

    private static String copyFromUri(@NonNull Context context, @NonNull Uri uri, @NonNull File dstFile) {
        if (!dstFile.exists() || dstFile.length() <= 0) {
            InputStream inputStream;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                OutputStream outputStream = new FileOutputStream(dstFile);
                copy(inputStream, outputStream);
            } catch (Exception e) {
                Logger.e(e);
            }
        }
        return dstFile.getAbsolutePath();
    }

    public static String uriToPath(@NonNull Context context, @NonNull Uri uri) {
        String tempFileName = "Temp_" + uri.hashCode() + "." + extractExtension(context, uri);
        File dstFile = createCachedDirFile(context, tempFileName);
        return copyFromUri(context, uri, dstFile);
    }

    public static File uriToFile(@NonNull Context context, @NonNull Uri uri) {
        return new File(uriToPath(context, uri));
    }

    public static File saveFile(@NonNull Context context, @NonNull File src, @NonNull String fileName) throws Exception {
        File newFile = createDownloadFile(fileName);
        return saveFile(context, src, newFile);
    }

    public static File saveFile(@NonNull Context context, @NonNull File src, @NonNull File dest) throws Exception {
        FileInputStream input = new FileInputStream(src);
        FileOutputStream output = new FileOutputStream(dest);
        copy(input, output);
        galleryAddPic(context, dest.getAbsolutePath());
        return dest;
    }

    public static File bitmapToFile(@NonNull Bitmap image, @NonNull File dest, @NonNull Bitmap.CompressFormat format) throws IOException {
        return bitmapToFile(image, dest, 100, format);
    }

    public static File bitmapToFile(@NonNull Bitmap image, @NonNull File dest, int quality, @NonNull Bitmap.CompressFormat format) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(dest, false)) {
            Logger.d("++ Create bitmap to file, quality=%s, format=%s", quality, format);
            image.compress(format, quality, outputStream);
        }
        return dest;
    }

    private static void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private static int copy(InputStream input, OutputStream output) throws Exception {
        int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            out.close();
            in.close();
        }
        return count;
    }

    public static Uri createPictureImageUri(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        cv.put(MediaStore.Images.Media.TITLE, fileName);
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] list = fileOrDirectory.listFiles();
            if (list != null) {
                for (File child : list) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    public static void removeDeletableDir(@NonNull Context context) {
        File dir = getDeletableDir(context);
        deleteRecursive(dir);
    }

    public static File getDeletableDir(@NonNull Context context) {
        File dir = context.getCacheDir();
        File file = new File(dir, "deletable");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File createDeletableFile(@NonNull Context context, @NonNull String fileName) {
        return new File(getDeletableDir(context), fileName);
    }

    public static File createCachedDirFile(@NonNull Context context, @NonNull String fileName) {
        File dir = context.getCacheDir();
        return new File(dir, fileName);
    }
}
