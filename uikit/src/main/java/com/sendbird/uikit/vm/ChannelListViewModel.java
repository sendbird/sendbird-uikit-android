package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelCollection;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.handlers.GroupChannelCollectionHandler;
import com.sendbird.android.handlers.GroupChannelContext;
import com.sendbird.uikit.R;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.tasks.JobTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ChannelListViewModel extends BaseViewModel implements PagerRecyclerView.Pageable<List<GroupChannel>>,
        GroupChannelCollectionHandler {

    @Nullable
    private GroupChannelCollection collection;
    @NonNull
    private final MutableLiveData<List<GroupChannel>> channelList = new MutableLiveData<>();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();

    private synchronized void initChannelCollection(@NonNull GroupChannelListQuery query) {
        Logger.d(">> ChannelListViewModel::initChannelCollection()");
        if (this.collection != null) {
            disposeChannelCollection();
        }
        this.collection = new GroupChannelCollection.Builder(query).build();
        this.collection.setGroupChannelCollectionHandler(this);
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
        changeAlertStatusIfEmpty(newList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        channelList.postValue(newList);
    }

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

    @Override
    protected void onCleared() {
        super.onCleared();
        disposeChannelCollection();
    }

    @NonNull
    public LiveData<List<GroupChannel>> getChannelList() {
        return channelList;
    }

    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    @Override
    public boolean hasNext() {
        if (collection == null) return false;
        return collection.hasMore();
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    public void loadInitial(@NonNull final GroupChannelListQuery query) {
        initChannelCollection(query);
        TaskQueue.addTask(new JobTask<List<GroupChannel>>() {
            @Override
            protected List<GroupChannel> call() throws Exception {
                return loadMore();
            }
        });
    }

    @Override
    public List<GroupChannel> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public List<GroupChannel> loadNext() throws Exception {
        return loadMore();
    }

    private List<GroupChannel> loadMore() throws Exception {
        if (!hasNext()) return Collections.emptyList();

        try {
            List<GroupChannel> channels = loadMoreBlocking();
            notifyChannelChanged();
            return channels;
        } catch (Exception ee) {
            handleError(ee);
            throw ee;
        }
    }

    private List<GroupChannel> loadMoreBlocking() throws Exception {
        if (!hasNext() || collection == null) return Collections.emptyList();
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<SendBirdException> error = new AtomicReference<>();
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

    private boolean hasData() {
        if (collection == null) return false;
        return collection.getChannelList().size() > 0;
    }

    private void handleError(@NonNull Exception e) {
        Logger.e(e);
        boolean hasData = hasData();
        if (!hasData) {
            changeAlertStatusIfEmpty(StatusFrameView.Status.ERROR);
        } else {
            notifyChannelChanged();
        }
    }

    private void changeAlertStatusIfEmpty(StatusFrameView.Status status) {
        if (!hasData() || status == StatusFrameView.Status.NONE) {
            statusFrame.postValue(status);
        }
    }

    public void setPushNotification(@NonNull GroupChannel channel, boolean enable) {
        channel.setMyPushTriggerOption(enable ? GroupChannel.PushTriggerOption.ALL : GroupChannel.PushTriggerOption.OFF, e -> Logger.i("++ setPushNotification enable : %s result : %s",enable, e == null ? "success" : "error"));
    }

    public void leaveChannel(@NonNull final GroupChannel channel) {
        channel.leave(e -> {
            if (e != null) errorToast.postValue(R.string.sb_text_error_leave_channel);
        });
    }
}
