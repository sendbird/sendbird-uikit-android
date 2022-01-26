package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.Member;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.ParticipantListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.adapter.UserTypeListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentUserTypeListBinding;
import com.sendbird.uikit.interfaces.CustomMemberListQueryHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.vm.UserTypeListViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.List;

/**
 * Fragment displays a list of participants on this channel.
 * It can create through the {@link Builder}.
 *
 * @see Builder
 * @since 2.0.0
 */
public class ParticipantsListFragment extends BaseOpenChannelFragment {
    private SbFragmentUserTypeListBinding binding;

    protected View.OnClickListener headerLeftButtonListener;
    protected UserTypeListAdapter adapter;
    protected OnItemClickListener<User> itemClickListener;
    protected OnItemLongClickListener<User> itemLongClickListener;
    protected OnItemClickListener<User> profileClickListener;

    public ParticipantsListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> ChannelUserListFragment::onCreate()");
        Bundle args = getArguments();
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        if (args != null) {
            themeResId = args.getInt(StringSet.KEY_THEME_RES_ID);
        }

        if (getActivity() != null) {
            getActivity().setTheme(themeResId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_user_type_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.statusFrame.setStatus(StatusFrameView.Status.LOADING);
        initHeaderOnCreated();
    }

    @Override
    protected void onReadyFailure() {
        Logger.i(">> ParticipantsListFragment::onReadyFailure()");
        setErrorFrame();
    }

    @Override
    protected void onDrawPage() {
        if (!containsExtra(StringSet.KEY_CHANNEL_URL)) {
            setErrorFrame();
            return;
        }
        initHeaderOnReady();
        initParticipantsList(channel);
    }

    @Override
    protected void onConfigure() {
    }

    protected void setErrorFrame() {
        binding.statusFrame.setStatus(StatusFrameView.Status.CONNECTION_ERROR);
        binding.statusFrame.setOnActionEventListener(v -> {
            binding.statusFrame.setStatus(StatusFrameView.Status.LOADING);
            connect();
        });
    }

    private void initHeaderOnCreated() {
        Bundle args = getArguments();
        String headerTitle = getString(R.string.sb_text_header_member_list);
        boolean useHeader = false;
        boolean useHeaderLeftButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        ColorStateList headerLeftButtonIconTint = null;
        int emptyIconId = R.drawable.icon_chat;
        ColorStateList emptyIconTint = null;
        int emptyTextId = R.string.sb_text_user_list_empty;

        if (args != null) {
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, headerTitle);
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
            emptyIconId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_chat);
            emptyIconTint = args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT);
            emptyTextId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_user_list_empty);
        }

        binding.abvMemberList.setVisibility(useHeader ? View.VISIBLE : View.GONE);

        binding.abvMemberList.getTitleTextView().setText(headerTitle);
        binding.abvMemberList.setUseLeftImageButton(useHeaderLeftButton);
        binding.abvMemberList.setUseRightButton(false);
        binding.abvMemberList.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.abvMemberList.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.abvMemberList.setLeftImageButtonClickListener(v -> finish());

        binding.statusFrame.setEmptyIcon(emptyIconId);
        binding.statusFrame.setEmptyText(emptyTextId);
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
            binding.statusFrame.setIconTint(emptyIconTint);
        }
    }

    private void initHeaderOnReady() {
        if (headerLeftButtonListener != null) {
            binding.abvMemberList.setLeftImageButtonClickListener(headerLeftButtonListener);
        }
    }

    private void initParticipantsList(@NonNull OpenChannel channel) {
        UserTypeListViewModel viewModel = new ViewModelProvider(getViewModelStore(), new ViewModelFactory(channel, new ParticipantsListQuery(channel))).get(channel.getUrl(), UserTypeListViewModel.class);
        getLifecycle().addObserver(viewModel);
        if (adapter == null) {
            adapter = new UserTypeListAdapter();
        }

        Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, SendBirdUIKit.shouldUseDefaultUserProfile());
        adapter.setOnItemClickListener(itemClickListener != null ? itemClickListener : this::onItemClicked);
        adapter.setOnItemLongClickListener(itemLongClickListener != null ? itemLongClickListener : this::onItemLongClicked);
        adapter.setOnProfileClickListener(profileClickListener != null ? profileClickListener : useUserProfile ? this::onProfileClicked : null);

        binding.rvMemberList.setAdapter(adapter);
        binding.rvMemberList.setHasFixedSize(true);
        binding.rvMemberList.setPager(viewModel);
        binding.rvMemberList.setThreshold(5);

        viewModel.getStatusFrame().observe(this, binding.statusFrame::setStatus);
        viewModel.getMemberList().observe(this, users -> {
            Logger.dev("++ observing result users size : %s", users.size());
            adapter.setItems((List<User>) users, channel.isOperator(SendBird.getCurrentUser()) ? Member.Role.OPERATOR : Member.Role.NONE);
        });
        viewModel.getChannelDeleted().observe(this, isDeleted -> {
            if (isDeleted) finish();
        });
        viewModel.loadInitial();
    }

    /**
     * Called when a participants item has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The user data that was clicked.
     */
    protected void onItemClicked(View view, int position, User user) {
        Logger.d(">> ParticipantsListFragment::onItemClicked()");
    }

    /**
     * Called when a participants item has been long clicked.
     *
     * @param view     The view that was long clicked.
     * @param position The position that was long clicked.
     * @param user     The user data that was long clicked.
     */
    protected void onItemLongClicked(View view, int position, User user) {
        Logger.d(">> ParticipantsListFragment::onItemLongClicked()");
    }

    /**
     * Called when the user profile has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The member data that was clicked.
     */
    protected void onProfileClicked(View view, int position, User user) {
        if (getContext() == null || getFragmentManager() == null) return;
        DialogUtils.buildUserProfile(getContext(), user, false, null, null).showSingle(getFragmentManager());
    }

    /**
     * Sets the click listener on the left button of the header.
     *
     * @param listener The callback that will run.
     */
    protected void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    /**
     * Sets the click listener on the item of channel participants list.
     *
     * @param itemClickListener The callback that will run.
     */
    protected void setItemClickListener(OnItemClickListener<User> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * Sets the long click listener on the item of channel participants list.
     *
     * @param itemLongClickListener The callback that will run.
     */
    protected void setItemLongClickListener(OnItemLongClickListener<User> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    /**
     * Sets the channel participants list adapter.
     *
     * @param adapter the adapter for the channel user list.
     */
    protected <T extends UserTypeListAdapter> void setUserListAdapter(T adapter) {
        this.adapter = adapter;
    }

    void setOnProfileClickListener(OnItemClickListener<User> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    private static class ParticipantsListQuery implements CustomMemberListQueryHandler<User> {
        private final OpenChannel channel;
        private ParticipantListQuery query;

        ParticipantsListQuery(@NonNull OpenChannel channel) {
            this.channel = channel;
        }

        @Override
        public void loadInitial(OnListResultHandler<User> handler) {
            this.query = channel.createParticipantListQuery();
            this.query.setLimit(30);
            load(handler);
        }

        @Override
        public void load(OnListResultHandler<User> handler) {
            query.next(handler::onResult);
        }

        @Override
        public boolean hasMore() {
            return query.hasNext();
        }
    }

    /**
     * This is a Builder that is able to create the fragment of participants list.
     * The builder provides options how the channel is showing and working. Also you can set the event handler what you want to override.
     *
     * @since 2.0.0
     */
    public static class Builder {
        private final Bundle bundle;
        private ParticipantsListFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private UserTypeListAdapter adapter;
        private OnItemClickListener<User> itemClickListener;
        private OnItemLongClickListener<User> itemLongClickListener;
        private OnItemClickListener<User> profileClickListener;

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
         * Sets the custom member list fragment. It must inherit {@link ParticipantsListFragment}.
         * @param fragment custom member list fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         */
        public <T extends ParticipantsListFragment> Builder setCustomParticipantsListFragment(T fragment) {
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
         * Sets the participants list adapter.
         *
         * @param adapter the adapter for the participants list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public <T extends UserTypeListAdapter> Builder setParticipantsListAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of participants list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemClickListener(OnItemClickListener<User> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of participants list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemLongClickListener(OnItemLongClickListener<User> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         */
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * Creates an {@link ParticipantsListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ParticipantsListFragment} applied to the {@link Bundle}.
         */
        public ParticipantsListFragment build() {
            ParticipantsListFragment fragment = customFragment != null ? customFragment : new ParticipantsListFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setItemClickListener(itemClickListener);
            fragment.setItemLongClickListener(itemLongClickListener);
            fragment.setUserListAdapter(adapter);
            fragment.setOnProfileClickListener(profileClickListener);
            return fragment;
        }
    }
}
