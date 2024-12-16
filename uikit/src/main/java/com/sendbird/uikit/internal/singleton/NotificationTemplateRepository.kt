package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.WorkerThread
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.params.NotificationTemplateListParams
import com.sendbird.message.template.model.MessageTemplate
import com.sendbird.message.template.providers.MessageTemplateProvider
import com.sendbird.uikit.internal.model.notifications.NotificationTemplateList
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

private const val TEMPLATE_KEY_PREFIX = "SB_TEMPLATE_"
private const val LAST_UPDATED_TEMPLATE_LIST_TOKEN = "LAST_UPDATED_TEMPLATE_LIST_AT"
private const val TEMPLATE_COUNT = "TEMPLATE_COUNT"
private const val PREFERENCE_FILE_NAME = "com.sendbird.notifications.templates"
private const val MAX_CACHED_TEMPLATE_COUNT = 1000

internal class NotificationTemplateRepository(context: Context) : MessageTemplateProvider, TemplateMapperDataProvider {
    private val templateCache: MutableMap<String, MessageTemplate> = ConcurrentHashMap()
    private val preferences = BaseSharedPreference(context.applicationContext, PREFERENCE_FILE_NAME)
    private var lastCacheToken: String = ""
        get() {
            return field.ifEmpty {
                field = preferences.optString(LAST_UPDATED_TEMPLATE_LIST_TOKEN)
                field
            }
        }
        private set(value) {
            if (value != field) {
                field = value
                preferences.putString(LAST_UPDATED_TEMPLATE_LIST_TOKEN, value)
            }
        }

    init {
        checkCountLimit()
        preferences.loadAll({ key ->
            key.startsWith(TEMPLATE_KEY_PREFIX)
        }, { key, value ->
            templateCache[key] = MessageTemplate.fromJson(value.toString())
        }).also {
            preferences.putInt(TEMPLATE_COUNT, templateCache.size)
        }
    }

    private fun checkCountLimit() {
        val count = preferences.getInt(TEMPLATE_COUNT)
        Logger.d("++ cached template count = $count")
        if (count > MAX_CACHED_TEMPLATE_COUNT) {
            clearAll()
        }
    }

    private fun getCacheKey(key: String) = "${TEMPLATE_KEY_PREFIX}$key"

    @WorkerThread
    @Synchronized
    private fun saveToCache(template: MessageTemplate) {
        Logger.d(">> NotificationTemplateRepository::saveToCache() key=${template.templateKey}")
        val key = getCacheKey(template.templateKey)
        templateCache[key] = template
        preferences.putString(key, template.toString())
        preferences.putInt(TEMPLATE_COUNT, templateCache.size)
    }

    internal fun needToUpdateTemplateList(latestUpdatedToken: String?): Boolean {
        return lastCacheToken.isEmpty() || lastCacheToken != latestUpdatedToken
    }

    internal fun getTemplate(key: String): MessageTemplate? {
        Logger.d(">> NotificationTemplateRepository::getTemplate() key=$key")
        return templateCache[getCacheKey(key)]
    }

    @WorkerThread
    @Throws(SendbirdException::class)
    internal fun requestTemplateListBlocking(): NotificationTemplateList {
        return requestTemplateListBlocking(
            NotificationTemplateListParams().apply { limit = 100 },
            lastCacheToken
        )
    }

    @WorkerThread
    @Throws(SendbirdException::class)
    internal fun requestTemplateListBlocking(params: NotificationTemplateListParams, cachedToken: String? = null): NotificationTemplateList {
        Logger.d(">> NotificationTemplateRepository::requestTemplateList()")
        val latch = CountDownLatch(1)
        var error: SendbirdException? = null
        val result: AtomicReference<NotificationTemplateList> = AtomicReference()
        val shouldUpdateToken = cachedToken != null
        SendbirdChat.getNotificationTemplateListByToken(
            cachedToken,
            params
        ) { notificationTemplateList, _, token, e ->
            error = e
            try {
                val templateList = notificationTemplateList?.let {
                    NotificationTemplateList.fromJson(it.jsonPayload)
                }
                if (shouldUpdateToken && !token.isNullOrEmpty()) lastCacheToken = token
                result.set(templateList)
            } catch (e: Throwable) {
                error = SendbirdException("notification template list data is not valid", e)
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        error?.let { throw it }
        return result.get().also {
            Logger.i("++ request response template list size=${it.templates.size}")
            it?.templates?.forEach { template ->
                // convert list to map
                saveToCache(template)
            }
        }
    }

    @WorkerThread
    @Throws(SendbirdException::class)
    internal fun requestTemplateBlocking(key: String): MessageTemplate {
        Logger.d(">> NotificationTemplateRepository::requestTemplate() key=$key")
        val latch = CountDownLatch(1)
        var error: SendbirdException? = null
        val result: AtomicReference<MessageTemplate> = AtomicReference()
        SendbirdChat.getNotificationTemplate(key) { template, e ->
            error = e
            try {
                template?.let {
                    Logger.i("++ request response template key=$key : ${it.jsonPayload}")
                    result.set(MessageTemplate.fromJson(it.jsonPayload))
                }
            } catch (e: Throwable) {
                error = SendbirdException("notification template response data is not valid", e)
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        error?.let { throw it }

        // save to cache
        return result.get().also { saveToCache(it) }
    }

    internal fun clearAll() {
        lastCacheToken = ""
        templateCache.clear()
        preferences.clearAll()
    }

    override fun provide(key: String): MessageTemplate? {
        return getTemplate(key)
    }

    override fun isValid(message: BaseMessage): Boolean = message.notificationData != null
    override fun isTemplateMessage(message: BaseMessage): Boolean = message.notificationData != null
    override fun hasAllTemplates(message: BaseMessage): Boolean {
        // if the sbm message supports the CarouselView later, it should also check the child templates.
        return message.notificationData?.let {
            return hasTemplate(it.templateKey)
        } ?: false
    }

    override fun hasTemplate(key: String): Boolean = getTemplate(key) != null
    override fun getTemplateKey(message: BaseMessage): String? = message.notificationData?.templateKey
    override fun childTemplateKeys(message: BaseMessage): List<String> = emptyList()

    @WorkerThread
    @Throws(SendbirdException::class)
    override fun requestTemplateListBlocking(keys: List<String>): List<MessageTemplate> {
        return requestTemplateListBlocking(NotificationTemplateListParams().apply {
            this.limit = 100
            this.keys = keys
        }).templates
    }
}
