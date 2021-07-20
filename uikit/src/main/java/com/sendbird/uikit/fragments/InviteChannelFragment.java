package com.sendbird.uikit.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.activities.adapter.UserListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the user list to invite to the channel.
 */
public class InviteChannelFragment extends SelectUserFragment {

    @Override
    protected List<String> getDisabledUserIds() {
        Logger.d(">> InviteChannelFragment::getDisabledUserIds()");
        List<String> invitedUserIds = new ArrayList<>();
        if (channel == null) {
            return invitedUserIds;
        }
        if (!channel.isSuper() && !channel.isBroadcast()) {
            for (User user : channel.getMembers()) {
                invitedUserIds.add(user.getUserId());
            }
        }

        return invitedUserIds;
    }

    @Override
    protected void onUserSelectComplete(List<String> selectedUsers) {
        Logger.d(">> InviteChannelFragment::onUserSelectComplete()");
        inviteUser(selectedUsers);
    }

    /**
     * It will be called before inviting users.
     * If you want add more data, you can override this and set the data.
     *
     * @param userIds The user list who will be invited.
     * @since 1.1.1
     */
    protected void onBeforeInviteUsers(@NonNull List<String> userIds) {
    }

    /**
     * Invites users.
     *
     * @param userIds The user list who will be invited.
     * @since 1.1.1
     */
    protected void inviteUser(@NonNull List<String> userIds) {
        Logger.d(">> InviteChannelFragment::inviteUser()");
        if (channel != null) {
            onBeforeInviteUsers(userIds);
            channel.inviteWithUserIds(userIds, e -> {
                if (e != null) {
                    toastError(R.string.sb_text_error_invite_member);
                    Logger.e(e);
                    return;
                }
                onNewUserInvited(channel);
            });
        }
    }

    /**
     * It will be called when the new users have been invited.
     *
     * @param channel the channel where new users are invited.
     * @since 1.1.1
     */
    protected void onNewUserInvited(@NonNull GroupChannel channel) {
        Logger.d(">> InviteChannelFragment::onNewUserInvited()");
        if (isActive()) {
            Intent intent = ChannelActivity.newIntent(getContext(), channel.getUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * Sets the invite button text.
     *
     * @param text {@link CharSequence} set on the invite button
     * @since 1.1.1
     */
    protected void setInviteButtonText(CharSequence text) {
        setRightButtonText(text);
    }

    /**
     * Sets the invite button enabled.
     *
     * @param enabled whether the invite button is enabled or not
     * @since 1.1.1
     */
    protected void setInviteButtonEnabled(boolean enabled) {
        setRightButtonEnabled(enabled);
    }

    public static class Builder {
        private final Bundle bundle;
        private InviteChannelFragment customFragment;
        private CustomUserListQueryHandler customUserListQueryHandler = null;
        private UserListAdapter adapter;
        private View.OnClickListener headerLeftButtonListener;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendBirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode {@link SendBirdUIKit.ThemeMode}
         */
        public Builder(@NonNull String channelUrl, SendBirdUIKit.ThemeMode themeMode) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, themeMode.getResId());
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom invite channel fragment. It must inherit {@link InviteChannelFragment}.
         * @param fragment custom invite channel fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.0.4
         */
        public <T extends InviteChannelFragment> Builder setCustomInviteChannelFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets the invite button text of the header.
         *
         * @param inviteButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        public Builder setInviteButtonText(String inviteButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, inviteButtonText);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderTitle(String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                             <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.3
         */
        public Builder setUseHeaderRightButton(boolean useHeaderRightButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useHeaderRightButton);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderLeftButtonIconResId(@DrawableRes int resId) {
            return setHeaderLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the handler that loads the list of user.
         *
         * @param handler The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setCustomUserListQueryHandler(CustomUserListQueryHandler handler) {
            this.customUserListQueryHandler = handler;
            return this;
        }

        /**
         * Sets the user list adapter.
         *
         * @param adapter the adapter for the user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setUserListAdapter(UserListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderLeftButtonListener(View.OnClickListener listener) {
            this.headerLeftButtonListener = listener;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.6
         */
        public Builder setEmptyIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_EMPTY_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_EMPTY_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the text when the data is not exists
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.6
         */
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Creates an {@link InviteChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link InviteChannelFragment} applied to the {@link Bundle}.
         */
        public InviteChannelFragment build() {
            InviteChannelFragment fragment = customFragment != null ? customFragment : new InviteChannelFragment();
            fragment.setArguments(bundle);
            fragment.setCustomUserListQueryHandler(customUserListQueryHandler);
            fragment.setUserListAdapter(adapter);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            return fragment;
        }
    }
}
