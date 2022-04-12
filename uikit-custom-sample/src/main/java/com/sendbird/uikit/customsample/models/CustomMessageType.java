package com.sendbird.uikit.customsample.models;

import androidx.annotation.NonNull;

import com.sendbird.uikit.customsample.consts.StringSet;

public enum CustomMessageType {
    NONE(""), HIGHLIGHT(StringSet.highlight), EMOJI(StringSet.emoji_type);

    private final String value;
    CustomMessageType(String value) { this.value = value; }

    @NonNull
    public String getValue() {
        return value;
    }

    @NonNull
    public static CustomMessageType from(@NonNull String value) {
        for (CustomMessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return NONE;
    }
}
