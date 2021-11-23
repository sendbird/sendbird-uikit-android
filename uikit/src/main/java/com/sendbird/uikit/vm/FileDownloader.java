package com.sendbird.uikit.vm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.sendbird.android.FileMessage;
import com.sendbird.android.SendBirdException;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.FileUtils;

import java.io.File;
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

    private File getDownloadFile(@NonNull Context context, @NonNull FileMessage message) {
        String newFileName = "Downloaded_file_" + message.getMessageId() + "_" + message.getName();
        return FileUtils.createCachedDirFile(context.getApplicationContext(), newFileName);
    }

    public boolean hasFile(@NonNull Context context, @NonNull FileMessage message) {
        File file = getDownloadFile(context, message);
        if (file.exists()) {
            if (file.length() == message.getSize()) {
                Logger.dev("__ return exist file");
                return true;
            }
        }
        return false;
    }

    @Nullable
    public File downloadToCache(@NonNull Context context, @NonNull FileMessage message) throws ExecutionException, InterruptedException {
        final String url = message.getUrl();
        if (downloadingFileSet.contains(url)) {
            return null;
        }
        try {
            downloadingFileSet.add(url);
            String newFileName = "Downloaded_file_" + message.getMessageId() + "_" + message.getName();
            File destFile = FileUtils.createCachedDirFile(context.getApplicationContext(), newFileName);
            Logger.dev("__ file size : " + destFile.length());

            if (destFile.exists()) {
                if (destFile.length() == message.getSize()) {
                    Logger.dev("__ return exist file");
                    return destFile;
                }
                destFile.delete();
            }

            File file = Glide.with(context).asFile().load(message.getUrl()).submit().get();
            if (file != null && file.exists() && file.renameTo(destFile)) {
                return destFile;
            }
        } finally {
            downloadingFileSet.remove(url);
        }
        return null;
    }

    public boolean isDownloading(@NonNull String url) {
        return downloadingFileSet.contains(url);
    }

    public void saveFile(Context context, @NonNull String url,
                         @NonNull String type, @NonNull String filename) throws Exception {
        if (downloadingFileSet.contains(url) || context == null) {
            return;
        }

        try {
            downloadingFileSet.add(url);
            final RequestManager glide = Glide.with(context);
            File file = glide.asFile().load(url).submit().get();
            FileUtils.saveFile(context, file, type, filename);
        } finally {
            downloadingFileSet.remove(url);
        }
    }

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
            public File call() throws ExecutionException, InterruptedException {
                return FileDownloader.getInstance().downloadToCache(context, message);
            }

            @Override
            public void onResultForUiThread(File file, SendBirdException e) {
                if (e != null) {
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
        List<FileMessage.Thumbnail> thumbnails = message.getThumbnails();
        FileMessage.Thumbnail thumbnail = null;
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
