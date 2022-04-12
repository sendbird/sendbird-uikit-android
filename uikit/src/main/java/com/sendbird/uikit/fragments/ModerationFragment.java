package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.BannedUserListActivity;
import com.sendbird.uikit.activities.MutedMemberListActivity;
import com.sendbird.uikit.activities.OperatorListActivity;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.modules.ModerationModule;
import com.sendbird.uikit.modules.components.HeaderComponent;
import com.sendbird.uikit.modules.components.ModerationListComponent;
import com.sendbird.uikit.vm.ModerationViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;

/**
 * Fragment displaying the menu list to control the channel.
 * It will be displayed if the member is an operator.
 *
 * @since 1.2.0
 */
public class ModerationFragment extends BaseModuleFragment<ModerationModule, ModerationViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private OnMenuItemClickListener<ModerationListComponent.ModerationMenu, BaseChannel> menuItemClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    @NonNull
    @Override
    protected ModerationModule onCreateModule(@NonNull Bundle args) {
        return new ModerationModule(requireContext());
    }

    @Override
    protected void onConfigureParams(@NonNull ModerationModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    protected ModerationViewModel onCreateViewModel() {
        return new ViewModelProvider(getViewModelStore(), new ViewModelFactory(getChannelUrl())).get(getChannelUrl(), ModerationViewModel.class);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ModerationModule module, @NonNull ModerationViewModel viewModel) {
        Logger.d(">> ModerationFragment::onBeforeReady()");
        onBindHeaderComponent(module.getHeaderComponent(), viewModel, viewModel.getChannel());
        onBindModerationListComponent(module.getModerationListComponent(), viewModel, viewModel.getChannel());
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ModerationModule module, @NonNull ModerationViewModel viewModel) {
        Logger.d(">> ModerationFragment::onReady status=%s", status);

        final ModerationListComponent moderationListComponent = getModule().getModerationListComponent();

        final GroupChannel channel = viewModel.getChannel();
        if (channel != null) {
            moderationListComponent.notifyChannelChanged(channel);
        }

        viewModel.getMyMemberStateChanges().observe(getViewLifecycleOwner(), memberState -> {
            if (memberState == Member.MemberState.NONE) shouldActivityFinish();
        });
        viewModel.getMyRoleChanges().observe(getViewLifecycleOwner(), role -> {
            if (role != Member.Role.OPERATOR) shouldActivityFinish();
        });

        viewModel.getIsChannelDeleted().observe(getViewLifecycleOwner(), s -> shouldActivityFinish());
        viewModel.getIsBanned().observe(getViewLifecycleOwner(), banned -> shouldActivityFinish());
        viewModel.getIsShowLoadingDialog().observe(getViewLifecycleOwner(), shouldShow -> {
            if (isFragmentAlive()) {
                if (shouldShow) {
                    shouldShowLoadingDialog();
                } else {
                    shouldDismissLoadingDialog();
                }
            }
        });
    }

    /**
     * Called to bind events to the HeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ModerationModule, ModerationViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.0.0
     */
    protected void onBindHeaderComponent(@NonNull HeaderComponent headerComponent, @NonNull ModerationViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ModerationFragment::onBindHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
    }

    /**
     * Called to bind events to the ModerationListComponent. This is called from {@link #onBeforeReady(ReadyStatus, ModerationModule, ModerationViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param moderationListComponent The component to which the event will be bound
     * @param viewModel               A view model that provides the data needed for the fragment
     * @param channel                 The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.0.0
     */
    protected void onBindModerationListComponent(@NonNull ModerationListComponent moderationListComponent, @NonNull ModerationViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ModerationFragment::onBindBannedUserListComponent()");

        if (channel == null) return;
        moderationListComponent.setOnMenuItemClickListener((view, menu, data) -> {
            Logger.dev("++ %s item clicked", menu.name());
            if (menuItemClickListener != null) {
                return menuItemClickListener.onMenuItemClicked(view, menu, channel);
            }
            if (getContext() == null) return false;
            switch (menu) {
                case OPERATORS:
                    startActivity(OperatorListActivity.newIntent(getContext(), channel.getUrl()));
                    break;
                case MUTED_MEMBERS:
                    startActivity(MutedMemberListActivity.newIntent(getContext(), channel.getUrl()));
                    break;
                case BANNED_MEMBERS:
                    startActivity(BannedUserListActivity.newIntent(getContext(), channel.getUrl()));
                    break;
                case FREEZE_CHANNEL:
                    freezeOrUnFreezeChannel(channel);
                    break;
                default:
                    return false;
            }
            return true;
        });
        viewModel.getFrozenStateChanges().observe(getViewLifecycleOwner(), baseChannel -> moderationListComponent.notifyChannelChanged((GroupChannel) baseChannel));
    }

    private void freezeOrUnFreezeChannel(@NonNull GroupChannel channel) {
        boolean isFrozen = channel.isFrozen();
        if (getContext() != null) {
            if (isFrozen) {
                getViewModel().unfreezeChannel();
            } else {
                getViewModel().freezeChannel();
            }
        }
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 1.2.5
     */
    protected boolean shouldShowLoadingDialog() {
        if (getContext() != null) {
            return getModule().shouldShowLoadingDialog(requireContext());
        }
        return false;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 1.2.5
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * @since 3.0.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
    }

    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private OnMenuItemClickListener<ModerationListComponent.ModerationMenu, BaseChannel> menuItemClickListener;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @since 1.2.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         * @since 1.2.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * @since 1.2.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        @NonNull
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
         * @since 1.2.0
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIconResId(@DrawableRes int resId) {
            return setHeaderLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        @NonNull
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
         * @since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        @NonNull
        public Builder setOnMenuItemClickListener(@NonNull OnMenuItemClickListener<ModerationListComponent.ModerationMenu, BaseChannel> listener) {
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
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Creates an {@link ModerationFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ModerationFragment} applied to the {@link Bundle}.
         * @since 1.2.0
         */
        @NonNull
        public ModerationFragment build() {
            ModerationFragment fragment = new ModerationFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.menuItemClickListener = menuItemClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            return fragment;
        }
    }
}
