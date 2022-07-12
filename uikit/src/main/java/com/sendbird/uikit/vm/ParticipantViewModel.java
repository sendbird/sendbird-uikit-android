package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.handler.ConnectionHandler;
import com.sendbird.android.handler.OpenChannelHandler;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.User;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.vm.queries.ParticipantsListQuery;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ViewModel preparing and managing data related with the list of participants
 *
 * @since 3.0.0
 */
public class ParticipantViewModel extends BaseViewModel implements OnPagedDataLoader<List<User>> {
    @NonNull
    private final String CONNECTION_HANDLER_ID = getClass().getName() + System.currentTimeMillis();
    @NonNull
    private final String CHANNEL_HANDLER_MEMBER_LIST = "CHANNEL_HANDLER_MEMBER_LIST" + System.currentTimeMillis();
    @NonNull
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<List<User>> userList = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> channelDeleted = new MutableLiveData<>();
    @NonNull
    private final String channelUrl;

    @NonNull
    private final PagedQueryHandler<User> queryHandler;
    @Nullable
    private OpenChannel channel;
    private volatile boolean isInitialRequest = false;

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A query to retrieve {@link User} list for the current <code>OpenChannel</code>
     * @since 3.0.0
     */
    public ParticipantViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<User> queryHandler) {
        super();
        this.channelUrl = channelUrl;
        this.queryHandler = queryHandler == null ? createQueryHandler(channelUrl) : queryHandler;
        SendbirdChat.addChannelHandler(CHANNEL_HANDLER_MEMBER_LIST, new OpenChannelHandler() {
            @Override
            public void onMessageReceived(@NonNull BaseChannel baseChannel, @NonNull BaseMessage baseMessage) {
            }

            @Override
            public void onChannelDeleted(@NonNull String channelUrl, @NonNull ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> UserViewModel::onChannelDeleted()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onUserBanned(@NonNull BaseChannel channel, @NonNull RestrictedUser user) {
                updateChannel(channel);
                final User currentUser = SendbirdChat.getCurrentUser();
                if (isCurrentChannel(channel.getUrl()) && currentUser != null &&
                        user.getUserId().equals(currentUser.getUserId())) {
                    Logger.i(">> UserViewModel::onUserBanned()");
                    channelDeleted.postValue(true);
                }
            }

            @Override
            public void onChannelChanged(@NonNull BaseChannel channel) {
                updateChannel(channel);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        SendbirdChat.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendbirdChat.removeChannelHandler(CHANNEL_HANDLER_MEMBER_LIST);
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channel != null && channelUrl.equals(channel.getUrl());
    }

    private void onResult(@NonNull List<User> memberList, @Nullable Exception e) {
        if (e != null) {
            Logger.e(e);
            if (isInitialRequest) {
                SendbirdChat.addConnectionHandler(CONNECTION_HANDLER_ID, new ConnectionHandler() {
                    @Override
                    public void onConnected(@NonNull String s) {

                    }

                    @Override
                    public void onDisconnected(@NonNull String s) {

                    }

                    @Override
                    public void onReconnectStarted() {

                    }

                    @Override
                    public void onReconnectSucceeded() {
                        SendbirdChat.removeConnectionHandler(CONNECTION_HANDLER_ID);
                        loadInitial();
                    }

                    @Override
                    public void onReconnectFailed() {

                    }
                });
                return;
            }
            changeAlertStatus(StatusFrameView.Status.ERROR);
            notifyDataSetChanged(this.userList.getValue());
        } else {
            final List<User> newUsers = new ArrayList<>(memberList);
            final List<User> origin = this.userList.getValue();
            if (origin != null) {
                newUsers.addAll(0, origin);
            }
            applyUserList(newUsers);
        }
        isInitialRequest = false;
    }

    /**
     * Returns {@code OpenChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code OpenChannel} this view model is currently associated with
     * @since 3.0.0
     */
    @Nullable
    public OpenChannel getChannel() {
        return channel;
    }

    /**
     * Returns URL of GroupChannel.
     *
     * @return The URL of a channel this view model is currently associated with
     * @since 3.0.0
     */
    @NonNull
    public String getChannelUrl() {
        return channelUrl;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding whether {@code OpenChannel} has been deleted
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getChannelDeleted() {
        return channelDeleted;
    }

    private void updateChannel(@NonNull BaseChannel channel) {
        if (isCurrentChannel(channel.getUrl())) {
            Logger.i(">> UserViewModel::updateChannel()");
            loadInitial();
        }
    }

    /**
     * Returns LiveData that can be observed for the status of the result of fetching the user list.
     * When the user list is fetched successfully, the status is {@link StatusFrameView.Status#NONE}.
     *
     * @return The Status for the user list
     * @since 3.0.0
     */
    @NonNull
    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    private void changeAlertStatus(@NonNull StatusFrameView.Status status) {
        if (!hasData() || status == StatusFrameView.Status.NONE) {
            statusFrame.postValue(status);
        }
    }

    private boolean hasData() {
        List<User> origin = userList.getValue();
        return origin != null && origin.size() > 0;
    }

    /**
     * Returns LiveData that can be observed for the list of participants.
     *
     * @return LiveData holding the latest list of participants
     * @since 3.0.0
     */
    @NonNull
    public LiveData<List<User>> getUserList() {
        return userList;
    }

    private void applyUserList(@NonNull List<User> newUserList) {
        changeAlertStatus(newUserList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        notifyDataSetChanged(newUserList);
    }

    private void notifyDataSetChanged(@Nullable List<User> list) {
        userList.postValue(list == null ? new ArrayList<>() : list);
    }

    @Override
    public boolean hasNext() {
        return queryHandler.hasMore();
    }

    /**
     * Returns {@code false} as the participant list do not support to load for the previous by default.
     *
     * @return Always {@code false}
     * @since 3.0.0
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     * Requests the list of <code>User</code>s for the first time.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getUserList()}.
     *
     * @since 3.0.0
     */
    public synchronized void loadInitial() {
        Logger.d(">> MemberListViewModel::loadInitial()");
        List<User> origin = this.userList.getValue();
        if (origin != null) {
            origin.clear();
        }
        this.isInitialRequest = true;
        queryHandler.loadInitial(ParticipantViewModel.this::onResult);
    }

    /**
     * Requests the list of <code>User</code>s.
     * If there is no more pages to be read, an empty <code>List</code> (not <code>null</code>) returns.
     * If the request is succeed, you can observe updated data through {@link #getUserList()}.
     *
     * @return Returns the queried list of <code>User</code>s if no error occurs
     * @throws Exception Throws exception if getting the user list are failed
     * @since 3.0.0
     */
    @NonNull
    @Override
    public List<User> loadNext() throws Exception {
        if (hasNext()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<List<User>> result = new AtomicReference<>();
            final AtomicReference<Exception> error = new AtomicReference<>();
            try {
                queryHandler.loadMore((userList, e) -> {
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

    /**
     * Returns the empty list as the participant list do not support to load for the previous by default.
     *
     * @return The empty list
     * @since 3.0.0
     */
    @NonNull
    @Override
    public List<User> loadPrevious() {
        return Collections.emptyList();
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * @since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                OpenChannel.getChannel(channelUrl, (channel, e1) -> {
                    ParticipantViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        handler.onAuthenticated();
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Creates participants user list query.
     *
     * @param channelUrl The url of {@code OpenChannel} with participants to be fetched by the query
     * @return {@code PagedQueryHandler<User>} to retrieve the list of operators
     * @since 3.0.0
     */
    @NonNull
    protected PagedQueryHandler<User> createQueryHandler(@NonNull String channelUrl) {
        return new ParticipantsListQuery(channelUrl);
    }
}
