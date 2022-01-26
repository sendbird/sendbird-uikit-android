package com.sendbird.uikit.fragments;

import android.content.Intent;
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

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageSearchQuery;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.activities.adapter.MessageSearchAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentMessageSearchBinding;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnSearchEventListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.HighlightMessageInfo;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.SearchViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.List;

public class MessageSearchFragment extends BaseGroupChannelFragment implements LoadingDialogHandler {
    private SbFragmentMessageSearchBinding binding;
    private MessageSearchAdapter adapter;
    private LoadingDialogHandler loadingDialogHandler;
    private OnSearchEventListener onSearchEventListener;
    private OnItemClickListener<BaseMessage> itemClickListener;
    private SearchViewModel viewModel;
    private MessageSearchQuery query;

    public MessageSearchFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> MessageSearchFragment::onCreate()");
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
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_message_search, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 2.1.0
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        showWaitingDialog();
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 2.1.0
     */
    @Override
    public void shouldDismissLoadingDialog() {
        dismissWaitingDialog();
    }

    @Override
    protected void onReadyFailure() {
        Logger.i(">> MessageSearchFragment::onReadyFailure()");
        setErrorFrame();
    }

    @Override
    protected void onConfigure() {
    }

    @Override
    protected void onDrawPage() {
        if (!containsExtra(StringSet.KEY_CHANNEL_URL)) {
            setErrorFrame();
            return;
        }

        initSearchBar();
        initSearchResultList(channel);
    }

    private void initSearchBar() {
        binding.searchBar.setOnSearchEventListener(keyword -> {
            Logger.d("++ request search keyword : %s", keyword);
            if (TextUtils.isEmpty(keyword.trim())) return;
            if (onSearchEventListener != null) {
                onSearchEventListener.onSearchRequested(keyword);
                return;
            }
            hideKeyboard();
            search(keyword);
        });

        Bundle args = getArguments();
        boolean useSearchBar = args != null && args.getBoolean(StringSet.KEY_USE_SEARCH_BAR, true);
        binding.searchBar.setVisibility(useSearchBar ? View.VISIBLE : View.GONE);

        if (useSearchBar) {
            final String searchText = args != null && args.containsKey(StringSet.KEY_SEARCH_BAR_BUTTON_TEXT) ? args.getString(StringSet.KEY_SEARCH_BAR_BUTTON_TEXT) : getString(R.string.sb_text_button_search);
            binding.searchBar.getSearchButton().setText(searchText);
            binding.searchBar.getSearchButton().setEnabled(false);
            binding.searchBar.setOnInputTextChangedListener((s, start, before, count) -> binding.searchBar.getSearchButton().setEnabled(s.length() > 0));
        }
    }

    private void initSearchResultList(@NonNull GroupChannel channel) {
        viewModel = new ViewModelProvider(getViewModelStore(), new ViewModelFactory(channel, query)).get(channel.getUrl(), SearchViewModel.class);
        getLifecycle().addObserver(viewModel);

        Bundle args = getArguments();
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
            int emptyIconResId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_chat);
            binding.statusFrame.setEmptyIcon(emptyIconResId);
            ColorStateList emptyIconTint = args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT);
            binding.statusFrame.setIconTint(emptyIconTint);
        }
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_TEXT_RES_ID)) {
            int emptyTextResId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_channel_message_empty);
            binding.statusFrame.setEmptyText(emptyTextResId);
        }

        if (adapter == null) adapter = new MessageSearchAdapter();
        if (loadingDialogHandler == null) loadingDialogHandler = this;

        adapter.setOnItemClickListener(itemClickListener != null ? itemClickListener : this::onItemClicked);

        binding.searchResultList.setAdapter(adapter);
        binding.searchResultList.setHasFixedSize(true);
        binding.searchResultList.setPager(viewModel);
        binding.searchResultList.setThreshold(5);
        binding.searchResultList.setUseDivider(false);

        viewModel.getSearchResultList().observe(this, searchResults -> {
            Logger.dev("++ search result size : %s", searchResults.size());
            onSearchResultReceived(searchResults);
        });

        SoftInputUtils.showSoftKeyboard(binding.searchBar.getBinding().etInputText);
    }

    private void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    /**
     * It called when the search request occurs with the written keyword.
     *
     * @param keyword The keyword to search of message.
     * @since 2.1.0
     */
    protected void search(@NonNull String keyword) {
        if (viewModel != null) {
            loadingDialogHandler.shouldShowLoadingDialog();
            viewModel.search(keyword, (result, e) -> {
                loadingDialogHandler.shouldDismissLoadingDialog();
                if (e != null) {
                    binding.statusFrame.setVisibility(View.VISIBLE);
                    binding.statusFrame.setStatus(StatusFrameView.Status.ERROR);
                }
            });
        }
    }

    /**
     * It called when the search results exists.
     *
     * @param searchResults The search results.
     * @since 2.1.0
     */
    protected void onSearchResultReceived(List<BaseMessage> searchResults) {
        loadingDialogHandler.shouldDismissLoadingDialog();
        binding.statusFrame.setVisibility(View.GONE);
        adapter.setItems(searchResults);

        if (searchResults.isEmpty()) {
            binding.statusFrame.setStatus(StatusFrameView.Status.EMPTY);
            binding.statusFrame.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when a message item has been clicked.
     *
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param message  The user data that was clicked.
     * @since 2.1.0
     */
    protected void onItemClicked(View view, int position, BaseMessage message) {
        Logger.d(">> MessageSearchFragment::onItemClicked(position=%s)", position);
        Intent intent = new ChannelActivity.IntentBuilder(getContext(), channel.getUrl())
                .setStartingPoint(message.getCreatedAt())
                .setHighlightMessageInfo(HighlightMessageInfo.fromMessage(message))
                .build();
        intent.putExtra(StringSet.KEY_FROM_SEARCH_RESULT, false);
        startActivity(intent);
    }

    protected void setErrorFrame() {
        binding.statusFrame.setStatus(StatusFrameView.Status.CONNECTION_ERROR);
        binding.statusFrame.setOnActionEventListener(v -> {
            binding.statusFrame.setStatus(StatusFrameView.Status.LOADING);
            connect();
        });
    }

    private void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    private void setMessageSearchAdapter(MessageSearchAdapter adapter) {
        this.adapter = adapter;
    }

    private void setItemClickListener(OnItemClickListener<BaseMessage> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private void setOnSearchEventListener(OnSearchEventListener rightButtonListener) {
        this.onSearchEventListener = rightButtonListener;
    }

    private void setMessageSearchQuery(MessageSearchQuery customQueryHandler) {
        this.query = customQueryHandler;
    }

    public static class Builder {
        private final Bundle bundle;
        private MessageSearchFragment customFragment;
        private OnSearchEventListener onSearchEventListener;
        private MessageSearchAdapter adapter;
        private OnItemClickListener<BaseMessage> itemClickListener;
        private LoadingDialogHandler loadingDialogHandler;
        private MessageSearchQuery query;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @since 2.1.0
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendBirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode {@link SendBirdUIKit.ThemeMode}
         * @since 2.1.0
         */
        public Builder(@NonNull String channelUrl, SendBirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         * @since 2.1.0
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom message search fragment. It must inherit {@link MessageSearchFragment}.
         * @param fragment custom message search fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 2.1.0
         */
        public <T extends MessageSearchFragment> Builder setCustomMessageSearchFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useSearchBar <code>true</code> if the search bar is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setUseSearchBar(boolean useSearchBar) {
            bundle.putBoolean(StringSet.KEY_USE_SEARCH_BAR, useSearchBar);
            return this;
        }

        /**
         * Sets the text on the search bar's right side.
         *
         * @param text the text which is shown on the search bar's right side.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setSearchBarButtonText(@NonNull String text) {
            bundle.putString(StringSet.KEY_SEARCH_BAR_BUTTON_TEXT, text);
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
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
         * @since 2.1.0
         */
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the search action event listener on the right button of the search bar.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setOnSearchEventListener(OnSearchEventListener listener) {
            this.onSearchEventListener = listener;
            return this;
        }

        /**
         * Sets the message search adapter.
         *
         * @param adapter The adapter for displaying the searched message list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public <T extends MessageSearchAdapter> Builder setMessageSearchAdapter(T adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of the message search result list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setItemClickListener(OnItemClickListener<BaseMessage> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * @since 2.1.0
         */
        public Builder setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Sets the query of custom message search.
         * The channelUrl, the keyword, and the order properties in the MessageSearchQuery are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param query The {@link MessageSearchQuery} instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setMessageSearchQuery(@NonNull MessageSearchQuery query) {
            this.query = query;
            return this;
        }

        /**
         * Creates an {@link MessageSearchFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link MessageSearchFragment} applied to the {@link Bundle}.
         */
        public MessageSearchFragment build() {
            MessageSearchFragment fragment = customFragment != null ? customFragment : new MessageSearchFragment();
            fragment.setArguments(bundle);
            fragment.setOnSearchEventListener(onSearchEventListener);
            fragment.setItemClickListener(itemClickListener);
            fragment.setMessageSearchAdapter(adapter);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            fragment.setMessageSearchQuery(query);
            return fragment;
        }
    }
}
