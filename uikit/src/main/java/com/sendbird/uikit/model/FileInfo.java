package com.sendbird.uikit.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.SendBirdException;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.ImageUtils;
import com.sendbird.uikit.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

final public class FileInfo {
    private final static int MAX_COMPRESS_QUALITY = 100;

    private final Uri uri;
    private final String path;
    private final int size;
    private final String mimeType;
    private final String fileName;
    private final int thumbnailWidth;
    private final int thumbnailHeight;
    private final String thumbnailPath;
    private final File file;

    public FileInfo(String path, int size, String mimeType, String fileName, Uri uri, int thumbnailWidth, int thumbnailHeight, String thumbnailPath) {
        this.path = path;
        this.size = size;
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.uri = uri;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.thumbnailPath = thumbnailPath;
        this.file = new File(path);
    }

    public String getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public Uri getUri() {
        return uri;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public File getThumbnailFile() {
        File file = null;
        if (!TextUtils.isEmpty(thumbnailPath)) {
            file = new File(thumbnailPath);
            if (file.exists()) {
                return file;
            }
        }
        return file;
    }

    public File getFile() {
        return file;
    }

    public FileMessageParams toFileParams() {
        FileMessageParams params = new FileMessageParams();
        params.setMimeType(getMimeType());
        params.setFileName(getFileName());
        params.setFileSize(getSize());
        params.setFile(getFile());

        int thumbWidth = getThumbnailWidth();
        int thumbHeight = getThumbnailHeight();
        if (thumbWidth > 0 && thumbHeight > 0) {
            Logger.dev("++ image width : %s, image height : %s", thumbWidth, thumbHeight);
            List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
            thumbnailSizes.add(new FileMessage.ThumbnailSize(thumbWidth, thumbHeight));
            thumbnailSizes.add(new FileMessage.ThumbnailSize(thumbWidth / 2, thumbHeight / 2));
            params.setThumbnailSizes(thumbnailSizes);
        }

        return params;
    }

    private static boolean isCompressible(@NonNull String mimeType) {
        return mimeType.startsWith(StringSet.image)
                && (mimeType.endsWith(StringSet.jpeg) || mimeType.endsWith(StringSet.jpg) || mimeType.endsWith(StringSet.png));
    }

    public static Future<FileInfo> fromUri(@NonNull final Context context, @NonNull final Uri uri, boolean useImageCompression, @Nullable OnResultHandler<FileInfo> handler) {
        return TaskQueue.addTask(new JobResultTask<FileInfo>() {
            @Override
            public FileInfo call() throws IOException {
                FileInfo fileInfo = null;
                try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                    String mimeType = context.getContentResolver().getType(uri);
                    String path = FileUtils.uriToPath(context, uri);
                    String originPath = path;

                    Pair<Integer, Integer> resizingSize = SendBirdUIKit.getResizingSize();
                    if (cursor != null) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        String thumbnailPath = path;
                        int thumbnailWidth = resizingSize.first / 2, thumbnailHeight = resizingSize.second / 2;

                        if (cursor.moveToFirst()) {
                            String name = cursor.getString(nameIndex);
                            int size = (int) cursor.getLong(sizeIndex);

                            if (useImageCompression && (mimeType != null && isCompressible(mimeType))) {
                                int quality = SendBirdUIKit.getCompressQuality();
                                if (quality < 0 || quality > MAX_COMPRESS_QUALITY) {
                                    throw new IllegalArgumentException("quality must be 0..100");
                                }

                                Logger.d("++ file size=%s, size from db=%s", new File(path).length(), size);
                                int originSize = size;
                                path = resizeImage(context, originPath, mimeType, quality, resizingSize.first, resizingSize.second);
                                size = (int) new File(path).length();
                                Logger.d("++ originFile size=%s, resized file size=%s", originSize, size);
                                Logger.d("\n++ originFile path=%s, \n resized file path=%s\n", originPath, path);

                                if (!originPath.equals(path) && size != originSize) {
                                    Logger.d("++ file has been resized. the original file will remove.");
                                    new File(originPath).delete();
                                }
                            }

                            if (mimeType != null) {
                                Pair<Integer, Integer> dimension = ImageUtils.getDimensions(originPath, mimeType);
                                thumbnailPath = path;
                                thumbnailWidth = dimension.first;
                                thumbnailHeight = dimension.second;
                            }

                            Logger.d("==============================================================================");
                            Logger.d("++ FILE PATH : %s", path);
                            Logger.d("++ SIZE : %s", size);
                            Logger.d("++ MIMETYPE : %s", mimeType);
                            Logger.d("++ NAME : %s", name);
                            Logger.d("++ THUMBNAIL PATH : %s", thumbnailPath);
                            Logger.d("++ THUMBNAIL HEIGHT : %s", thumbnailWidth);
                            Logger.d("++ THUMBNAIL HEIGHT : %s", thumbnailHeight);
                            Logger.d("==============================================================================");
                            fileInfo = new FileInfo(path, size, mimeType, name, uri, thumbnailWidth, thumbnailHeight, thumbnailPath);
                        }
                    }
                }
                return fileInfo;
            }

            @Override
            public void onResultForUiThread(FileInfo info, SendBirdException e) {
                if (handler == null) return;
                if (e != null || info == null) {
                    Logger.w(e);
                    handler.onError(e);
                    return;
                }

                handler.onResult(info);
            }
        });
    }

    private static String resizeImage(@NonNull Context context, @NonNull String path, @NonNull String mimeType, int quality, int width, int height) throws IOException {
        int inSampleSize = ImageUtils.calculateInSampleSize(path, width, height);

        // When it comes to compressing is ignored if the image's mine-type is a PNG.
        // inSampleSize is a 1 meant that the bitmap resizing is not necessary.
        if (inSampleSize > 1 || (!mimeType.endsWith(StringSet.png) && quality < MAX_COMPRESS_QUALITY)) {
            File originFile = new File(path);
            String tempFileName = String.format(Locale.US, "Resized_%s_%s", quality, originFile.getName());
            File destFile = FileUtils.createCachedDirFile(context, tempFileName);
            if (destFile != null && destFile.exists() && destFile.length() > 0) {
                Logger.d("++ resized file exists");
                return destFile.getAbsolutePath();
            }
            Bitmap bitmap = ImageUtils.getBitmap(path, width, height);
            if (bitmap != null) {
                Logger.d("++ resized image with=%s, height=%s", bitmap.getWidth(), bitmap.getHeight());
                return FileUtils.bitmapToFile(bitmap, destFile, quality, FileUtils.extractBitmapFormat(mimeType)).getAbsolutePath();
            }
        }
        return path;
    }

    public void clear() {
        Logger.d(">> FileInfo::clear()");
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                Logger.d("-- file delete=%s, path=%s", deleted, path);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;
        return uri.equals(fileInfo.getUri());
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path='" + path + '\'' +
                ", size=" + size +
                ", mimeType='" + mimeType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", uri=" + uri +
                ", thumbnailWidth=" + thumbnailWidth +
                ", thumbnailHeight=" + thumbnailHeight +
                '}';
    }
}
