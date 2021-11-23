package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageSearchQuery;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.widgets.PagerRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SearchViewModel extends BaseViewModel implements LifecycleObserver, PagerRecyclerView.Pageable<List<BaseMessage>> {
    private final MutableLiveData<List<BaseMessage>> searchResultList = new MutableLiveData<>();
    private final GroupChannel channel;
    private final long searchFrom;
    private MessageSearchQuery query;

    SearchViewModel(GroupChannel channel, @NonNull MessageSearchQuery query) {
        super();
        this.channel = channel;
        this.searchFrom = channel.getInvitedAt();
        this.query = query;
    }

    private MessageSearchQuery createMessageSearchQuery(@NonNull String keyword) {
        if (query != null) {
            return new MessageSearchQuery.Builder(query)
                    .setChannelUrl(channel.getUrl())
                    .setKeyword(keyword)
                    .setOrder(MessageSearchQuery.Order.TIMESTAMP)
                    .build();
        }
        return new MessageSearchQuery.Builder()
                .setChannelUrl(channel.getUrl())
                .setKeyword(keyword)
                .setLimit(40)
                .setMessageTimestampFrom(searchFrom)
                .setOrder(MessageSearchQuery.Order.TIMESTAMP)
                .setReverse(false)
                .build();
    }

    public LiveData<List<BaseMessage>> getSearchResultList() {
        return searchResultList;
    }

    public void search(@NonNull String keyword, OnListResultHandler<BaseMessage> handler) {
        if (TextUtils.isEmpty(keyword)) return;

        this.query = createMessageSearchQuery(keyword.trim());
        List<BaseMessage> value = this.searchResultList.getValue();
        if (value != null) {
            value.clear();
        }
        this.query.next((queryResult, e) -> {
            if (handler != null) {
                handler.onResult(queryResult, e);
            }
            SearchViewModel.this.onResult(queryResult, e);
        });
    }

    public String getKeyword() {
        return this.query.getKeyword();
    }

    private void onResult(List<BaseMessage> results, Exception e) {
        if (e != null) {
            Logger.w(e);
            return;
        }

        List<BaseMessage> newDataList = new ArrayList<>();
        if (results != null && results.size() > 0) {
            newDataList.addAll(results);
            List<BaseMessage> origin = this.searchResultList.getValue();
            if (origin != null) {
                newDataList.addAll(0, origin);
                Logger.d("____________ onResult origin=%s", origin.size());
            }
        }
        Logger.d("____________ onResult newDataList=%s", newDataList.size());
        searchResultList.postValue(newDataList);
    }

    @Override
    public boolean hasNext() {
        return query.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public List<BaseMessage> loadPrevious() {
        return Collections.emptyList();
    }

    @Override
    public List<BaseMessage> loadNext() {
        Logger.d("____________ loadNext hasNext=%s", query.hasNext());
        if (hasNext()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<List<BaseMessage>> result = new AtomicReference<>();
            final AtomicReference<Exception> error = new AtomicReference<>();
            try {
                query.next((queryResult, e) -> {
                    try {
                        if (e != null) {
                            error.set(e);
                            return;
                        }
                        result.set(queryResult);
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await();
            } catch (Exception e) {
                error.set(e);
            } finally {
                onResult(result.get(), error.get());
            }
            return result.get();
        }
        return Collections.emptyList();
    }
}
