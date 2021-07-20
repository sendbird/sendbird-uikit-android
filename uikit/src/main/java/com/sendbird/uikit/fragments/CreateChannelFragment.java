package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.activities.adapter.UserListAdapter;
import com.sendbird.uikit.consts.CreateableChannelType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.log.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment displaying the user list to create the channel.
 */
public class CreateChannelFragment extends SelectUserFragment {
    private boolean isDistinct;
    private CreateableChannelType selectedChannelType;
    private final AtomicBoolean isCreatingChannel = new AtomicBoolean();

    @Override
    protected void onConfigure() {
        Bundle args = getArguments();
        this.selectedChannelType = args != null && args.containsKey(StringSet.KEY_SELECTED_CHANNEL_TYPE) ? (CreateableChannelType) args.getSerializable(StringSet.KEY_SELECTED_CHANNEL_TYPE) : CreateableChannelType.Normal;
        this.isDistinct = args != null && args.getBoolean(StringSet.KEY_DISTINCT, false);
    }

    @Override
    protected void onUserSelectComplete(List<String> selectedUsers) {
        GroupChannelParams params = new GroupChannelParams();
        params.addUserIds(selectedUsers);
        params.setDistinct(isDistinct);
        params.setName("");
        params.setCoverUrl("");
        params.setOperators(Collections.singletonList(SendBird.getCurrentUser()));

        Logger.d("=++ selected channel type : " + selectedChannelType);
        switch (selectedChannelType) {
            case Super:
                params.setSuper(true);
                break;
            case Broadcast:
                params.setBroadcast(true);
                break;
            default:
                break;
        }
        createGroupChannel(params);
    }

    /**
     * It will be called before creating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelParams}.
     * @since 1.0.4
     */
    protected void onBeforeCreateGroupChannel(@NonNull GroupChannelParams params) {
    }

    /**
     * Creates <code>GroupChannel</code> with GroupChannelParams.
     *
     * @param params Params of channel. Refer to {@link GroupChannelParams}.
     * @since 1.0.4
     */
    protected void createGroupChannel(@NonNull GroupChannelParams params) {
        Logger.dev(">> CreateChannelFragment::createGroupChannel()");
        CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
        if (cutsomHandler != null) {
            cutsomHandler.onBeforeCreateGroupChannel(params);
        }
        onBeforeCreateGroupChannel(params);

        Logger.dev("++ createGroupChannel params : " + params);
        Logger.dev("++ createGroupChannel isCreatingChannel : " + isCreatingChannel.get());
        if (isCreatingChannel.compareAndSet(false, true)) {
            GroupChannel.createChannel(params, (channel, e) -> {
                if (e != null) {
                    toastError(R.string.sb_text_error_create_channel);
                    Logger.e(e);
                    isCreatingChannel.set(false);
                } else {
                    onNewChannelCreated(channel);
                }
            });
        }
    }

    /**
     * It will be called when the new channel has been created.
     *
     * @param channel the new channel
     * @since 1.0.4
     */
    protected void onNewChannelCreated(@NonNull GroupChannel channel) {
        if (isActive()) {
            startActivity(ChannelActivity.newIntent(getContext(), channel.getUrl()));
            finish();
        }
    }

    /**
     * Sets the create button text.
     *
     * @param text {@link CharSequence} set on the create button
     * @since 1.1.1
     */
    protected void setCreateButtonText(CharSequence text) {
        setRightButtonText(text);
    }

    /**
     * Sets the create button enabled.
     *
     * @param enabled whether the create button is enabled or not
     * @since 1.1.1
     */
    protected void setCreateButtonEnabled(boolean enabled) {
        setRightButtonEnabled(enabled);
    }

    public static class Builder {
        private final Bundle bundle;
        private CreateChannelFragment customFragment;
        private CustomUserListQueryHandler customUserListQueryHandler = null;
        private UserListAdapter adapter;
        private View.OnClickListener headerLeftButtonListener;

        /**
         * Constructor
         */
        public Builder() {
            this(SendBirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param themeMode {@link SendBirdUIKit.ThemeMode}
         */
        public Builder(SendBirdUIKit.ThemeMode themeMode) {
            this(themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param type A type of channel. Default is a {@link CreateableChannelType#Normal}
         */
        public Builder(@NonNull CreateableChannelType type) {
            this(SendBirdUIKit.getDefaultThemeMode().getResId(), type);
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@StyleRes int customThemeResId) {
            this(customThemeResId, CreateableChannelType.Normal);
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         * @param type             A type of channel. Default is a {@link CreateableChannelType#Normal}
         * @since 1.2.0
         */
        public Builder(@StyleRes int customThemeResId, @NonNull CreateableChannelType type) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putSerializable(StringSet.KEY_SELECTED_CHANNEL_TYPE, type);
        }

        /**
         * Sets the custom create channel fragment. It must inherit {@link CreateChannelFragment}.
         *
         * @param fragment custom create channel fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.0.4
         */
        public <T extends CreateChannelFragment> Builder setCustomCreateChannelFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets the create button text of the header.
         *
         * @param createButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        public Builder setCreateButtonText(String createButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, createButtonText);
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
         * Sets distinct mode. Distinct mode must be false, if super mode is true.
         *
         * @param isDistinct true if distinct mode channel.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setIsDistinct(boolean isDistinct) {
            bundle.putBoolean(StringSet.KEY_DISTINCT, isDistinct);
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
         * Creates an {@link CreateChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link CreateChannelFragment} applied to the {@link Bundle}.
         */
        public CreateChannelFragment build() {
            CreateChannelFragment fragment = customFragment != null ? customFragment : new CreateChannelFragment();
            fragment.setArguments(bundle);
            fragment.setCustomUserListQueryHandler(customUserListQueryHandler);
            fragment.setUserListAdapter(adapter);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            return fragment;
        }
    }
}
