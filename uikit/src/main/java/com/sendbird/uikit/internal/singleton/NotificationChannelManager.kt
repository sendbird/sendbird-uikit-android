package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.WorkerThread
import com.sendbird.android.exception.SendbirdException
import com.sendbird.uikit.internal.extensions.runOnUiThread
import com.sendbird.uikit.internal.interfaces.GetTemplateResultHandler
import com.sendbird.uikit.internal.model.notifications.NotificationChannelSettings
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal object NotificationChannelManager {
    private val worker = Executors.newFixedThreadPool(10)
    private val isInitialized: AtomicBoolean = AtomicBoolean()
    private val templateRequestHandlers: MutableMap<String, MutableSet<GetTemplateResultHandler>> = ConcurrentHashMap()

    private lateinit var templateRepository: NotificationTemplateRepository
    private lateinit var channelSettingsRepository: NotificationChannelRepository

    @JvmStatic
    fun init(context: Context) {
        if (isInitialized.getAndSet(true)) return
        worker.submit {
            templateRepository = NotificationTemplateRepository(context.applicationContext)
            channelSettingsRepository = NotificationChannelRepository(context.applicationContext)
        }.get()
    }

    @JvmStatic
    fun hasTemplate(key: String): Boolean = templateRepository.getTemplate(key) != null

    @JvmStatic
    fun makeTemplate(
        key: String,
        variables: Map<String, String>,
        themeMode: NotificationThemeMode,
        callback: GetTemplateResultHandler
    ) {
        Logger.d(">> NotificationChannelManager::makeTemplate(), key=$key, handler=$callback")

        templateRepository.getTemplate(key)?.getTemplateSyntax(variables, themeMode)?.let {
            Logger.d("++ template[$key]=$it")
            callback.onResult(key, it, null)
            return
        }

        synchronized(templateRequestHandlers) {
            templateRequestHandlers[key]?.let {
                it.add(callback)
                Logger.i("-- return (fetching template request already exists), key=$key, handler count=${templateRequestHandlers.size}")
                return
            } ?: run {
                templateRequestHandlers[key] = mutableSetOf<GetTemplateResultHandler>().apply {
                    add(callback)
                }
            }
        }
        Logger.d("++ templateRequestHandlers size=${templateRequestHandlers.size}, templateRequestHandlers[key].size=${templateRequestHandlers[key]?.size}")
        worker.submit {
            try {
                val template = templateRepository.requestTemplateBlocking(key).getTemplateSyntax(variables, themeMode)
                Logger.d("++ template[$key]=$template")
                notifyTemplateFetched(key, template)
            } catch (e: Throwable) {
                notifyTemplateFetched(key, null, SendbirdException(e))
            }
        }
    }

    @JvmStatic
    fun getGlobalNotificationChannelSettings(): NotificationChannelSettings? {
        return channelSettingsRepository.settings
    }

    @WorkerThread
    @JvmStatic
    @Throws(Exception::class)
    @Synchronized
    fun requestTemplateListBlocking(latestToken: String?) {
        // 1. check updated time with server.
        if (!templateRepository.needToUpdateTemplateList(latestToken)) {
            Logger.d("++ skip request template list. no more items to update")
            return
        }

        // 2. call api
        templateRepository.requestTemplateListBlocking()
    }

    @WorkerThread
    @JvmStatic
    @Throws(Exception::class)
    @Synchronized
    fun requestNotificationChannelSettingBlocking(latestUpdatedAt: Long): NotificationChannelSettings {
        // 0-1. check from cache
        channelSettingsRepository.settings?.let {
            if (!channelSettingsRepository.needToUpdate(latestUpdatedAt)) {
                Logger.d("++ skip request channel theme settings. no more items to update")
                return it
            }
        }
        // 1. call api
        return channelSettingsRepository.requestSettings()
    }

    private fun notifyTemplateFetched(key: String, template: String?, e: SendbirdException? = null) {
        runOnUiThread {
            synchronized(templateRequestHandlers) {
                try {
                    Logger.d("NotificationChannelManager::notifyTemplateFetched()")
                    templateRequestHandlers[key]?.forEach { handler ->
                        handler.onResult(key, template, e)
                    }
                } finally {
                    templateRequestHandlers.remove(key)
                }
            }
        }
    }

    @JvmStatic
    fun clearAll() {
        Logger.d("NotificationChannelManager::clearAll()")
        templateRepository.clearAll()
        channelSettingsRepository.clearAll()
    }
}
