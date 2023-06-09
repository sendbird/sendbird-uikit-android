package com.sendbird.uikit.internal.singleton

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.interfaces.MessageDisplayDataProvider
import com.sendbird.uikit.internal.model.MessageDisplayDataWrapper
import com.sendbird.uikit.model.MessageDisplayData
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.ConcurrentHashMap

internal object MessageDisplayDataManager {
    private val messageDisplayDataMap: MutableMap<String, MessageDisplayDataWrapper> = ConcurrentHashMap()

    @JvmStatic
    fun getOrNull(message: BaseMessage): MessageDisplayData? {
        return messageDisplayDataMap[generateKey(message)]?.messageDisplayData
    }

    @JvmStatic
    @Synchronized
    fun clearAll() {
        messageDisplayDataMap.clear()
    }

    @JvmStatic
    @Synchronized
    @JvmName("checkAndGenerateDisplayData")
    fun checkAndGenerateDisplayData(
        messageList: List<BaseMessage>,
        provider: MessageDisplayDataProvider,
    ) {
        messageList.filter {
            val userMessageDisplayDataWrapper = messageDisplayDataMap[generateKey(it)]
            userMessageDisplayDataWrapper == null || userMessageDisplayDataWrapper.updatedAt < it.updatedAt
        }.apply {
            provider.generate(this).let {
                it.mapValues { (message, messageDisplayData) ->
                    messageDisplayDataMap.put(
                        generateKey(message),
                        MessageDisplayDataWrapper(
                            messageDisplayData = messageDisplayData,
                            updatedAt = message.updatedAt
                        )
                    )
                }
            }
        }
    }

    @JvmStatic
    @JvmName("checkAndGenerateDisplayDataFromChannelList")
    fun checkAndGenerateDisplayData(
        channelList: List<GroupChannel>,
        provider: MessageDisplayDataProvider
    ) {
        val messageList = mutableListOf<BaseMessage>()
        for (channel in channelList) {
            channel.lastMessage?.let {
                messageList.add(it)
            }
        }
        checkAndGenerateDisplayData(messageList, provider)
    }

    private fun generateKey(message: BaseMessage): String {
        return "${message.requestId}_${message.messageId}"
    }

    @TestOnly
    fun getDataMap(): Map<String, MessageDisplayDataWrapper> {
        return messageDisplayDataMap
    }
}
