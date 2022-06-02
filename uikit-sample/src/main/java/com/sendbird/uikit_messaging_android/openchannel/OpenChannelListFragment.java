package com.sendbird.uikit_messaging_android.openchannel;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.widgets.StatusFrameView;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.databinding.FragmentOpenChannelListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Displays an open channel list screen.
 */
abstract public class OpenChannelListFragment extends Fragment implements SendBird.ConnectionHandler {
    private final String CONNECTION_HANDLER_ID = getClass().getName() + System.currentTimeMillis();

    private FragmentOpenChannelListBinding binding;

    @NonNull
    private final StatusComponent statusComponent = new StatusComponent();
    private OpenChannelListQuery openChannelListQuery;
    private String customTypeFilter;
    private final OpenChannelListAdapter<? extends OpenChannelListViewHolder> adapter;
    private final Set<OpenChannel> channelListCache = new HashSet<>();
    private final AtomicBoolean hasMore = new AtomicBoolean();
    private final Comparator<OpenChannel> comparator = (openChannel1, openChannel2) -> {
        int result = Long.compare(openChannel2.getCreatedAt(), openChannel1.getCreatedAt());
        if (result == 0) {
            result = openChannel1.getUrl().compareTo(openChannel2.getUrl());
        }
        return result;
    };
    private final AtomicBoolean refreshing = new AtomicBoolean();

    public OpenChannelListFragment(@NonNull OpenChannelListAdapter<? extends OpenChannelListViewHolder> adapter) {
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOpenChannelListBinding.inflate(inflater, container, false);
        statusComponent.getParams().setErrorText(getString(R.string.sb_text_error_get_channel_list));
        statusComponent.getParams().setEmptyText(getString(R.string.sb_text_channel_list_empty));
        final int statusStyle = SendbirdUIKit.isDarkMode() ? R.style.Component_Dark_Status : R.style.Component_Status;
        final Context statusThemeContext = new ContextThemeWrapper(requireContext(), statusStyle);
        final View status = statusComponent.onCreateView(statusThemeContext, inflater, binding.statusComponent, savedInstanceState);
        binding.statusComponent.addView(status);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnItemClickListener<OpenChannel> itemClickListener = this::onItemClick;

        adapter.setOnItemClickListener(itemClickListener);
        binding.rvOpenChannelList.setAdapter(adapter);
        binding.rvOpenChannelList.setHasFixedSize(true);
        binding.rvOpenChannelList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int threshold = 1;
                if (layoutManager == null) return;
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int lastItemPosition = channelListCache.size();

                if (!refreshing.get() && lastItemPosition - lastVisibleItemPosition <= threshold && hasMore.get()) {
                    refreshing.set(true);
                    next(false);
                }
            }
        });
        statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInitial();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
    }

    @Override
    public void onReconnectStarted() {}

    @Override
    public void onReconnectSucceeded() {
        if (!isActive()) return;
        loadInitial();
    }

    @Override
    public void onReconnectFailed() {}

    private boolean isActive() {
        boolean isDeactivated = isRemoving() || isDetached() || getContext() == null;
        return !isDeactivated;
    }

    private void loadInitial() {
        openChannelListQuery = OpenChannel.createOpenChannelListQuery();
        next(true);
    }

    private void next(boolean isInitialLoading) {
        if (openChannelListQuery == null) return;

        if (customTypeFilter != null) {
            openChannelListQuery.setCustomTypeFilter(customTypeFilter);
        }

        if (!openChannelListQuery.isLoading()) {
            openChannelListQuery.next((list, e) -> {
                refreshing.set(false);
                final boolean hasData = channelListCache.size() > 0;

                if (e != null) {
                    Logger.e(e);
                    if (!hasData) {
                        drawError();
                    }
                    return;
                }
                hasMore.set(!list.isEmpty());
                synchronized (channelListCache) {
                    if (isInitialLoading) {
                        channelListCache.clear();
                        binding.rvOpenChannelList.scrollToPosition(0);
                    }
                    channelListCache.addAll(list);
                }
                applyChannelList();
            });
        }
    }

    private void drawError() {
        if (SendBird.getCurrentUser() == null) {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.CONNECTION_ERROR);
            statusComponent.setOnActionButtonClickListener(v -> connectAndNext());
        } else {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.ERROR);
        }
    }

    private void connectAndNext() {
        statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
        SendbirdUIKit.connect((user, e) -> {
            if (e != null) {
                Logger.e(e);
                drawError();
                return;
            }
            loadInitial();
        });
    }

    private void applyChannelList() {
        List<OpenChannel> newList = new ArrayList<>(channelListCache);
        Collections.sort(newList, comparator);

        statusComponent.notifyStatusChanged(newList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        notifyDataSetChanged(newList);
    }

    private void notifyDataSetChanged(List<OpenChannel> newList) {
        adapter.setItems(newList == null ? new ArrayList<>() : newList);
    }

    /**
     * Sets the custom type filter to be applied to <code>OpenChannelListQuery</code>.
     *
     * @param customTypeFilter Filter to discriminate <code>OpenChannel</code> by type
     */
    public void setCustomTypeFilter(@NonNull String customTypeFilter) {
        this.customTypeFilter = customTypeFilter;
    }

    abstract protected void clickOpenChannelItem(@Nullable OpenChannel openChannel);

    private void onItemClick(View viewHolder, int position, OpenChannel channel) {
        clickOpenChannelItem(channel);
    }
}
