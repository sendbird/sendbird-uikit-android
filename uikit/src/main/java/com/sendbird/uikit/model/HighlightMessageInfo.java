package com.sendbird.uikit.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseMessage;

/**
 * The information of the message to highlight.
 * @since 2.1.0
 */
public final class HighlightMessageInfo implements Parcelable {

    private final long messageId;
    private final long updatedAt;

    public HighlightMessageInfo(long firstMessageId, long updatedAt) {
        this.messageId = firstMessageId;
        this.updatedAt = updatedAt;
    }

    protected HighlightMessageInfo(@NonNull Parcel in) {
        messageId = in.readLong();
        updatedAt = in.readLong();
    }

    public static final Creator<HighlightMessageInfo> CREATOR = new Creator<HighlightMessageInfo>() {
        @Override
        public HighlightMessageInfo createFromParcel(Parcel in) {
            return new HighlightMessageInfo(in);
        }

        @Override
        public HighlightMessageInfo[] newArray(int size) {
            return new HighlightMessageInfo[size];
        }
    };

    /**
     * Returns the highlighted message ID.
     *
     * @return The message ID.
     * @since 2.1.0
     */
    public long getMessageId() {
        return messageId;
    }

    /**
     * Returns message updated time of the highlighted message
     *
     * @return Timestamp in milliseconds.
     * @since 2.1.0
     */
    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * The generator that creates the HighlightMessageInfo from the message.
     *
     * @param message A {@link BaseMessage} that includes the highlighting texts.
     * @return The created HighlightMessageInfo instance.
     * @since 2.1.0
     */
    @NonNull
    public static HighlightMessageInfo fromMessage(@NonNull BaseMessage message) {
        return new HighlightMessageInfo(message.getMessageId(), message.getUpdatedAt());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(messageId);
        dest.writeLong(updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HighlightMessageInfo that = (HighlightMessageInfo) o;

        if (messageId != that.messageId) return false;
        return updatedAt == that.updatedAt;
    }

    @Override
    public int hashCode() {
        int result = (int) (messageId ^ (messageId >>> 32));
        result = 31 * result + (int) (updatedAt ^ (updatedAt >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "HighlightMessageInfo{" +
                "messageId=" + messageId +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
