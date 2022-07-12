package com.sendbird.uikit.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.log.Logger;

import java.util.concurrent.Callable;

public abstract class JobTask<T> {
    @Nullable
    protected abstract T call() throws Exception;

    @NonNull
    final Callable<T> getCallable() {
        return () -> {
            try {
                return JobTask.this.call();
            } catch (Exception e) {
                Logger.e(e);
            }
            return null;
        };
    }
}
