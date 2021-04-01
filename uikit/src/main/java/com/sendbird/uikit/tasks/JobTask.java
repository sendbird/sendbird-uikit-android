package com.sendbird.uikit.tasks;

import com.sendbird.uikit.log.Logger;

import java.util.concurrent.Callable;

public abstract class JobTask<T> {
    protected abstract T call() throws Exception;

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
