package com.sendbird.uikit.activities.viewholder;

/**
 * Represents type of messages.
 */
public enum MessageType {
    VIEW_TYPE_USER_MESSAGE_ME(0),
    VIEW_TYPE_USER_MESSAGE_OTHER(1),
    VIEW_TYPE_FILE_MESSAGE_ME(2),
    VIEW_TYPE_FILE_MESSAGE_OTHER(3),
    VIEW_TYPE_FILE_MESSAGE_IMAGE_ME(4),
    VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER(5),
    VIEW_TYPE_FILE_MESSAGE_VIDEO_ME(6),
    VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER(7),
    VIEW_TYPE_ADMIN_MESSAGE(8),
    VIEW_TYPE_TIME_LINE(9),
    VIEW_TYPE_UNKNOWN_MESSAGE_ME(10),
    VIEW_TYPE_UNKNOWN_MESSAGE_OTHER(11);

    int value;
    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType from(int value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return VIEW_TYPE_ADMIN_MESSAGE;
    }
}
