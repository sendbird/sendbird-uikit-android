package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.query.GroupChannelListQuery;
import com.sendbird.android.collection.GroupChannelContext;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.GroupChannelCollectionHandler;
import com.sendbird.android.params.GroupChannelListQueryParams;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.interfaces.OnCompleteHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.internal.tasks.JobTask;
import com.sendbird.uikit.internal.wrappers.TaskQueueImpl;
import com.sendbird.uikit.internal.wrappers.TaskQueueWrapper;
import com.sendbird.uikit.internal.wrappers.GroupChannelCollectionImpl;
import com.sendbird.uikit.internal.wrappers.GroupChannelCollectionWrapper;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitImpl;
import com.sendbird.uikit.internal.wrappers.SendbirdUIKitWrapper;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.Available;

import org.jetbrains.annotations.TestOnly;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of channels
 *
 * since 3.0.0
 */
public class ChannelListViewModel extends BaseViewModel implements OnPagedDataLoader<List<GroupChannel>> {

    @Nullable
    private GroupChannelCollectionWrapper collection;

    @NonNull
    private final GroupChannelListQuery query;
    @NonNull
    private final MutableLiveData<List<GroupChannel>> channelList = new MutableLiveData<>();

    @NonNull
    private final GroupChannelCollectionHandler collectionHandler = new GroupChannelCollectionHandler() {
        @Override
        public void onChannelsAdded(@NonNull GroupChannelContext context, @NonNull List<GroupChannel> channels) {
            notifyChannelChanged();
        }

        @Override
        public void onChannelsUpdated(@NonNull GroupChannelContext context, @NonNull List<GroupChannel> channels) {
            notifyChannelChanged();
        }

        @Override
        public void onChannelsDeleted(@NonNull GroupChannelContext context, @NonNull List<String> deletedChannelUrls) {
            notifyChannelChanged();
        }
    };
    @NonNull
    private final TaskQueueWrapper taskQueue;

    /**
     * Constructor
     *
     * @param query A query to retrieve {@code GroupChannel} list for the current user
     */
    public ChannelListViewModel(@Nullable GroupChannelListQuery query) {
        this(query, new SendbirdUIKitImpl(), new TaskQueueImpl());
    }

    @VisibleForTesting
    ChannelListViewModel(@Nullable GroupChannelListQuery query, @NonNull SendbirdUIKitWrapper sendbirdUIKit, @NonNull TaskQueueWrapper taskQueue) {
        super(sendbirdUIKit);
        this.query = query == null ? createGroupChannelListQuery() : query;
        this.taskQueue = taskQueue;
    }

    /**
     * Live data that can be observed for a list of channels.
     *
     * @return LiveData holding the list of {@code GroupChannel} for the current user
     * since 3.0.0
     */
    @NonNull
    public LiveData<List<GroupChannel>> getChannelList() {
        return channelList;
    }

    private synchronized void initChannelCollection() {
        Logger.d(">> ChannelListViewModel::initChannelCollection()");
        if (this.collection != null) {
            disposeChannelCollection();
        }
        this.collection = createGroupChannelCollection();
        this.collection.setGroupChannelCollectionHandler(collectionHandler);
    }

    private synchronized void disposeChannelCollection() {
        Logger.d(">> ChannelListViewModel::disposeChannelCollection()");
        if (this.collection != null) {
            this.collection.setGroupChannelCollectionHandler(null);
            this.collection.dispose();
        }
    }

    private void notifyChannelChanged() {
        if (collection == null) return;
        List<GroupChannel> newList = collection.getChannelList();
        Logger.d(">> ChannelListViewModel::notifyDataSetChanged(), size = %s", newList.size());
        channelList.postValue(newList);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposeChannelCollection();
    }

    /**
     * Returns {@code false} as the channel list do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * since 3.0.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Returns the empty list as the channel list do not support to load for the previous by default.
     *
     * @return The empty list
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<GroupChannel> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasNext() {
        return collection != null && collection.getHasMore();
    }

    /**
     * Requests the list of <code>GroupChannel</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     *
     * since 3.0.0
     */
    public void loadInitial() {
        initChannelCollection();
        taskQueue.addTask(new JobTask<List<GroupChannel>>() {
            @Override
            protected List<GroupChannel> call() throws Exception {
                return loadNext();
            }
        });
    }

    /**
     * Requests the list of <code>GroupChannel</code>s.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getChannelList()}.
     *
     * @return Returns the queried list of <code>GroupChannel</code>s if no error occurs
     * @throws Exception Throws exception if getting the channel list are failed
     * since 3.0.0
     */
    @NonNull
    @Override
    public List<GroupChannel> loadNext() throws Exception {
        if (!hasNext()) return Collections.emptyList();

        try {
            return loadMoreBlocking();
        } finally {
            notifyChannelChanged();
        }
    }

    @NonNull
    private List<GroupChannel> loadMoreBlocking() throws Exception {
        if (collection == null) return Collections.emptyList();

        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<SendbirdException> error = new AtomicReference<>();
        final AtomicReference<List<GroupChannel>> channelListRef = new AtomicReference<>();
        collection.loadMore((channelList, e) -> {
            channelListRef.set(channelList);
            error.set(e);
            lock.countDown();
        });
        lock.await();

        if (error.get() != null) throw error.get();
        return channelListRef.get();
    }

    /**
     * Sets push notification settings of this channel.
     *
     * @param channel Target GroupChannel
     * @param enable  Whether the push notification turns on
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void setPushNotification(@NonNull GroupChannel channel, boolean enable, @Nullable OnCompleteHandler handler) {
        channel.setMyPushTriggerOption(enable ? GroupChannel.PushTriggerOption.ALL :
                        GroupChannel.PushTriggerOption.OFF,
                e -> {
                    if (handler != null) handler.onComplete(e);
                    Logger.i("++ setPushNotification enable : %s result : %s", enable, e == null ? "success" : "error");
                });
    }

    /**
     * Leaves the targeted channel.
     *
     * @param channel Target GroupChannel
     * @param handler Callback handler called when this method is completed
     * since 3.0.0
     */
    public void leaveChannel(@NonNull final GroupChannel channel, @Nullable OnCompleteHandler handler) {
        channel.leave(false, e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ leave channel");
        });
    }

    /**
     * Tries to connect Sendbird Server.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                handler.onAuthenticated();
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Creates group channel list query.
     *
     * @return {@code GroupChannelListQuery} to retrieve the list of channels
     * since 3.0.0
     */
    @NonNull
    protected GroupChannelListQuery createGroupChannelListQuery() {
        final GroupChannelListQueryParams params = new GroupChannelListQueryParams();
        params.setIncludeChatNotification(Available.isSupportChatNotification());
        return GroupChannel.createMyGroupChannelListQuery(params);
    }

    @VisibleForTesting
    @NonNull
    GroupChannelCollectionWrapper createGroupChannelCollection() {
        return new GroupChannelCollectionImpl(query);
    }

    @TestOnly
    @NonNull
    GroupChannelListQuery getGroupChannelListQuery() {
        return query;
    }
}
