package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.WorkerThread
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.MessageTemplateListParams
import com.sendbird.uikit.internal.model.templates.MessageTemplate
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

private const val MESSAGE_TEMPLATE_KEY_PREFIX = "SB_MESSAGE_TEMPLATE_"
private const val MESSAGE_TEMPLATE_LAST_UPDATED_TOKEN = "MESSAGE_TEMPLATE_LAST_UPDATED_TOKEN"
private const val PREFERENCE_FILE_NAME = "com.sendbird.message.templates"

/**
 * This class is used to store templates which is used for [com.sendbird.android.channel.GroupChannel].
 * It doesn't manage the templates for Notification. For Notification, use [NotificationTemplateRepository].
 */
internal class MessageTemplateRepository(context: Context) {
    private val templateCache: MutableMap<String, MessageTemplate> = ConcurrentHashMap()
    private val preferences = BaseSharedPreference(context.applicationContext, PREFERENCE_FILE_NAME)
    internal var lastCachedToken: String = ""
        get() {
            return field.ifEmpty {
                field = preferences.optString(MESSAGE_TEMPLATE_LAST_UPDATED_TOKEN)
                field
            }
        }
        private set(value) {
            if (value != field) {
                field = value
                preferences.putString(MESSAGE_TEMPLATE_LAST_UPDATED_TOKEN, value)
            }
        }

    private val initialTemplateLoadLock = Any()

    init {
        thread {
            synchronized(initialTemplateLoadLock) {
                preferences.loadAll(
                    predicate = { key ->
                        key.startsWith(MESSAGE_TEMPLATE_KEY_PREFIX)
                    },
                    onEach = { key, value ->
                        templateCache[key] = MessageTemplate.fromJson(value.toString())
                    }
                )
            }
        }
    }

    @WorkerThread
    private fun saveToCache(template: MessageTemplate) {
        Logger.d(">> MessageTemplateRepository::saveToCache() key=${template.templateKey}")
        val key = template.templateKey.toMessageTemplateKey()
        templateCache[key] = template
        preferences.putString(key, template.toString())
    }

    fun getTemplate(key: String): MessageTemplate? {
        Logger.d(">> MessageTemplateRepository::getTemplate() key=$key")
        return synchronized(initialTemplateLoadLock) {
            templateCache[key.toMessageTemplateKey()]
        }
    }

    @WorkerThread
    @Throws(SendbirdException::class)
    fun requestMessageTemplatesBlocking(
        params: MessageTemplateListParams = MessageTemplateListParams(limit = 100)
    ): List<MessageTemplate> {
        Logger.d(">> MessageTemplateRepository::requestTemplateList()")
        val latch = CountDownLatch(1)
        var error: SendbirdException? = null
        val result: AtomicReference<List<MessageTemplate>> = AtomicReference()
        val hasNoFilter = params.keys.isNullOrEmpty()
        val token = if (hasNoFilter) lastCachedToken else null
        SendbirdChat.getMessageTemplatesByToken(token, params) { messageTemplatesResult, e ->
            error = e
            try {
                if (hasNoFilter) {
                    // cache the token only when there is no filter
                    messageTemplatesResult?.token.takeUnless { it.isNullOrEmpty() }?.let { token ->
                        lastCachedToken = token
                    }
                }
                val templateList = messageTemplatesResult?.templates?.map {
                    MessageTemplate.fromJson(it.template)
                }
                result.set(templateList)
            } catch (e: Throwable) {
                error = SendbirdException("message template list data is not valid", e)
            } finally {
                latch.countDown()
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        error?.let { throw it }
        return result.get().also {
            it?.forEach { template ->
                // convert list to map
                saveToCache(template)
            }
        }
    }

    fun clearAll() {
        lastCachedToken = ""
        templateCache.clear()
        preferences.clearAll()
    }

    private fun String.toMessageTemplateKey() = MESSAGE_TEMPLATE_KEY_PREFIX + this
}
