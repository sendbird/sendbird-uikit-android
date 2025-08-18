package com.sendbird.uikit.internal.model

import com.sendbird.android.message.BaseMessage
import java.util.TreeSet

/**
 * A data class that holds properties used for mark as unread feature.
 *
 * @property unseenMessageList List of unseen messages. If this list is empty when the newLine is seen, the channel is marked as read. Otherwise, it's marked as unread.
 * @property hasSeenNewLine Indicates whether the newLine has been seen at least once.
 * @property hasUserMarkedUnread Indicates whether the user has explicitly invoked the mark as unread since entering the channel.
 * @property isNewLineExistInChannel Indicates whether the newLine exists in the channel. If this is false, the channel is marked as read.
 */
internal class ChannelUnreadInfo {
    // Ascending order by createdAt.
    private val unseenMessageList: TreeSet<BaseMessage> = TreeSet { o1: BaseMessage, o2: BaseMessage ->
        when {
            o1.createdAt > o2.createdAt -> 1
            o1.createdAt < o2.createdAt -> -1
            else -> 0
        }
    }

    val firstUnseenMessage: BaseMessage?
        get() = synchronized(this) {
            if (unseenMessageList.isEmpty()) null else unseenMessageList.first()
        }

    var hasSeenNewLine: Boolean = false
    var hasUserMarkedUnread: Boolean = false
    var isNewLineExistInChannel: Boolean = false

    fun shouldClearNewLine(): Boolean {
        if (hasUserMarkedUnread) return false
        return !isNewLineExistInChannel || hasSeenNewLine
    }

    @Synchronized
    fun addUnseenMessages(messages: List<BaseMessage>) {
        unseenMessageList.addAll(messages)
    }

    @Synchronized
    fun removeUnseenMessages(messages: List<BaseMessage>) {
        val deletedIds = messages.map { it.messageId }.toSet()
        unseenMessageList.removeAll { deletedIds.contains(it.messageId) }
    }

    @Synchronized
    fun clearUnseenMessages() {
        unseenMessageList.clear()
    }
}
