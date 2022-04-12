package com.sendbird.uikit.tasks;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskQueue {
    @NonNull
    final private static ExecutorService taskExecutor = Executors.newCachedThreadPool();

    @NonNull
    public static <T> Future<T> addTask(@NonNull JobTask<T> task) {
        return taskExecutor.submit(task.getCallable());
    }

    @NonNull
    public static <T> Future<T> addTask(@NonNull JobResultTask<T> task) {
        return taskExecutor.submit(task.getCallable());
    }
}
