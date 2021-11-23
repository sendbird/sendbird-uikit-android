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
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.CustomMemberListQueryHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class UserTypeListViewModel extends BaseViewModel implements LifecycleObserver, PagerRecyclerView.Pageable<List<User>> {
    private final String CONNECTION_HANDLER_ID = getClass().getName() + System.currentTimeMillis();
    private final String CHANNEL_HANDLER_MEMBER_LIST = "CHANNEL_HANDLER_MEMBER_LIST" + System.currentTimeMillis();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    private final MutableLiveData<List<User>> memberList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operatorDismissed = new MutableLiveData<>();
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    private final CustomMemberListQueryHandler<User> queryHandler;
    protected BaseChannel channel;
    private volatile boolean isInitialRequest = true;

    @Override
    protected void onCleared() {
        super.onCleared();
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
    }

    private void onResult(List<User> memberList, Exception e) {
        if (e != null) {
            Logger.e(e);
            if (isInitialRequest) {
                SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
                    @Override
                    public void onReconnectStarted() {
                    }

                    @Override
                    public void onReconnectSucceeded() {
                        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
                        loadInitial();
                    }

                    @Override
                    public void onReconnectFailed() {
                        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
                        changeAlertStatus(StatusFrameView.Status.ERROR);
                        notifyDataSetChanged(UserTypeListViewModel.this.memberList.getValue());
                    }
                });
                return;
            }
            changeAlertStatus(StatusFrameView.Status.ERROR);
            notifyDataSetChanged(this.memberList.getValue());
        } else {
            List<User> newUsers = new ArrayList<>(memberList);
            List<User> origin = this.memberList.getValue();
            if (origin != null) {
                newUsers.addAll(0, origin);
            }
            applyUserList(newUsers);
        }
        isInitialRequest = false;
    }

    UserTypeListViewModel(BaseChannel channel, CustomMemberListQueryHandler<User> customQuery) {
        super();
        this.channel = channel;
        this.queryHandler = customQuery;
        //queryHandler = createQueryHandler(channel, type);
    }

    public LiveData<Boolean> getOperatorDismissed() {
        return operatorDismissed;
    }

    public LiveData<Boolean> getChannelDeleted() {
        return channelDeleted;
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channelUrl.equals(channel.getUrl());
    }

    private void updateChannel(@NonNull BaseChannel channel) {
        if (isCurrentChannel(channel.getUrl())) {
            Logger.i(">> UserTypeListViewModel::updateChannel()");
            UserTypeListViewModel.this.channel = channel;
            loadInitial();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void onCreate() {
        Logger.i(">> UserTypeListViewModel::onCreate()");
        SendBird.addChannelHandler(CHANNEL_HANDLER_MEMBER_LIST, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel channel, BaseMessage message) {
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> UserTypeListViewModel::onUserLeft()");
                    if (channel.getMyMemberState() == Member.MemberState.NONE) {
                        channelDeleted.postValue(true);
                    }
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> UserTypeListViewModel::onChannelDeleted()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                updateChannel(channel);
                if (isCurrentChannel(channel.getUrl()) &&
                        ((GroupChannel) channel).getMyRole() != Member.Role.OPERATOR) {
                    Logger.i(">> UserTypeListViewModel::onOperatorUpdated()");
                    Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                    operatorDismissed.postValue(true);
                }
            }

            @Override
            public void onUserMuted(BaseChannel channel, User user) {
                updateChannel(channel);
            }

            @Override
            public void onUserUnmuted(BaseChannel channel, User user) {
                updateChannel(channel);
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                updateChannel(channel);
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> UserTypeListViewModel::onUserBanned()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onUserUnbanned(BaseChannel channel, User user) {
                updateChannel(channel);
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                updateChannel(channel);
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy() {
        Logger.i(">> MemberListViewModel::onDestroy()");
        SendBird.removeChannelHandler(CHANNEL_HANDLER_MEMBER_LIST);
    }

    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    protected void changeAlertStatus(StatusFrameView.Status status) {
        if (!hasData() || status == StatusFrameView.Status.NONE) {
            statusFrame.postValue(status);
        }
    }

    protected boolean hasData() {
        List<? extends User> origin = memberList.getValue();
        return origin != null && origin.size() > 0;
    }

    public LiveData<? extends Collection<?>> getMemberList() {
        return memberList;
    }

    private void applyUserList(List<User> newUserList) {
        changeAlertStatus(newUserList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        notifyDataSetChanged(newUserList);
    }

    @SuppressWarnings("unchecked")
    protected void notifyDataSetChanged(Collection<User> list) {
        memberList.postValue(list == null ? new ArrayList<>() : (List<User>)list);
    }

    @Override
    public boolean hasNext() {
        return queryHandler.hasMore();
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    public void loadInitial() {
        Logger.d(">> MemberListViewModel::loadInitial()");
        List<? extends User> origin = this.memberList.getValue();
        if (origin != null) {
            origin.clear();
        }
        queryHandler.loadInitial(UserTypeListViewModel.this::onResult);
    }

    @Override
    public List<User> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public List<User> loadNext() throws InterruptedException {
        if (hasNext()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<List<User>> result = new AtomicReference<>();
            final AtomicReference<Exception> error = new AtomicReference<>();
            try {
                queryHandler.load((userList, e) -> {
                    try {
                        if (e != null) {
                            error.set(e);
                            return;
                        }
                        result.set(userList);
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await();
            } catch (Exception e) {
                error.set(e);
                throw e;
            } finally {
                onResult(result.get(), error.get());
            }
            return result.get();
        }
        return Collections.emptyList();
    }
}
