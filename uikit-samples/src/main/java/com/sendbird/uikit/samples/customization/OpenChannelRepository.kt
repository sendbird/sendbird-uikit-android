package com.sendbird.uikit.samples.customization

import android.app.Activity
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.params.OpenChannelListQueryParams
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.utils.ContextUtils
import java.util.concurrent.Executors

internal object OpenChannelRepository {
    private val worker = Executors.newSingleThreadExecutor()
    private var channelCache = mutableListOf<OpenChannel>()

    fun getRandomChannel(activity: Activity, callback: (OpenChannel) -> Unit) {
        if (channelCache.isNotEmpty()) {
            callback(channelCache.random())
            return
        }
        WaitingDialog.show(activity)
        worker.submit {
            OpenChannel.createOpenChannelListQuery(OpenChannelListQueryParams()).next { channels, e ->
                WaitingDialog.dismiss()
                if (e != null || channels.isNullOrEmpty()) {
                    ContextUtils.toastError(activity, "No channels")
                    return@next
                }
                channelCache.addAll(channels)
                activity.runOnUiThread { callback(channelCache.random()) }
            }
        }
    }
}
