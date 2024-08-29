package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.WorkerThread
import com.sendbird.android.exception.SendbirdException
import com.sendbird.uikit.internal.extensions.runOnUiThread
import com.sendbird.uikit.internal.interfaces.GetTemplateResultHandler
import com.sendbird.uikit.internal.model.notifications.NotificationChannelSettings
import com.sendbird.uikit.internal.model.notifications.NotificationTemplate
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal object NotificationChannelManager {
    private data class TemplateRequestData(
        val key: String,
        val variables: Map<String, String>,
        val themeMode: NotificationThemeMode,
        val handler: GetTemplateResultHandler
    )

    private val worker = Executors.newFixedThreadPool(10)
    private val isInitialized: AtomicBoolean = AtomicBoolean()
    private val templateRequestDatas: MutableMap<String, MutableSet<TemplateRequestData>> = ConcurrentHashMap()

    private lateinit var templateRepository: NotificationTemplateRepository
    private lateinit var channelSettingsRepository: NotificationChannelRepository

    /**
     * To avoid sending an unintended exception, if the NotificationChannelManager hasn't been initialized it tries to initialize automatically.
     * This is very defensive code and only works when creating a Fragment and attempting to reference NotificationChannelManager in exceptional cases.
     */
    @Synchronized
    internal fun checkAndInit(context: Context) {
        Logger.i(">> NotificationChannelManager::checkAndInit() isInitialized=${isInitialized.get()}")
        if (!isInitialized.get()) {
            init(context)
        }
    }

    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        Logger.d("++ NotificationChannelManager init start ${Thread.currentThread().name}, isInitialized=${isInitialized.get()}")
        if (isInitialized.get()) return
        worker.submit {
            channelSettingsRepository = NotificationChannelRepository(context.applicationContext)
            templateRepository = NotificationTemplateRepository(context.applicationContext)
            isInitialized.set(true)
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

        templateRepository.getTemplate(key)?.let {
            val jsonTemplate = it.getTemplateSyntax(variables, themeMode)
            Logger.d("++ template[$key]=$jsonTemplate")
            callback.onResult(key, jsonTemplate, it.isDataTemplate, null)
            return
        }

        synchronized(templateRequestDatas) {
            val request = TemplateRequestData(key, variables, themeMode, callback)
            templateRequestDatas[key]?.let {
                it.add(request)
                Logger.i("-- return (fetching template request already exists), key=$key, handler count=${templateRequestDatas.size}")
                return
            } ?: run {
                templateRequestDatas[key] = mutableSetOf<TemplateRequestData>().apply {
                    add(request)
                }
            }
        }
        Logger.d("++ templateRequestHandlers size=${templateRequestDatas.size}, templateRequestHandlers[key].size=${templateRequestDatas[key]?.size}")
        worker.submit {
            try {
                val rawTemplate = templateRepository.requestTemplateBlocking(key)
                makeAndNotifyTemplate(key, rawTemplate)
            } catch (e: Throwable) {
                notifyError(key, SendbirdException(e))
            }
        }
    }

    private fun makeAndNotifyTemplate(key: String, rawTemplate: NotificationTemplate) {
        runOnUiThread {
            synchronized(templateRequestDatas) {
                try {
                    Logger.d("NotificationChannelManager::makeAndNotifyTemplate()")
                    templateRequestDatas[key]?.forEach { requestData ->
                        // The template may be the same but variable may be a different message.(NOTI-1027)
                        val template = rawTemplate.getTemplateSyntax(requestData.variables, requestData.themeMode)
                        requestData.handler.onResult(key, template, rawTemplate.isDataTemplate, null)
                    }
                } finally {
                    templateRequestDatas.remove(key)
                }
            }
        }
    }

    private fun notifyError(key: String, e: SendbirdException) {
        runOnUiThread {
            synchronized(templateRequestDatas) {
                try {
                    Logger.d("NotificationChannelManager::notifyError()")
                    templateRequestDatas[key]?.forEach { requestData ->
                        requestData.handler.onResult(key, null, false, e)
                    }
                } finally {
                    templateRequestDatas.remove(key)
                }
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

    @JvmStatic
    fun clearAll() {
        Logger.d("NotificationChannelManager::clearAll()")
        templateRepository.clearAll()
        channelSettingsRepository.clearAll()
    }
}
