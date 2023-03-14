package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.WorkerThread
import com.sendbird.uikit.internal.model.notifications.NotificationChannelSettings
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.log.Logger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal object NotificationChannelManager {
    private val worker = Executors.newFixedThreadPool(10)
    private val isInitialized: AtomicBoolean = AtomicBoolean()
    private val loadingKeys: MutableSet<String> = mutableSetOf()

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
    fun getTemplate(key: String, variables: Map<String, String>, themeMode: NotificationThemeMode): String? {
        return templateRepository.getTemplate(key)?.let {
            return it.getTemplateSyntax(variables, themeMode).run {
                Logger.d("++ key=[$key], template=$this")
                this
            }
        } ?: run {
            // If the data is not in the cache, it is not applied in real time even if it is received from the API.
            requestTemplate(key)
            null
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
        templateRepository.requestTemplateList()
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

    private fun requestTemplate(key: String) {
        Logger.d(">> NotificationChannelManager::requestTemplate(), key=$key")
        // 0. check it already has been requested.
        synchronized(loadingKeys) {
            if (!loadingKeys.add(key)) return
        }
        worker.submit {
            try {
                // 1. call API
                templateRepository.requestTemplate(key)
            } catch (ignore: Exception) {
            } finally {
                synchronized(loadingKeys) {
                    loadingKeys.remove(key)
                }
            }
        }
    }

    @JvmStatic
    fun dispose() {
        templateRepository.dispose()
        channelSettingsRepository.dispose()
        worker.shutdown()
    }
}

