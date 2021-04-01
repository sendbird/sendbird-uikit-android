package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class ChatMessageCollection {
    private static final Comparator<BaseMessage> comparator = (o1, o2) -> {
        if (o1.getCreatedAt() > o2.getCreatedAt()) {
            return 1;
        } else if (o1.equals(o2)) {
            return 0;
        }
        return -1;
    };
    private final TreeSet<BaseMessage> messageList = new TreeSet<>(comparator);

    private BaseMessage createTimeline(BaseMessage anchor) {
        return new TimelineMessage(anchor);
    }

    public synchronized void clear() {
        messageList.clear();
    }

    public synchronized void add(@NonNull BaseMessage message) {
        messageList.add(message);
    }

    public synchronized void addAll(@NonNull Collection<BaseMessage> messageList) {
        for (BaseMessage message : messageList) {
            add(message);
        }
    }

    public synchronized void removeByMessageId(long msgId) {
        if (messageList.size() > 0) {
            for (BaseMessage message : messageList) {
                if (message.getMessageId() == msgId) {
                    remove(message);
                    break;
                }
            }
        }
    }

    public synchronized boolean remove(BaseMessage message) {
        if (message == null) return false;
        return messageList.remove(message);
    }

    public synchronized void update(@NonNull BaseMessage updatedMessage) {
        if (messageList.contains(updatedMessage)) {
            remove(updatedMessage);
            add(updatedMessage);
        }
    }

    public synchronized void updateAll(@NonNull List<BaseMessage> updated) {
        for (BaseMessage updatedMessage : updated) {
            update(updatedMessage);
        }
    }

    public boolean hasMessage() {
        return size() > 0;
    }

    public int size() {
        return messageList.size();
    }

    public BaseMessage get(long messageId) {
        for (BaseMessage message : messageList) {
            if (message.getMessageId() == messageId) {
                return message;
            }
        }
        return null;
    }

    public BaseMessage last() {
        return messageList.last();
    }

    public BaseMessage first() {
        return messageList.first();
    }

    public synchronized List<BaseMessage> copyToList() {
        BaseMessage prevMessage = null;
        List<BaseMessage> copiedList = new ArrayList<>();
        for (BaseMessage message : messageList) {
            copiedList.add(0, BaseMessage.clone(message));
            if (prevMessage == null || !DateUtils.hasSameDate(message.getCreatedAt(), prevMessage.getCreatedAt())) {
                BaseMessage timeline = new TimelineMessage(message);
                copiedList.add(1, timeline);
            }
            prevMessage = message;
        }
        return copiedList;
    }
}
