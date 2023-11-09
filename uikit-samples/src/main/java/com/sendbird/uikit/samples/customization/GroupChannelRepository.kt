package com.sendbird.uikit.samples.customization

import android.app.Activity
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.GroupChannelListQueryParams
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.utils.ContextUtils
import java.util.concurrent.Executors

internal object GroupChannelRepository {
    private val worker = Executors.newSingleThreadExecutor()
    private var channelCache = mutableListOf<GroupChannel>()

    fun getRandomChannel(activity: Activity, callback: (GroupChannel) -> Unit) {
        if (channelCache.isNotEmpty()) {
            callback(channelCache.random())
            return
        }
        WaitingDialog.show(activity)
        worker.submit {
            GroupChannel.createMyGroupChannelListQuery(GroupChannelListQueryParams()).next { channels, e ->
                WaitingDialog.dismiss()
                if (e != null) {
                    ContextUtils.toastError(activity, "No channels")
                    return@next
                }
                channels?.let { channelCache.addAll(it) }
                activity.runOnUiThread { callback(channelCache.random()) }
            }
        }
    }
}
