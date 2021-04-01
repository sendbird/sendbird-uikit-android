package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelMemberListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.adapter.UserListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.interfaces.UserListResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the user list.
 *
 * @since 1.2.0
 */
public class PromoteOperatorsFragment extends SelectUserFragment {

    @Override
    protected void onConfigure() {
        MemberListQuery query = new MemberListQuery(channel);
        if (customUserListQueryHandler == null) {
            setCustomUserListQueryHandler(query);
        }
        if (adapter == null) {
            setUserListAdapter(new PromoteOperatorListAdapter(query));
        }
    }

    @Override
    protected List<String> getDisabledUserIds() {
        return super.getDisabledUserIds();
    }

    @Override
    protected void onUserSelectComplete(List<String> selectedUsers) {
        Logger.d(">> PromoteOperatorsFragment::onUserSelectComplete()");
        if (channel != null) {
            channel.addOperators(selectedUsers, e -> {
                if (e != null) {
                    toastError(R.string.sb_text_error_promote_operator);
                    Logger.e(e);
                    return;
                }
                finish();
            });
        }
    }

    private static class MemberListQuery implements CustomUserListQueryHandler {
        private final GroupChannelMemberListQuery query;
        private final GroupChannel channel;
        private final List<Member> members = new ArrayList<>();

        MemberListQuery(@NonNull GroupChannel channel) {
            this.channel = channel;
            this.query = channel.createMemberListQuery();
        }

        @Override
        public void loadInitial(UserListResultHandler handler) {
            loadNext(handler);
        }

        @Override
        public void loadNext(UserListResultHandler handler) {
            if (channel.isSuper() || channel.isBroadcast()) {
                query.next((list, ex) -> {
                    if (ex != null) {
                        handler.onResult(null, ex);
                        return;
                    }

                    Logger.dev("++ list : %s", list);
                    List<UserInfo> newUsers = new ArrayList<>();
                    for (User user : list) {
                        newUsers.add(UserUtils.toUserInfo(user));
                    }

                    this.members.addAll(list);
                    handler.onResult(newUsers, null);
                });
            } else {
                List<Member> members = channel.getMembers();
                List<UserInfo> newUsers = new ArrayList<>();
                for (User member : members) {
                    newUsers.add(UserUtils.toUserInfo(member));
                }
                synchronized (this.members) {
                    this.members.addAll(members);
                }
                handler.onResult(newUsers, null);
            }
        }

        @Override
        public boolean hasMore() {
            return (channel.isSuper() || channel.isBroadcast()) && query.hasNext();
        }

        public Member getMember(UserInfo userInfo) {
            synchronized (members) {
                for (Member member : members) {
                    if (member.getUserId().equals(userInfo.getUserId())) {
                        return member;
                    }
                }
            }
            return null;
        }
    }

    private static class PromoteOperatorListAdapter extends UserListAdapter {
        private final MemberListQuery query;
        PromoteOperatorListAdapter(MemberListQuery query) {
            this.query = query;
        }

        @Override
        protected boolean isDisabled(UserInfo userInfo) {
            Member member = query.getMember(userInfo);
            return member.getRole() == Member.Role.OPERATOR;
        }
    }

    public static class Builder {
        protected Bundle bundle;
        protected PromoteOperatorsFragment customFragment;
        protected CustomUserListQueryHandler customUserListQueryHandler = null;
        protected UserListAdapter adapter;
        protected View.OnClickListener headerLeftButtonListener;

        public Builder() {
            this(SendBirdUIKit.getDefaultThemeMode());
        }

        public Builder(@NonNull SendBirdUIKit.ThemeMode themeMode) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, themeMode.getResId());
        }

        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendBirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendBirdUIKit.ThemeMode}
         * @since 1.2.0
         */
        public Builder(@NonNull String channelUrl, @NonNull SendBirdUIKit.ThemeMode themeMode) {
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
         * Sets the custom promoting operator fragment. It must inherit {@link PromoteOperatorsFragment}.
         *
         * @param fragment custom selecting user fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        public <T extends PromoteOperatorsFragment> Builder setCustomPromoteOperatorFragment(T fragment) {
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
         * Sets the right button text of the header.
         *
         * @param rightButtonText text to be displayed to the right button.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setRightButtonText(String rightButtonText) {
            bundle.putString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, rightButtonText);
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
         * Creates an {@link PromoteOperatorsFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link PromoteOperatorsFragment} applied to the {@link Bundle}.
         */
        public Fragment build() {
            PromoteOperatorsFragment fragment = customFragment != null ? customFragment : new PromoteOperatorsFragment();
            fragment.setArguments(bundle);
            fragment.setCustomUserListQueryHandler(customUserListQueryHandler);
            fragment.setUserListAdapter(adapter);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            return fragment;
        }
    }
}
