package com.sendbird.uikit.consts;

import androidx.annotation.NonNull;

public class DialogEditTextParams {
    private String hintText;
    private boolean enableSingleLine;
    private android.text.TextUtils.TruncateAt ellipsis;
    private int selection;
    private String text;

    public DialogEditTextParams() {
    }

    public DialogEditTextParams(@NonNull String hintText) {
        this.hintText = hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public void setEnableSingleLine(boolean enableSingleLine) {
        this.enableSingleLine = enableSingleLine;
    }

    public void setEllipsis(android.text.TextUtils.TruncateAt ellipsis) {
        this.ellipsis = ellipsis;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHintText() {
        return hintText;
    }

    public boolean enabledSingleLine() {
        return enableSingleLine;
    }

    public android.text.TextUtils.TruncateAt getEllipsis() {
        return ellipsis;
    }

    public int getSelection() {
        return selection;
    }

    public String getText() {
        return text;
    }
}
