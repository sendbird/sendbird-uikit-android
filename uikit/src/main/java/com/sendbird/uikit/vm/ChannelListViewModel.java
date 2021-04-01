package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelChangeLogsParams;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.tasks.JobTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelListViewModel extends BaseViewModel implements PagerRecyclerView.Pageable<List<GroupChannel>>, LifecycleObserver {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private final AtomicLong lastSyncTs = new AtomicLong(0);
    private final MutableLiveData<List<GroupChannel>> channelList = new MutableLiveData<>();
    private final GroupChannelListQuery channelListQuery;
    private final GroupChannelChangeLogsParams changeLogsParams;
    private final Set<GroupChannel> channelListCache = new HashSet<>();
    private final AtomicBoolean hasMore = new AtomicBoolean();

    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();

    private final Comparator<GroupChannel> comparator = new Comparator<GroupChannel>() {
        @Override
        public int compare(GroupChannel groupChannel1, GroupChannel groupChannel2) {
            return GroupChannel.compareTo(groupChannel1, groupChannel2, channelListQuery.getOrder());
        }
    };

    ChannelListViewModel(@NonNull GroupChannelListQuery customQuery) {
        super();
        this.channelListQuery = customQuery;
        this.changeLogsParams = GroupChannelChangeLogsParams.from(channelListQuery);
        Logger.d("++ limit =%s, isIncludeEmpty=%s", channelListQuery.getLimit(), channelListQuery.isIncludeEmpty());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void onResume(){
        Logger.dev(">> ChannelListViewModel::onResume()");

        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {}

            @Override
            public void onReconnectSucceeded() {
                requestChangeLogs();
            }

            @Override
            public void onReconnectFailed() {}
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel channel, BaseMessage message) {
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                Logger.i(">> ChannelListFragment::onChannelChanged()");
                updateOrInsert((GroupChannel) channel);
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                Logger.i(">> ChannelListFragment::onUserLeft()");
                Logger.d("++ user : %s", user);
                if (channel.getMyMemberState() == Member.MemberState.NONE) {
                    deleteChannel(channel);
                    return;
                }
                if (channelListQuery.isIncludeEmpty()) {
                    updateOrInsert(channel);
                } else {
                    updateIfExist(channel);
                }
            }

            @Override
            public void onUserJoined(GroupChannel channel, User user) {
                Logger.i(">> ChannelListFragment::onUserLeft()");
                Logger.d("++ user : %s", user);
                if (channelListQuery.isIncludeEmpty()) {
                    updateOrInsert(channel);
                } else {
                    updateIfExist(channel);
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                Logger.i(">> ChannelListFragment::onChannelDeleted()");
                Logger.d("++ deleted channelUrl : %s", channelUrl);
                if (channelType == BaseChannel.ChannelType.GROUP) {
                    GroupChannel.getChannel(channelUrl, (channel, e) -> {
                        if (e == null || channel != null) {
                            deleteChannel(channel);
                        }
                    });
                }
            }

            @Override
            public void onChannelFrozen(BaseChannel channel) {
                updateOrInsert((GroupChannel) channel);
            }

            @Override
            public void onChannelUnfrozen(BaseChannel channel) {
                updateOrInsert((GroupChannel) channel);
            }
        });

        requestChangeLogs();
        markChannelSyncTs();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void onPause() {
        Logger.dev(">> ChannelListViewModel::onPause()");
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    public LiveData<List<GroupChannel>> getChannelList() {
        return channelList;
    }

    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    private void markChannelSyncTs() {
        this.lastSyncTs.set(System.currentTimeMillis() -  60000);
    }

    private void updateIfExist(@NonNull GroupChannel channel) {
        Logger.dev(">> updateIfExist()");
        synchronized (channelListCache) {
            if (channelListCache.contains(channel)) {
                channelListCache.remove(channel);
                channelListCache.add(channel);
                applyChannelList();
            }
        }
    }

    private void updateOrInsert(@NonNull GroupChannel channel) {
        Logger.dev(">> updateOrInsert()");
        synchronized (channelListCache) {
            channelListCache.remove(channel);
            channelListCache.add(channel);
        }
        applyChannelList();
    }

    private boolean deleteChannel(@NonNull GroupChannel deletedChannel) {
        boolean deleted;
        synchronized (channelListCache) {
            deleted = channelListCache.remove(deletedChannel);
        }

        if (deleted) {
            applyChannelList();
        }
        return deleted;
    }

    private void requestChangeLogs() {
        Logger.dev(">> ChannelListViewModel::requestChangeLogs(%s)", lastSyncTs.get());
        long lastTs = lastSyncTs.get();
        if (lastTs > 0) {
            ChannelChangeLogsPager pager = new ChannelChangeLogsPager(lastTs, changeLogsParams);
            pager.load(new ChannelChangeLogsPager.ChannelChangeLogsResultHandler() {
                @Override
                public void onError(SendBirdException e) {
                    Logger.e(e);
                }

                @Override
                public void onResult(List<GroupChannel> updatedChannels, List<String> deletedChannelUrls) {
                    Logger.i("[changeLogs] updatedChannels size : %s, deletedChannelUrls size : %s", updatedChannels.size(), deletedChannelUrls.size());
                    synchronized (channelListCache) {
                        channelListCache.removeAll(updatedChannels);
                        channelListCache.addAll(updatedChannels);
                        List<GroupChannel> willDeleteChannel = new ArrayList<>();
                        for (GroupChannel channel : channelListCache) {
                            if (deletedChannelUrls.contains(channel.getUrl())) {
                                willDeleteChannel.add(channel);
                            }
                        }
                        channelListCache.removeAll(willDeleteChannel);
                    }
                    markChannelSyncTs();
                    applyChannelList();
                }
            });
        }
    }

    public void loadInitial() {
        TaskQueue.addTask(new JobTask<List<GroupChannel>>() {
            @Override
            protected List<GroupChannel> call() throws Exception {
                return next();
            }
        });
    }

    @Override
    public List<GroupChannel> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public List<GroupChannel> loadNext() throws Exception {
        if (hasMore.get()) {
            return next();
        }
        return Collections.emptyList();
    }

    private List<GroupChannel> next() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<GroupChannel>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();

        try {
            channelListQuery.next((list, e) -> {
                try {
                    Logger.i("____________ channelListQuery requestd result=%s", list != null ? list.size() : 0);
                    result.set(list);
                    error.set(e);
                } finally {
                    latch.countDown();
                }
            });
            latch.await();
        } catch (Exception e) {
            error.set(e);
        }
        onResult(result.get(), error.get());

        if (error.get() != null) throw error.get();
        return result.get();
    }

    private void onResult(List<GroupChannel> list, Exception e) {
        boolean hasData = channelListCache.size() > 0;
        if (e != null) {
            Logger.e(e);
            if (!hasData) {
                changeAlertStatusIfEmpty(StatusFrameView.Status.ERROR);
            }
            notifyDataSetChanged(channelList.getValue());
            return;
        }
        Logger.dev("++ list : %s", list);
        this.hasMore.set(!list.isEmpty());
        synchronized (channelListCache) {
            channelListCache.addAll(list);
        }
        applyChannelList();
    }

    private void changeAlertStatusIfEmpty(StatusFrameView.Status status) {
        if (channelListCache.size() <= 0 || status == StatusFrameView.Status.NONE) {
            statusFrame.postValue(status);
        }
    }

    private void applyChannelList() {
        List<GroupChannel> newList = new ArrayList<>(channelListCache);
        Collections.sort(newList, comparator);

        changeAlertStatusIfEmpty(newList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        notifyDataSetChanged(newList);
    }

    private void notifyDataSetChanged(List<GroupChannel> newList) {
        channelList.postValue(newList == null ? new ArrayList<>() : newList);
    }

    public void setPushNotification(@NonNull GroupChannel channel, boolean enable) {
        channel.setMyPushTriggerOption(enable ? GroupChannel.PushTriggerOption.ALL : GroupChannel.PushTriggerOption.OFF, new GroupChannel.GroupChannelSetMyPushTriggerOptionHandler() {
            @Override
            public void onResult(SendBirdException e) {
                Logger.i("++ setPushNotification enable : %s result : %s",enable, e == null ? "success" : "error");
            }
        });
    }

    public void leaveChannel(@NonNull final GroupChannel channel) {
        channel.leave(e -> {
            if (e != null) errorToast.postValue(R.string.sb_text_error_leave_channel);
            if (e == null) {
                deleteChannel(channel);
                Logger.i("++ channel [%s] was left.", channel.getUrl());
            }
        });
    }
}
