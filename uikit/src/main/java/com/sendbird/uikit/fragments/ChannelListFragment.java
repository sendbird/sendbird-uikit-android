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
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.activities.CreateChannelActivity;
import com.sendbird.uikit.activities.adapter.ChannelListAdapter;
import com.sendbird.uikit.consts.CreateableChannelType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentChannelListBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.Available;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.vm.ChannelListViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.SelectChannelTypeView;
import com.sendbird.uikit.widgets.StatusFrameView;

/**
 * Fragment displaying the list of channels.
 */
public class ChannelListFragment extends BaseGroupChannelFragment {
    private SbFragmentChannelListBinding binding;
    private ChannelListViewModel viewModel;

    private View.OnClickListener headerLeftButtonListener;
    private View.OnClickListener headerRightButtonListener;
    private ChannelListAdapter adapter;
    private GroupChannelListQuery query;
    private OnItemClickListener<GroupChannel> itemClickListener;
    private OnItemLongClickListener<GroupChannel> itemLongClickListener;

    public ChannelListFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> ChannelListFragment::onCreate()");
        Bundle args = getArguments();
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        if (args != null) {
            themeResId = args.getInt(StringSet.KEY_THEME_RES_ID, SendBirdUIKit.getDefaultThemeMode().getResId());
        }

        if (getActivity() != null) {
            getActivity().setTheme(themeResId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_channel_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.statusFrame.setStatus(StatusFrameView.Status.LOADING);
        initHeaderOnCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    protected void onReadyFailure() {
        setErrorFrame();
    }

    @Override
    protected void onConfigure() {
    }

    @Override
    protected void onDrawPage() {
        Logger.i(">> ChannelListFragment::onDrawPage()");
        initHeaderOnReady();
        initChannelList();
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
        String headerTitle = getString(R.string.sb_text_header_channel_list);
        boolean useHeader = false;
        boolean useHeaderLeftButton = true;
        boolean useHeaderRightButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        int headerRightButtonIconResId = R.drawable.icon_create;
        ColorStateList headerLeftButtonIconTint = null;
        ColorStateList headerRightButtonIconTint = null;

        if (args != null) {
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, getString(R.string.sb_text_header_channel_list));
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            useHeaderRightButton = args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerRightButtonIconResId = args.getInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, R.drawable.icon_create);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
            headerRightButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT);
        }

        binding.abvChannelList.setVisibility(useHeader ? View.VISIBLE : View.GONE);

        binding.abvChannelList.getTitleTextView().setText(headerTitle);

        binding.abvChannelList.setUseLeftImageButton(useHeaderLeftButton);
        binding.abvChannelList.setUseRightButton(useHeaderRightButton);

        binding.abvChannelList.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.abvChannelList.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.abvChannelList.setRightImageButtonResource(headerRightButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID)) {
            binding.abvChannelList.setRightImageButtonTint(headerRightButtonIconTint);
        }
        binding.abvChannelList.setLeftImageButtonClickListener(v -> finish());
    }

    private void initHeaderOnReady() {
        if (headerLeftButtonListener != null) {
            binding.abvChannelList.setLeftImageButtonClickListener(headerLeftButtonListener);
        }

        if (headerRightButtonListener != null) {
            binding.abvChannelList.setRightImageButtonClickListener(headerRightButtonListener);
        } else {
            binding.abvChannelList.setRightImageButtonClickListener(v -> {
                if (getContext() != null && getFragmentManager() != null) {
                    if (Available.isSupportSuper() || Available.isSupportBroadcast()) {
                        final SelectChannelTypeView layout = new SelectChannelTypeView(getContext());
                        layout.canCreateSuperGroupChannel(Available.isSupportSuper());
                        layout.canCreateBroadcastGroupChannel(Available.isSupportBroadcast());
                        SendBirdDialogFragment dialogFragment = DialogUtils.buildContentViewTop(layout);
                        layout.setOnItemClickListener((view, position, channelType) -> {
                            if (dialogFragment != null) {
                                dialogFragment.dismiss();
                            }
                            Logger.dev("++ channelType : " + channelType);
                            if (isActive()) {
                                onSelectedChannelType(channelType);
                            }
                        });
                        dialogFragment.showSingle(getFragmentManager());
                    } else {
                        if (isActive()) {
                            onSelectedChannelType(CreateableChannelType.Normal);
                        }
                    }
                }
            });
        }
    }

    /**
     * A callback that selected channel types.
     *
     * @param channelType selected channel type. see this {@link CreateableChannelType}
     * @since 1.2.0
     */
    protected void onSelectedChannelType(@NonNull CreateableChannelType channelType) {
        startActivity(CreateChannelActivity.newIntent(getContext(), channelType));
    }

    private void initChannelList() {
        Bundle args = getArguments();
        boolean includeEmpty = false;
        if (args != null) {
            includeEmpty = args.getBoolean(StringSet.KEY_INCLUDE_EMPTY, false);
        }

        // if a query exists, includeEmpty property has to ignore. if don't the two values will be conflicted.
        if (query == null) {
            query = GroupChannel.createMyGroupChannelListQuery();
            query.setIncludeEmpty(includeEmpty);
        }

        viewModel = new ViewModelProvider(this, new ViewModelFactory()).get(ChannelListViewModel.class);

        initAdapter();
        binding.rvGroupChannelList.setAdapter(adapter);
        binding.rvGroupChannelList.setHasFixedSize(true);
        binding.rvGroupChannelList.setPager(viewModel);
        binding.rvGroupChannelList.setItemAnimator(new ItemAnimator());
        binding.rvGroupChannelList.setThreshold(5);

        if (args != null && args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
            int emptyIconResId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_chat);
            binding.statusFrame.setEmptyIcon(emptyIconResId);
            ColorStateList emptyIconTint = args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT);
            binding.statusFrame.setIconTint(emptyIconTint);
        }
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_TEXT_RES_ID)) {
            int emptyTextResId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_channel_list_empty);
            binding.statusFrame.setEmptyText(emptyTextResId);
        }
        viewModel.getStatusFrame().observe(this, binding.statusFrame::setStatus);
        viewModel.getChannelList().observe(this, groupChannels -> {
            adapter.setItems(groupChannels);
        });
        viewModel.getErrorToast().observe(this, this::toastError);
        viewModel.loadInitial(query);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                if (toPosition == 0 && adapter != null) {
                    if (binding.rvGroupChannelList.findFirstVisibleItemPosition() == 0) {
                        binding.rvGroupChannelList.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0 && adapter != null) {
                    if (binding.rvGroupChannelList.findFirstVisibleItemPosition() == 0) {
                        binding.rvGroupChannelList.scrollToPosition(0);
                    }
                }
            }
        });
    }

    /**
     * Leaves this channel.
     *
     * @since 1.0.4
     */
    protected void leaveChannel(@NonNull GroupChannel channel) {
        if (viewModel != null) {
            viewModel.leaveChannel(channel);
        }
    }

    private void initAdapter() {
        if (adapter == null) {
            adapter = new ChannelListAdapter();
        }

        if (itemClickListener == null) {
            itemClickListener = (view, position, channel) -> {
                Logger.d("++ ChannelListFragment::onItemClicked()");
                if (isActive()) {
                    startActivity(ChannelActivity.newIntent(getContext(), channel.getUrl()));
                }
            };
        }

        if (itemLongClickListener == null) {
            itemLongClickListener = (view1, position, channel) -> {
                Logger.d("++ ChannelListFragment::onItemLongClicked()");
                DialogListItem pushOnOff = new DialogListItem(ChannelUtils.isChannelPushOff(channel) ? R.string.sb_text_channel_list_push_on : R.string.sb_text_channel_list_push_off);
                DialogListItem leaveChannel = new DialogListItem(R.string.sb_text_channel_list_leave);
                DialogListItem[] items = {pushOnOff, leaveChannel};

                if (isActive() && getFragmentManager() != null) {
                    DialogUtils.buildItems(ChannelUtils.makeTitleText(getContext(), channel),
                            (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                            items, (v, p, key) -> {
                                if (key == R.string.sb_text_channel_list_leave) {
                                    Logger.dev("leave channel");
                                    leaveChannel(channel);
                                } else {
                                    Logger.dev("change push notifications");
                                    viewModel.setPushNotification(channel, ChannelUtils.isChannelPushOff(channel));
                                }
                            }).showSingle(getFragmentManager());
                }
            };
        }
        adapter.setOnItemClickListener(itemClickListener);
        adapter.setOnItemLongClickListener(itemLongClickListener);
    }

    private void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    private void setHeaderRightButtonListener(View.OnClickListener listener) {
        this.headerRightButtonListener = listener;
    }

    private void setChannelListAdapter(ChannelListAdapter adapter) {
        this.adapter = adapter;
    }

    private void setItemClickListener(OnItemClickListener<GroupChannel> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private void setItemLongClickListener(OnItemLongClickListener<GroupChannel> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    private void setGroupChannelListQuery(GroupChannelListQuery query) {
        this.query = query;
    }

    public static class Builder {
        private final Bundle bundle;
        private ChannelListFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private View.OnClickListener headerRightButtonListener;
        private ChannelListAdapter adapter;
        private OnItemClickListener<GroupChannel> itemClickListener;
        private OnItemLongClickListener<GroupChannel> itemLongClickListener;
        private GroupChannelListQuery query;

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
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
        }

        /**
         * Sets the custom channel fragment. It must inherit {@link ChannelListFragment}.
         * @param fragment custom channel list fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.0.4
         */
        public <T extends ChannelListFragment> Builder setCustomChannelListFragment(T fragment) {
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
         * Sets the includeEmpty when getting the channel list.
         *
         * @param isIncludeEmpty Flag to include empty channels.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @deprecated As of 1.0.5, replaced by {@link GroupChannelListQuery#setIncludeEmpty(boolean)} )}. If the GroupChannelListQuery was set, this value will be ignored.
         */
        @Deprecated
        public Builder setIsIncludeEmpty(boolean isIncludeEmpty) {
            bundle.putBoolean(StringSet.KEY_INCLUDE_EMPTY, isIncludeEmpty);
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
         * Sets the channel list adapter.
         *
         * @param adapter the adapter for the channel list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setChannelListAdapter(ChannelListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of channel list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemClickListener(OnItemClickListener<GroupChannel> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of channel list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItemLongClickListener(OnItemLongClickListener<GroupChannel> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the query instance to get <code>GroupChannel</code>s the current <code>User</code> has joined.
         * @param query The GroupChannelListQuery instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.0.5
         */
        public Builder setGroupChannelListQuery(GroupChannelListQuery query) {
            this.query = query;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.0.2
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
         * @since 2.0.2
         */
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Creates an {@link ChannelListFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelListFragment} applied to the {@link Bundle}.
         */
        public ChannelListFragment build() {
            ChannelListFragment fragment = customFragment != null ? customFragment : new ChannelListFragment();
            fragment.setArguments(bundle);
            fragment.setChannelListAdapter(adapter);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setHeaderRightButtonListener(headerRightButtonListener);
            fragment.setItemClickListener(itemClickListener);
            fragment.setItemLongClickListener(itemLongClickListener);
            fragment.setGroupChannelListQuery(query);
            return fragment;
        }
    }
}
