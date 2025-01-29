package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.sendbird.android.channel.SimpleTemplateData
import com.sendbird.android.exception.SendbirdException
import com.sendbird.message.template.TemplateParser
import com.sendbird.message.template.consts.MessageTemplateError
import com.sendbird.message.template.model.TemplateParams
import com.sendbird.uikit.internal.extensions.runOnUiThread
import com.sendbird.uikit.internal.extensions.toTemplateTheme
import com.sendbird.uikit.internal.interfaces.GetTemplateResultHandler
import com.sendbird.uikit.internal.model.notifications.NotificationChannelSettings
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.TemplateParamsCreator
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@VisibleForTesting
internal const val MAX_REQUEST_TEMPLATE_RETRY_COUNT = 10
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
    @VisibleForTesting
    internal val templateRequestCount: MutableMap<String, Int> = ConcurrentHashMap()

    @VisibleForTesting
    internal lateinit var templateRepository: NotificationTemplateRepository
    @VisibleForTesting
    internal lateinit var channelSettingsRepository: NotificationChannelRepository
    private lateinit var templateParser: TemplateParser
    @JvmStatic
    val mapper: TemplateMapperDataProvider
        get() = templateRepository

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
            templateParser = TemplateParser(templateRepository)
            isInitialized.set(true)
        }.get()
    }

    @JvmStatic
    fun hasTemplate(key: String): Boolean = templateRepository.getTemplate(key) != null

    @Throws(SendbirdException::class)
    @JvmStatic
    fun parseTemplate(key: String, themeMode: NotificationThemeMode, dataVariables: Map<String, String>, viewVariables: Map<String, List<SimpleTemplateData>> = emptyMap()): TemplateParams {
        val template = templateRepository.getTemplate(key) ?: throw SendbirdException("dataTemplate is empty", MessageTemplateError.ERROR_TEMPLATE_NOT_EXIST)
        return if (template.isDataTemplate) {
            TemplateParamsCreator.createDataTemplateViewParams(template.dataTemplate, NotificationThemeMode.Default.toTemplateTheme())
        } else {
            templateParser.parse(key, themeMode.toTemplateTheme(), dataVariables, viewVariables)
        }
    }

    @JvmStatic
    fun requestTemplate(
        key: String,
        variables: Map<String, String>,
        themeMode: NotificationThemeMode,
        callback: GetTemplateResultHandler) {
        synchronized(templateRequestDatas) {
            val request = TemplateRequestData(key, variables, themeMode, callback)
            val isRequesting = templateRequestDatas[key] != null
            if (isRequesting.not()) {
                templateRequestDatas[key] = mutableSetOf()
            }
            templateRequestDatas[key]?.add(request)

            // Apply a retry count to prevent infinite requests in case of failure.
            val retryCount = templateRequestCount[key] ?: 0
            if (retryCount >= MAX_REQUEST_TEMPLATE_RETRY_COUNT) {
                notifyError(key, SendbirdException("Too many template requests have been made.[key=$key]"), true)
                return
            }
            templateRequestCount[key] = retryCount + 1

            if (isRequesting) {
                Logger.i("-- return (fetching template request already exists), key=$key, handler count=${templateRequestDatas.size}")
                return
            }
        }
        Logger.d("++ templateRequestHandlers size=${templateRequestDatas.size}, templateRequestHandlers[key].size=${templateRequestDatas[key]?.size}")
        worker.submit {
            try {
                // 1. get template
                templateRepository.requestTemplateBlocking(key)

                // 2. parse template to templateParams
                val templateParams = parseTemplate(key, themeMode, variables)

                // 3. notify
                notifyTemplateFetched(key, templateParams)
            } catch (e: Throwable) {
                notifyError(key, SendbirdException(e))
            }
        }
    }

    private fun notifyTemplateFetched(key: String, params: TemplateParams) {
        Logger.d("NotificationChannelManager::makeAndNotifyTemplate()")
        runOnUiThread {
            dispatchResults(key, params, null)
        }
    }

    private fun notifyError(key: String, e: SendbirdException, runOnCurrentThread: Boolean = false) {
        if (runOnCurrentThread) {
            dispatchResults(key, null, e)
            return
        }
        runOnUiThread {
            dispatchResults(key, null, e)
        }
    }

    private fun dispatchResults(key: String, params: TemplateParams?, e: SendbirdException? = null) {
        synchronized(templateRequestDatas) {
            try {
                Logger.d("NotificationChannelManager::dispatchResults()")
                templateRequestDatas[key]?.forEach { requestData ->
                    requestData.handler.onResult(key, params, e)
                }
            } finally {
                templateRequestDatas.remove(key)
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
