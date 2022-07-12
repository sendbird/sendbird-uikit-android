package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SingleEvent<T> {
    @NonNull
    private final T content;
    private boolean hasBeenHandled = false;

    public SingleEvent(@NonNull T content) {
        this.content = content;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) return null;
        hasBeenHandled = true;
        return content;
    }

    @NonNull
    public T peekContent() {
        return content;
    }
}
