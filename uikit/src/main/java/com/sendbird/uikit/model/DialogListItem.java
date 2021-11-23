package com.sendbird.uikit.model;

public class DialogListItem {
    private final int key;
    private final int icon;
    private final boolean isAlert;
    private boolean disabled;

    public DialogListItem(int key) {
        this(key, 0);
    }

    public DialogListItem(int key, int icon) {
        this(key, icon, false);
    }

    public DialogListItem(int key, int icon, boolean isAlert) {
        this.key = key;
        this.icon = icon;
        this.isAlert = isAlert;
    }

    public DialogListItem(int key, int icon, boolean isAlert, boolean disabled) {
        this.key = key;
        this.icon = icon;
        this.isAlert = isAlert;
        this.disabled = disabled;
    }

    public int getKey() {
        return key;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
