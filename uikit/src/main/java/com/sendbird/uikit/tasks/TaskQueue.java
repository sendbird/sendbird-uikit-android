package com.sendbird.uikit.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskQueue {
    final private static ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public static <T> Future<T> addTask(JobTask<T> task) {
        return taskExecutor.submit(task.getCallable());
    }

    public static <T> Future<T> addTask(JobResultTask<T> task) {
        return taskExecutor.submit(task.getCallable());
    }
}
