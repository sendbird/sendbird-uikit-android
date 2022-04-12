package com.sendbird.uikit.tasks;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendBirdError;
import com.sendbird.android.SendBirdException;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public abstract class JobResultTask<T> {
    @NonNull
    private final static Handler mainHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final JobTask<T> task = new JobTask<T>() {

        @Override
        public T call() throws InterruptedException {
            T result = null;
            SendBirdException ex = null;

            try {
                result = JobResultTask.this.call();
            } catch (SendBirdException e) {
                ex = e;
            } catch (Exception e) {
                ex = new SendBirdException(e.getMessage(), SendBirdError.ERR_REQUEST_FAILED);
            }

            final T response = result;
            final SendBirdException e = ex;

            final CountDownLatch lock = new CountDownLatch(1);
            mainHandler.post(() -> {
                try {
                    JobResultTask.this.onResultForUiThread(response, e);
                } finally {
                    lock.countDown();
                }
            });

            lock.await();
            return result;
        }
    };

//    private static volatile Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    abstract public T call() throws Exception;

    abstract public void onResultForUiThread(@Nullable T result, @Nullable SendBirdException e);

    @NonNull
    final Callable<T> getCallable() {
        return task.getCallable();
    }
}
