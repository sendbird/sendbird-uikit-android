package com.sendbird.uikit.consts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogEditTextParams {
    @Nullable
    private String hintText;
    private boolean enableSingleLine;
    @Nullable
    private android.text.TextUtils.TruncateAt ellipsis;
    private int selection;
    @Nullable
    private String text;

    public DialogEditTextParams() {
    }

    public DialogEditTextParams(@NonNull String hintText) {
        this.hintText = hintText;
    }

    public void setHintText(@NonNull String hintText) {
        this.hintText = hintText;
    }

    public void setEnableSingleLine(boolean enableSingleLine) {
        this.enableSingleLine = enableSingleLine;
    }

    public void setEllipsis(@NonNull android.text.TextUtils.TruncateAt ellipsis) {
        this.ellipsis = ellipsis;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }

    public void setText(@NonNull String text) {
        this.text = text;
    }

    @Nullable
    public String getHintText() {
        return hintText;
    }

    public boolean enabledSingleLine() {
        return enableSingleLine;
    }

    @Nullable
    public android.text.TextUtils.TruncateAt getEllipsis() {
        return ellipsis;
    }

    public int getSelection() {
        return selection;
    }

    @Nullable
    public String getText() {
        return text;
    }
}
