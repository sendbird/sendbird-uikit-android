package com.sendbird.uikit.consts;

/**
 * Represents type of message group UI.
 */
public enum MessageGroupType {
    GROUPING_TYPE_SINGLE(0),
    GROUPING_TYPE_HEAD(1),
    GROUPING_TYPE_BODY(2),
    GROUPING_TYPE_TAIL(3);

    int value;
    MessageGroupType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageGroupType from(int value) {
        for (MessageGroupType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return GROUPING_TYPE_SINGLE;
    }
}
