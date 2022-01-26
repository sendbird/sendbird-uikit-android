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
import com.sendbird.android.Member;
import com.sendbird.android.OperatorListQuery;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.PromoteOperatorsActivity;
import com.sendbird.uikit.activities.adapter.UserTypeListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomMemberListQueryHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.DialogUtils;

import java.util.Collections;

/**
 * Fragment displaying the operators of the channel.
 *
 * @since 1.2.0
 */
public class OperatorListFragment extends UserTypeListFragment implements LoadingDialogHandler {
    @Override
    protected void onConfigure() {
        super.onConfigure();
        if (channel.getMyRole() != Member.Role.OPERATOR) finish();
        if (customQueryHandler == null) setCustomQueryHandler(new OperatorQueryHandler(channel));
        if (headerRightButtonListener == null) {
            setHeaderRightButtonListener(v -> {
                if (isActive() && getContext() != null) {
                    startActivity(PromoteOperatorsActivity.newIntent(getContext(), channel.getUrl()));
                }
            });
        }
    }

    @Override
    protected void onActionItemClicked(View view, int position, User user) {
        if (getContext() == null || getFragmentManager() == null) return;
        Logger.d(">> OperatorListFragment::onActionItemClicked()");
        DialogListItem[] items;
        DialogListItem removeOperator = new DialogListItem(R.string.sb_text_dismiss_operator);
        items = new DialogListItem[]{removeOperator};
        DialogUtils.buildItems(user.getNickname(),
                (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                items, (v, p, item) -> removeOperator(user.getUserId())).showSingle(getFragmentManager());
    }

    private void removeOperator(@NonNull String userId) {
        Logger.d(">> OperatorListFragment::removeOperator()");
        loadingDialogHandler.shouldShowLoadingDialog();
        channel.removeOperators(Collections.singletonList(userId), e -> {
            loadingDialogHandler.shouldDismissLoadingDialog();
            if (e != null) {
                toastError(R.string.sb_text_error_dismiss_operator);
            }
        });
    }

    @Override
    protected void onOperatorDismissed() {
        finish();
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

    private static class OperatorQueryHandler implements CustomMemberListQueryHandler<User> {
        private OperatorListQuery query;
        private final GroupChannel channel;

        OperatorQueryHandler(@NonNull GroupChannel channel) {
            this.channel = channel;
        }

        @Override
        public void loadInitial(OnListResultHandler<User> handler) {
            this.query = channel.createOperatorListQuery();
            this.query.setLimit(30);
            load(handler);
        }

        @Override
        public void load(OnListResultHandler<User> handler) {
            Logger.d(">> OperatorQueryHandler::load()");
            this.query.next(handler::onResult);
        }

        @Override
        public boolean hasMore() {
            return this.query.hasNext();
        }
    }

    public static class Builder {
        private final Bundle bundle;
        private OperatorListFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private View.OnClickListener headerRightButtonListener;
        private UserTypeListAdapter adapter;
        private OnItemClickListener<User> itemClickListener;
        private OnItemLongClickListener<User> itemLongClickListener;
        private OnItemClickListener<User> actionItemClickListener;
        private OnItemClickListener<User> profileClickListener;
        private LoadingDialogHandler loadingDialogHandler;

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
            this(channelUrl, themeMode.getResId());
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
         * Sets the custom operator list fragment. It must inherit {@link OperatorListFragment}.
         * @param fragment custom operator list fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.2.0
         */
        public <T extends OperatorListFragment> Builder setCustomOperatorListFragment(T fragment) {
            this.customFragment = fragment;
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
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderRightButtonIconResId(@DrawableRes int resId) {
            return setHeaderRightButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setEmptyIcon(@DrawableRes int resId) {
            return setEmptyIcon(resId, null);
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
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
         */
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
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
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderRightButtonListener(View.OnClickListener listener) {
            this.headerRightButtonListener = listener;
            return this;
        }

        /**
         * Sets the channel user list adapter.
         *
         * @param adapter the adapter for the channel user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public <T extends UserTypeListAdapter> Builder setUserListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of channel user list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemClickListener(OnItemClickListener<User> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of channel user list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemLongClickListener(OnItemLongClickListener<User> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the action item click listener on the item of channel user list.
         *
         * @param actionItemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setActionItemClickListener(OnItemClickListener<User> actionItemClickListener) {
            this.actionItemClickListener = actionItemClickListener;
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.2
         */
        public Builder setOnProfileClickListener(OnItemClickListener<User> profileClickListener) {
            this.profileClickListener = profileClickListener;
            return this;
        }

        /**
         * Sets whether the user profile uses.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown when the profile image clicked, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.2
         */
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
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
         * Creates an {@link OperatorListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OperatorListFragment} applied to the {@link Bundle}.
         */
        public OperatorListFragment build() {
            OperatorListFragment fragment = customFragment != null ? customFragment : new OperatorListFragment();
            fragment.setArguments(bundle);
            fragment.setUserListAdapter(adapter);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setHeaderRightButtonListener(headerRightButtonListener);
            fragment.setItemClickListener(itemClickListener);
            fragment.setItemLongClickListener(itemLongClickListener);
            fragment.setActionItemClickListener(actionItemClickListener);
            fragment.setOnProfileClickListener(profileClickListener);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
