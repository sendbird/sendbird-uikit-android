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
import com.sendbird.android.GroupChannelMemberListQuery;
import com.sendbird.android.Member;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.InviteChannelActivity;
import com.sendbird.uikit.activities.adapter.MemberListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomMemberListQueryHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.DialogUtils;

import java.util.Collections;

/**
 * Fragment displaying the member list in the channel.
 */
public class MemberListFragment extends MemberTypeListFragment {
    @Override
    protected void onConfigure() {
        super.onConfigure();
        if (customQueryHandler == null) {
            CustomMemberListQueryHandler<Member> query = new MemberListQuery(channel);
            setCustomQueryHandler(query);
        }

        if (headerRightButtonListener == null) {
            setHeaderRightButtonListener(v -> {
                if (isActive() && getContext() != null) {
                    startActivity(InviteChannelActivity.newIntent(getContext(), channel.getUrl()));
                }
            });
        }
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

    @Override
    protected void onActionItemClicked(View view, int position, Member member) {
        if (member == null || getContext() == null || getFragmentManager() == null) return;
        boolean isMuted = member.isMuted();
        boolean isOperator = member.getRole() == Member.Role.OPERATOR;
        DialogListItem promoteOperator = new DialogListItem(isOperator ? R.string.sb_text_dismiss_operator : R.string.sb_text_promote_operator);
        DialogListItem muteMember = new DialogListItem(isMuted ? R.string.sb_text_unmute_member : R.string.sb_text_mute_member);
        DialogListItem banMember = new DialogListItem(R.string.sb_text_ban_member, 0, true);
        DialogListItem[] items = !channel.isBroadcast() ?
                new DialogListItem[]{promoteOperator, muteMember, banMember} :
                new DialogListItem[]{promoteOperator, banMember};
        DialogUtils.buildItems(member.getNickname(),
                (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                items, (v, p, item) -> {
                    final int key = item.getKey();
                    if (key == R.string.sb_text_promote_operator) {
                        addOperator(member.getUserId());
                    } else if (key == R.string.sb_text_dismiss_operator) {
                        removeOperator(member.getUserId());
                    } else if (key == R.string.sb_text_mute_member) {
                        muteUser(member.getUserId());
                    } else if (key == R.string.sb_text_unmute_member) {
                        unmuteUser(member.getUserId());
                    } else if (key == R.string.sb_text_ban_member) {
                        banUser(member.getUserId());
                    }
                }).showSingle(getFragmentManager());
    }

    private void addOperator(@NonNull String userId) {
        loadingDialogHandler.shouldShowLoadingDialog();
        channel.addOperators(Collections.singletonList(userId), e -> {
            loadingDialogHandler.shouldDismissLoadingDialog();
            if (e != null) {
                toastError(R.string.sb_text_error_promote_operator);
            }
        });
    }

    private void removeOperator(@NonNull String userId) {
        loadingDialogHandler.shouldShowLoadingDialog();
        channel.removeOperators(Collections.singletonList(userId), e -> {
            loadingDialogHandler.shouldDismissLoadingDialog();
            if (e != null) {
                toastError(R.string.sb_text_error_dismiss_operator);
            }
        });
    }

    private void muteUser(@NonNull String userId) {
        loadingDialogHandler.shouldShowLoadingDialog();
        channel.muteUserWithUserId(userId, e -> {
            loadingDialogHandler.shouldDismissLoadingDialog();
            if (e != null) {
                toastError(R.string.sb_text_error_mute_member);
            }
        });
    }

    private void unmuteUser(@NonNull String userId) {
        loadingDialogHandler.shouldShowLoadingDialog();
        channel.unmuteUserWithUserId(userId, e -> {
            loadingDialogHandler.shouldDismissLoadingDialog();
            if (e != null) {
                toastError(R.string.sb_text_error_unmute_member);
            }
        });
    }

    private void banUser(@NonNull String userId) {
        loadingDialogHandler.shouldShowLoadingDialog();
        channel.banUserWithUserId(userId, null, -1, e -> {
            loadingDialogHandler.shouldDismissLoadingDialog();
            if (e != null) {
                toastError(R.string.sb_text_error_ban_member);
            }
        });
    }

    private static class MemberListQuery implements CustomMemberListQueryHandler<Member> {
        private final GroupChannel channel;
        private GroupChannelMemberListQuery query;

        MemberListQuery(@NonNull GroupChannel channel) {
            this.channel = channel;
        }

        @Override
        public void loadInitial(OnListResultHandler<Member> handler) {
            this.query = channel.createMemberListQuery();
            this.query.setLimit(30);
            load(handler);
        }

        @Override
        public void load(OnListResultHandler<Member> handler) {
            query.next(handler::onResult);
        }

        @Override
        public boolean hasMore() {
            return query.hasNext();
        }
    }

    public static class Builder {
        private final Bundle bundle;
        private MemberListFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private View.OnClickListener headerRightButtonListener;
        private MemberListAdapter adapter;
        private OnItemClickListener<Member> itemClickListener;
        private OnItemLongClickListener<Member> itemLongClickListener;
        private OnItemClickListener<Member> actionItemClickListener;
        private OnItemClickListener<Member> profileClickListener;
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
         * Sets the custom member list fragment. It must inherit {@link MemberListFragment}.
         * @param fragment custom member list fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.0.4
         */
        public <T extends MemberListFragment> Builder setCustomMemberListFragment(T fragment) {
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
         * Sets the member list adapter.
         *
         * @param adapter the adapter for the member list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public <T extends MemberListAdapter> Builder setMemberListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of member list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemClickListener(OnItemClickListener<Member> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of member list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemLongClickListener(OnItemLongClickListener<Member> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the action item click listener on the item of channel user list.
         *
         * @param actionItemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setActionItemClickListener(OnItemClickListener<Member> actionItemClickListener) {
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
        public Builder setOnProfileClickListener(OnItemClickListener<Member> profileClickListener) {
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
         * Creates an {@link MemberListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link MemberListFragment} applied to the {@link Bundle}.
         */
        public MemberListFragment build() {
            MemberListFragment fragment = customFragment != null ? customFragment : new MemberListFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setHeaderRightButtonListener(headerRightButtonListener);
            fragment.setItemClickListener(itemClickListener);
            fragment.setItemLongClickListener(itemLongClickListener);
            fragment.setMemberListAdapter(adapter);
            fragment.setActionItemClickListener(actionItemClickListener);
            fragment.setOnProfileClickListener(profileClickListener);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
