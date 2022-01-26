package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.adapter.UserTypeListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentUserTypeListBinding;
import com.sendbird.uikit.interfaces.CustomMemberListQueryHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.vm.UserTypeListViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.List;

/**
 * Fragment displaying the member list in the channel.
 * The data type has to be a {@link User} instance.
 *
 * @since 1.2.0
 */
abstract public class UserTypeListFragment extends BaseGroupChannelFragment implements LoadingDialogHandler {
    private SbFragmentUserTypeListBinding binding;

    protected View.OnClickListener headerLeftButtonListener;
    protected View.OnClickListener headerRightButtonListener;
    protected UserTypeListAdapter adapter;
    protected OnItemClickListener<User> itemClickListener;
    protected OnItemLongClickListener<User> itemLongClickListener;
    protected OnItemClickListener<User> actionItemClickListener;
    protected CustomMemberListQueryHandler<User> customQueryHandler;
    protected OnItemClickListener<User> profileClickListener;
    protected LoadingDialogHandler loadingDialogHandler;

    public UserTypeListFragment() {
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
        Logger.i(">> UserTypeListFragment::onReadyFailure()");
        setErrorFrame();
    }

    protected void onConfigure() {
        if (this.loadingDialogHandler == null) {
            this.loadingDialogHandler = this;
        }
    }

    @Override
    protected void onDrawPage() {
        if (!containsExtra(StringSet.KEY_CHANNEL_URL)) {
            setErrorFrame();
            return;
        }
        initHeaderOnReady();
        initChannelUserList(channel);
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
        boolean useHeaderRightButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        int headerRightButtonIconResId = R.drawable.icon_plus;
        ColorStateList headerLeftButtonIconTint = null;
        ColorStateList headerRightButtonIconTint = null;
        int emptyIconId = R.drawable.icon_chat;
        ColorStateList emptyIconTint = null;
        int emptyTextId = R.string.sb_text_user_list_empty;

        if (args != null) {
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, headerTitle);
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            useHeaderRightButton = args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerRightButtonIconResId = args.getInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, R.drawable.icon_plus);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
            headerRightButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT);
            emptyIconId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_chat);
            emptyIconTint = args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT);
            emptyTextId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_user_list_empty);
        }

        binding.abvMemberList.setVisibility(useHeader ? View.VISIBLE : View.GONE);

        binding.abvMemberList.getTitleTextView().setText(headerTitle);
        binding.abvMemberList.setUseLeftImageButton(useHeaderLeftButton);
        binding.abvMemberList.setLeftImageButtonClickListener(v -> finish());
        binding.abvMemberList.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.abvMemberList.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.abvMemberList.setRightImageButtonResource(headerRightButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID)) {
            binding.abvMemberList.setRightImageButtonTint(headerRightButtonIconTint);
        }
        binding.abvMemberList.setUseRightButton(useHeaderRightButton);

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

        if (headerRightButtonListener != null) {
            binding.abvMemberList.setRightImageButtonClickListener(headerRightButtonListener);
        }
    }

    private void initChannelUserList(@NonNull GroupChannel channel) {
        UserTypeListViewModel viewModel = new ViewModelProvider(getViewModelStore(), new ViewModelFactory(channel, customQueryHandler)).get(channel.getUrl(), UserTypeListViewModel.class);
        getLifecycle().addObserver(viewModel);
        if (adapter == null) {
            adapter = new UserTypeListAdapter();
        }

        Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, SendBirdUIKit.shouldUseDefaultUserProfile());
        adapter.setOnItemClickListener(itemClickListener != null ? itemClickListener : this::onItemClicked);
        adapter.setOnItemLongClickListener(itemLongClickListener != null ? itemLongClickListener : this::onItemLongClicked);
        adapter.setOnActionItemClickListener(actionItemClickListener != null ? actionItemClickListener : this::onActionItemClicked);
        adapter.setOnProfileClickListener(profileClickListener != null ? profileClickListener : useUserProfile ? this::onProfileClicked : null);

        binding.rvMemberList.setAdapter(adapter);
        binding.rvMemberList.setHasFixedSize(true);
        binding.rvMemberList.setPager(viewModel);
        binding.rvMemberList.setThreshold(5);

        viewModel.getStatusFrame().observe(this, binding.statusFrame::setStatus);
        viewModel.getMemberList().observe(this, users -> {
            Logger.dev("++ observing result users size : %s", users.size());
            adapter.setItems((List<User>) users, channel.getMyRole());
        });
        viewModel.getOperatorDismissed().observe(this, isDismissed -> {
            if (isDismissed) onOperatorDismissed();
        });
        viewModel.getChannelDeleted().observe(this, isDeleted -> {
            if (isDeleted) onChannelDeleted();
        });
        viewModel.loadInitial();
    }

    /**
     * Called when a user is dismissed from the operator.
     *
     * @since 2.1.3
     */
    protected void onOperatorDismissed() {}

    /**
     * Called when a channel is deleted from the user.
     *
     * @since 2.1.3
     */
    protected void onChannelDeleted() {
        finish();
    }

    /**
     * Called when a user item has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The user data that was clicked.
     * @since 1.2.0
     */
    protected void onItemClicked(View view, int position, User user) {
        Logger.d(">> UserTypeListFragment::onItemClicked()");
    }

    /**
     * Called when a user item has been long clicked.
     *
     * @param view     The view that was long clicked.
     * @param position The position that was long clicked.
     * @param user     The user data that was long clicked.
     * @since 1.2.0
     */
    protected void onItemLongClicked(View view, int position, User user) {
        Logger.d(">> UserTypeListFragment::onItemLongClicked()");
    }

    /**
     * Called when a user action item has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The user data that was clicked.
     * @since 1.2.0
     */
    protected void onActionItemClicked(View view, int position, User user) {
        Logger.d(">> UserTypeListFragment::onActionItemClicked()");
    }

    /**
     * Called when the user profile has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param user     The member data that was clicked.
     * @since 1.2.2
     */
    protected void onProfileClicked(View view, int position, User user) {
        if (getContext() == null || getFragmentManager() == null) return;
        boolean useChannelCreateButton = !user.getUserId().equals(SendBird.getCurrentUser().getUserId());
        DialogUtils.buildUserProfile(getContext(), user, useChannelCreateButton, null, loadingDialogHandler).showSingle(getFragmentManager());
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
     * Sets the click listener on the right button of the header.
     *
     * @param listener The callback that will run.
     * @since 1.2.0
     */
    protected void setHeaderRightButtonListener(View.OnClickListener listener) {
        this.headerRightButtonListener = listener;
    }

    /**
     * Sets the click listener on the item of channel user list.
     *
     * @param itemClickListener The callback that will run.
     * @since 1.2.0
     */
    protected void setItemClickListener(OnItemClickListener<User> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * Sets the long click listener on the item of channel user list.
     *
     * @param itemLongClickListener The callback that will run.
     * @since 1.2.0
     */
    protected void setItemLongClickListener(OnItemLongClickListener<User> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    /**
     * Sets the action item click listener on the item of channel user list.
     *
     * @param actionItemClickListener The callback that will run.
     * @since 1.2.0
     */
    protected void setActionItemClickListener(OnItemClickListener<User> actionItemClickListener) {
        this.actionItemClickListener = actionItemClickListener;
    }

    /**
     * Sets the channel user list adapter.
     *
     * @param adapter the adapter for the channel user list.
     * @since 1.2.0
     */
    protected <T extends UserTypeListAdapter> void setUserListAdapter(T adapter) {
        this.adapter = adapter;
    }

    void setCustomQueryHandler(CustomMemberListQueryHandler<User> handler) {
        this.customQueryHandler = handler;
    }

    void setOnProfileClickListener(OnItemClickListener<User> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }
}