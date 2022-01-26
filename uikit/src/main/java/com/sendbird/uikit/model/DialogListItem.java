package com.sendbird.uikit.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class DialogListItem {
    @StringRes
    private final int key;
    @DrawableRes
    private final int icon;
    private final boolean isAlert;
    private boolean disabled;

    /**
     * A Single item of selectable dialog list.
     *
     * @param key the resource identifier of the string resource to be displayed.
     */
    public DialogListItem(@StringRes int key) {
        this(key, 0);
    }

    /**
     * A Single item of selectable dialog list.
     *
     * @param key The resource identifier of the string resource to be displayed.
     * @param icon Resource identifier of the icon Drawable.
     */
    public DialogListItem(@StringRes int key, @DrawableRes int icon) {
        this(key, icon, false);
    }

    /**
     * A Single item of selectable dialog list.
     *
     * @param key The resource identifier of the string resource to be displayed.
     * @param icon Resource identifier of the icon Drawable.
     * @param isAlert Determine whether the item text uses an error color. If it sets <code>true</code>, the text color will be shown as an error color.
     */
    public DialogListItem(@StringRes int key, @DrawableRes int icon, boolean isAlert) {
        this.key = key;
        this.icon = icon;
        this.isAlert = isAlert;
    }

    /**
     * A Single item of selectable dialog list.
     *
     * @param key the resource identifier of the string resource to be displayed.
     * @param icon Resource identifier of the icon Drawable.
     * @param isAlert Determine whether the item text uses an error color. If it sets <code>true</code>, the text color will be shown as an error color.
     * @param disabled Determine whether to disable the item.
     */
    public DialogListItem(@StringRes int key, @DrawableRes int icon, boolean isAlert, boolean disabled) {
        this.key = key;
        this.icon = icon;
        this.isAlert = isAlert;
        this.disabled = disabled;
    }

    /**
     * Returns a key of item.
     *
     * @return String resource id.
     */
    public int getKey() {
        return key;
    }

    /**
     * Returns an icon of item.
     *
     * @return Drawable resource id.
     */
    public int getIcon() {
        return icon;
    }

    /**
     * Returns the item text uses error color.
     *
     * @return <code>true</code> if the text color uses error color, <code>false</code> otherwise.
     */
    public boolean isAlert() {
        return isAlert;
    }

    /**
     * Returns the item is disabled.
     *
     * @return <code>true</code> if the item is disabled, <code>false</code> otherwise.
     */
    public boolean isDisabled() {
        return disabled;
    }
}
