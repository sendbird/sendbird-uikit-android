package com.sendbird.uikit.model;

import androidx.annotation.NonNull;


final public class VoiceMessageInfo {
    @NonNull
    private final String path;
    @NonNull
    private final String mimeType;
    private final int duration;

    public VoiceMessageInfo(@NonNull String path,
                            @NonNull String mimeType,
                            int duration) {
        this.path = path;
        this.mimeType = mimeType;
        this.duration = duration;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    @NonNull
    public String getMimeType() {
        return mimeType;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoiceMessageInfo)) return false;

        VoiceMessageInfo that = (VoiceMessageInfo) o;

        if (duration != that.duration) return false;
        if (!path.equals(that.path)) return false;
        return mimeType.equals(that.mimeType);
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + mimeType.hashCode();
        result = 31 * result + duration;
        return result;
    }
}
