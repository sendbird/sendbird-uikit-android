package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.interfaces.UserListResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.UserUtils;
import com.sendbird.uikit.widgets.PagerRecyclerView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SelectableUserInfoListViewModel extends BaseViewModel implements PagerRecyclerView.Pageable<List<? extends UserInfo>>, UserListResultHandler {
    private final String CONNECTION_HANDLER_ID = getClass().getName() + System.currentTimeMillis();
    private final static int USER_LIST_LIMIT = 15;
    private final ApplicationUserListQuery userListQuery;
    private final MutableLiveData<List<UserInfo>> userList = new MutableLiveData<>();
    private final MutableLiveData<StatusFrameView.Status> statusFrame = new MutableLiveData<>();
    private final CustomUserListQueryHandler customUserListQueryHandler;
    private volatile boolean isInitialRequest = true;

    @Override
    protected void onCleared() {
        super.onCleared();
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
    }

    @Override
    public void onResult(List<? extends UserInfo> userList, Exception e) {
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
                        notifyDataSetChanged(SelectableUserInfoListViewModel.this.userList.getValue());
                    }
                });
                return;
            }
            changeAlertStatus(StatusFrameView.Status.ERROR);
            notifyDataSetChanged(this.userList.getValue());
        } else {
            List<UserInfo> newUsers = new ArrayList<>(userList);
            List<UserInfo> origin = this.userList.getValue();
            if (origin != null) {
                newUsers.addAll(0, origin);
            }

            removeCurrentUser(newUsers);
            applyUserList(newUsers);
        }
        isInitialRequest = false;
    }

    SelectableUserInfoListViewModel(CustomUserListQueryHandler customUserListQueryHandler) {
        super();
        this.userListQuery = SendBird.createApplicationUserListQuery();
        this.userListQuery.setLimit(USER_LIST_LIMIT);
        this.customUserListQueryHandler = customUserListQueryHandler == null ? SendBirdUIKit.getCustomUserListQueryHandler() : customUserListQueryHandler;
    }

    public LiveData<List<UserInfo>> getUserList() {
        loadInitial();
        return userList;
    }

    public LiveData<StatusFrameView.Status> getStatusFrame() {
        return statusFrame;
    }

    private void changeAlertStatus(StatusFrameView.Status status) {
        List<UserInfo> origin = userList.getValue();
        boolean hasData = origin != null && origin.size() > 0;
        if (!hasData || status == StatusFrameView.Status.NONE) {
            statusFrame.postValue(status);
        }
    }

    private void removeCurrentUser(@NonNull List<UserInfo> newUserList) {
        for (Iterator<UserInfo> iterator = newUserList.iterator(); iterator.hasNext();) {
            UserInfo userInfo = iterator.next();
            if (userInfo != null && userInfo.getUserId() != null &&
                    SendBird.getCurrentUser() != null &&
                    userInfo.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                iterator.remove();
                break;
            }
        }
    }

    private void applyUserList(List<UserInfo> newUserList) {
        changeAlertStatus(newUserList.size() == 0 ? StatusFrameView.Status.EMPTY : StatusFrameView.Status.NONE);
        notifyDataSetChanged(newUserList);
    }

    private void notifyDataSetChanged(List<UserInfo> newList) {
        userList.postValue(newList == null ? new ArrayList<>() : newList);
    }

    private void loadInitial() {
        isInitialRequest = true;
        if (customUserListQueryHandler != null) {
            customUserListQueryHandler.loadInitial(this);
        } else {
            TaskQueue.addTask(new JobResultTask<List<? extends UserInfo>>() {
                @Override
                public List<? extends UserInfo> call() throws Exception {
                    return loadFromSendBird();
                }

                @Override
                public void onResultForUiThread(List<? extends UserInfo> result, SendBirdException e) {
                    onResult(result, e);
                }
            });
        }
    }

    @Override
    public boolean hasNext() {
        if (customUserListQueryHandler != null) {
            return customUserListQueryHandler.hasMore();
        } else {
            return userListQuery.hasNext();
        }
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public List<? extends UserInfo> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends UserInfo> loadNext() {
        if (hasNext()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<List<? extends UserInfo>> result = new AtomicReference<>();
            final AtomicReference<Exception> error = new AtomicReference<>();

            try {
                if (customUserListQueryHandler != null) {
                    customUserListQueryHandler.loadNext((userList, e) -> {
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
                } else {
                    result.set(loadFromSendBird());
                }
            } catch (Exception e) {
                error.set(e);
            } finally {
                onResult(result.get(), error.get());
            }
            return result.get();
        }
        return Collections.emptyList();
    }

    private List<? extends UserInfo> loadFromSendBird() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<List<? extends UserInfo>> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        userListQuery.next((list, e) -> {
            try {
                if (e != null) {
                    error.set(e);
                    return;
                }
                Logger.dev("++ list : %s", list);
                List<UserInfo> newUsers = new ArrayList<>();
                for (User user : list) {
                    newUsers.add(UserUtils.toUserInfo(user));
                }
                result.set(newUsers);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        if (error.get() != null) throw error.get();
        return result.get();
    }
}
