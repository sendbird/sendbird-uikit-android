package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.BannedListActivity;
import com.sendbird.uikit.activities.MutedMemberListActivity;
import com.sendbird.uikit.activities.OperatorListActivity;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentModerationsBinding;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;
import com.sendbird.uikit.log.Logger;

/**
 * Fragment displaying the menu list to control the channel.
 * It will be displayed if the member is an operator.
 *
 * @since 1.2.0
 */
public class ModerationFragment extends BaseGroupChannelFragment implements LoadingDialogHandler {
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_MODERATION" + System.currentTimeMillis();;

    private LoadingDialogHandler loadingDialogHandler;

    /**
     * Represents all moderation menus.
     *
     * @since 1.2.0
     */
    public enum ModerationMenu {
        OPERATORS, MUTED_MEMBERS, BANNED_MEMBERS, FREEZE_CHANNEL
    }

    private SbFragmentModerationsBinding binding;
    protected View.OnClickListener headerLeftButtonListener;
    protected OnMenuItemClickListener<ModerationMenu, BaseChannel> menuItemClickListener;

    public ModerationFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> ModerationFragment::onCreate()");
        Bundle args = getArguments();
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        if (args != null) {
            themeResId = args.getInt(StringSet.KEY_THEME_RES_ID);
        }

        if (getActivity() != null) {
            getActivity().setTheme(themeResId);
        }

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {}

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ModerationFragment::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    if (channel.getMyMemberState() == Member.MemberState.NONE) {
                        finish();
                    }
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> ModerationFragment::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    finish();
                }
            }

            @Override
            public void onChannelFrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ModerationFragment::onChannelFrozen(%s)", channel.isFrozen());
                    ModerationFragment.this.channel = (GroupChannel) channel;
                    binding.freezeChannelItem.setChecked(true);
                }
            }

            @Override
            public void onChannelUnfrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ModerationFragment::onChannelUnfrozen(%s)", channel.isFrozen());
                    ModerationFragment.this.channel = (GroupChannel) channel;
                    binding.freezeChannelItem.setChecked(false);
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl()) &&
                        ((GroupChannel) channel).getMyRole() != Member.Role.OPERATOR) {
                    Logger.i(">> ModerationFragment::onOperatorUpdated()");
                    ModerationFragment.this.channel = (GroupChannel) channel;
                    Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                    finish();
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> ModerationFragment::onUserBanned()");
                    finish();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_moderations, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeaderOnCreated();
    }

    @Override
    protected void onReadyFailure() {}

    @Override
    protected void onConfigure() {
        if (channel.getMyRole() != Member.Role.OPERATOR) finish();
    }

    @Override
    protected void onDrawPage() {
        initHeaderOnReady();
        initModerations(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channelUrl.equals(channel.getUrl());
    }

    private void initHeaderOnCreated() {
        Bundle args = getArguments();
        String headerTitle = getString(R.string.sb_text_channel_settings_moderations);
        boolean useHeader = false;
        boolean useHeaderLeftButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        ColorStateList headerLeftButtonIconTint = null;

        if (args != null) {
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, getString(R.string.sb_text_channel_settings_moderations));
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
        }

        binding.abSettingsHeader.setVisibility(useHeader ? View.VISIBLE : View.GONE);

        binding.abSettingsHeader.getTitleTextView().setText(headerTitle);

        binding.abSettingsHeader.setUseRightButton(false);
        binding.abSettingsHeader.setUseLeftImageButton(useHeaderLeftButton);
        binding.abSettingsHeader.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.abSettingsHeader.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.abSettingsHeader.setLeftImageButtonClickListener(v -> finish());
    }

    private void initHeaderOnReady() {
        if (headerLeftButtonListener != null) {
            binding.abSettingsHeader.setLeftImageButtonClickListener(headerLeftButtonListener);
        }
    }

    private void initModerations(@NonNull final GroupChannel channel) {
        if (this.loadingDialogHandler == null) {
            this.loadingDialogHandler = this;
        }

        binding.vgContent.setBackgroundResource(SendBirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50);
        binding.operatorsItem.setOnClickListener(v -> {
            Logger.dev("++ operation item clicked");
            if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, ModerationMenu.OPERATORS, channel)) {
                return;
            }
            startActivity(OperatorListActivity.newIntent(getContext(), channel.getUrl()));
        });

        binding.freezeChannelItem.setChecked(channel.isFrozen());
        binding.mutedMembersItem.setOnClickListener(v -> {
            Logger.dev("++ muted item clicked");
            if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, ModerationMenu.MUTED_MEMBERS, channel)) {
                return;
            }
            startActivity(MutedMemberListActivity.newIntent(getContext(), channel.getUrl()));
        });
        binding.bannedMembersItem.setOnClickListener(v -> {
            Logger.dev("++ banned item clicked");
            if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, ModerationMenu.BANNED_MEMBERS, channel)) {
                return;
            }
            startActivity(BannedListActivity.newIntent(getContext(), channel.getUrl()));
        });

        if (channel instanceof GroupChannel) {
            boolean isBroadcast = ((GroupChannel)channel).isBroadcast();
            binding.mutedMembersItem.setVisibility(isBroadcast ? View.GONE : View.VISIBLE);
            binding.freezeChannelItem.setVisibility(isBroadcast ? View.GONE : View.VISIBLE);

            binding.freezeChannelItem.setOnClickListener(v -> {
                Logger.dev("++ freeze item clicked");
                if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, ModerationMenu.FREEZE_CHANNEL, channel)) {
                    return;
                }
                freezeOrUnFreezeChannel((GroupChannel) channel);
            });
            binding.freezeChannelItem.setOnActionMenuClickListener(v -> {
                Logger.dev("++ menu action clicked");
                if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, ModerationMenu.FREEZE_CHANNEL, channel)) {
                    return;
                }
                freezeOrUnFreezeChannel((GroupChannel) channel);
            });
        } else {
            binding.freezeChannelItem.setVisibility(View.GONE);
        }
    }

    private void freezeOrUnFreezeChannel(@NonNull GroupChannel channel) {
        boolean isFrozen = channel.isFrozen();
        loadingDialogHandler.shouldShowLoadingDialog();
        if (isFrozen) {
            channel.unfreeze(e -> {
                loadingDialogHandler.shouldDismissLoadingDialog();
            });
        } else {
            channel.freeze(e -> {
                loadingDialogHandler.shouldDismissLoadingDialog();
            });
        }
    }

    private void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * Sets the click listener on the left button of the header.
     *
     * @param listener The callback that will run.
     * @since 1.2.0
     */
    protected void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    /**
     * Sets the moderation menu click listener.
     *
     * @param listener The callback that will run.
     * @since 1.2.0
     */
    protected void setOnMenuItemClickListener(OnMenuItemClickListener<ModerationMenu, BaseChannel> listener) {
        this.menuItemClickListener = listener;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 1.2.5
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        showWaitingDialog();
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 1.2.5
     */
    @Override
    public void shouldDismissLoadingDialog() {
        dismissWaitingDialog();
    }

    public static class Builder {
        private final Bundle bundle;
        private ModerationFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private OnMenuItemClickListener<ModerationMenu, BaseChannel> menuItemClickListener;
        private LoadingDialogHandler loadingDialogHandler;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         *
         * @since 1.2.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendBirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode {@link SendBirdUIKit.ThemeMode}
         *
         * @since 1.2.0
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
         *
         * @since 1.2.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom moderation fragment. It must inherit {@link ModerationFragment}.
         * @param fragment custom moderation fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.2.0
         */
        public <T extends ModerationFragment> Builder setCustomModerationFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.2.0
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
         *
         * @since 1.2.0
         */
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.2.0
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
         *
         * @since 1.2.0
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
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.2.0
         */
        public Builder setHeaderLeftButtonListener(View.OnClickListener listener) {
            this.headerLeftButtonListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.2.0
         */
        public Builder setOnMenuItemClickListener(OnMenuItemClickListener<ModerationMenu, BaseChannel> listener) {
            this.menuItemClickListener = listener;
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * @since 1.2.5
         */
        public Builder setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Creates an {@link ModerationFragment} with the arguments supplied to this
         * builder.
         * @return The {@link ModerationFragment} applied to the {@link Bundle}.
         *
         * @since 1.2.0
         */
        public ModerationFragment build() {
            ModerationFragment fragment = customFragment != null ? customFragment : new ModerationFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setOnMenuItemClickListener(menuItemClickListener);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
