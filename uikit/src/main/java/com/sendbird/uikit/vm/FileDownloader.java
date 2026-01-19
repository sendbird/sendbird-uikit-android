package com.sendbird.uikit.vm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.Thumbnail;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.internal.model.GlideCachedUrlLoader;
import com.sendbird.uikit.internal.tasks.JobResultTask;
import com.sendbird.uikit.internal.tasks.TaskQueue;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class FileDownloader {
    private FileDownloader() {}

    private static class FileDownloadHolder {
        static final FileDownloader INSTANCE = new FileDownloader();
    }

    public static FileDownloader getInstance() {
        return FileDownloadHolder.INSTANCE;
    }

    private final Set<String> downloadingFileSet = new HashSet<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public File downloadVoiceFileToCache(@NonNull Context context, @NonNull FileMessage message) throws ExecutionException, InterruptedException, IOException {
        final File destFile = FileUtils.getVoiceFile(context, message);
        return downloadToCache(context, message, destFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public File downloadToCache(@NonNull Context context, @NonNull FileMessage message) throws ExecutionException, InterruptedException, IOException {
        final File destFile = FileUtils.createCachedDirFile(context, message.getMessageId() + "_" + message.getName());
        return downloadToCache(context, message, destFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public File downloadToCache(@NonNull Context context, @NonNull FileMessage message, @NonNull final File destFile) throws ExecutionException, InterruptedException, IOException {
        final String url = message.getUrl();
        final String plainUrl = message.getPlainUrl();
        final String cacheKey = String.valueOf(plainUrl.hashCode());

        if (hasValidCacheFile(destFile)) {
            Logger.dev("__ return cached file");
            return destFile;
        }
        destFile.delete();

        if (downloadingFileSet.contains(url)) {
            return null;
        }

        final File tempFile = FileUtils.createDeletableFile(context, destFile.getName() + ".tmp");

        try {
            downloadingFileSet.add(url);

            File result = downloadAndSaveToFile(context, url, cacheKey, tempFile, destFile);
            if (result != null) {
                return result;
            }

            // Retry (handles corrupted Glide cache - glideFile was deleted in the above call)
            return downloadAndSaveToFile(context, url, cacheKey, tempFile, destFile);
        } finally {
            downloadingFileSet.remove(url);
        }
    }

    /**
     * Downloads a file via Glide, copies it to a temp file, then saves via atomic rename.
     *
     * @return destFile on success, null on failure (tempFile and glideFile are deleted on failure)
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    private File downloadAndSaveToFile(
        @NonNull Context context,
        @NonNull String url,
        @NonNull String cacheKey,
        @NonNull File tempFile,
        @NonNull File destFile
    ) throws ExecutionException, InterruptedException, IOException {
        File glideFile = GlideCachedUrlLoader.load(Glide.with(context).asFile(), url, cacheKey).submit().get();
        Logger.dev("__ file size : " + glideFile.length());
        Logger.d("__ glideFile path : " + glideFile.getAbsolutePath());

        tempFile.delete();
        FileUtils.copyFile(glideFile, tempFile);

        // Verify copy was successful by comparing sizes before replacing destFile
        if (tempFile.length() > 0 && tempFile.length() == glideFile.length()) {
            destFile.delete();
            if (tempFile.renameTo(destFile)) {
                return destFile;
            }
        }

        // Cleanup on failure
        tempFile.delete();
        glideFile.delete();
        return null;
    }

    private boolean hasValidCacheFile(@Nullable File file) {
        return file != null && file.exists() && file.length() > 0;
    }

    public boolean isDownloading(@NonNull String url) {
        return downloadingFileSet.contains(url);
    }

    public void saveFile(@NonNull Context context, @NonNull String url,
                         @NonNull String type, @NonNull String filename, @NonNull String cacheKey) throws Exception {
        if (downloadingFileSet.contains(url)) {
            return;
        }

        try {
            downloadingFileSet.add(url);
            File file = GlideCachedUrlLoader.load(Glide.with(context).asFile(), url, cacheKey).submit().get();
            FileUtils.saveFile(context, file, type, filename);
        } finally {
            downloadingFileSet.remove(url);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean downloadFile(@NonNull Context context, @NonNull FileMessage message, @NonNull OnResultHandler<File> handler) {
        boolean isDownloading = FileDownloader.getInstance().isDownloading(message.getUrl());
        Logger.d("++ request download file url=%s", message.getUrl());
        Logger.d("++ isDownloading=%s", isDownloading);
        if (isDownloading) {
            Logger.d("-- [%s] already request download.", message.getUrl());
            return false;
        }
        TaskQueue.addTask(new JobResultTask<File>() {
            @Override
            public File call() throws ExecutionException, InterruptedException, IOException {
                if (MessageUtils.isVoiceMessage(message)) {
                    return FileDownloader.getInstance().downloadVoiceFileToCache(context, message);
                } else {
                    return FileDownloader.getInstance().downloadToCache(context, message);
                }
            }

            @Override
            public void onResultForUiThread(@Nullable File file, @Nullable SendbirdException e) {
                if (e != null || file == null) {
                    Logger.e(e);
                    handler.onError(e);
                    return;
                }

                Logger.d("++ file download Complete file path : " + file.getAbsolutePath());
                handler.onResult(file);
            }
        });
        return true;
    }

    public static void downloadThumbnail(@NonNull Context context, @NonNull FileMessage message) {
        List<Thumbnail> thumbnails = message.getThumbnails();
        Thumbnail thumbnail = null;
        if (thumbnails.size() > 0) {
            thumbnail = thumbnails.get(0);
        }
        String url = message.getUrl();
        if (thumbnail != null) {
            Logger.dev("++ thumbnail width : %s, thumbnail height : %s", thumbnail.getRealWidth(), thumbnail.getRealHeight());
            url = thumbnail.getUrl();
        }
        Glide.with(context).asFile().load(url).submit();
    }
}
