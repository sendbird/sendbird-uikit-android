package com.sendbird.uikit.samples.customization.moderation

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sendbird.android.channel.ReportCategory
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.uikit.activities.ChannelActivity
import com.sendbird.uikit.activities.viewholder.MessageType
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory
import com.sendbird.uikit.fragments.ChannelFragment
import com.sendbird.uikit.interfaces.OnCompleteHandler
import com.sendbird.uikit.interfaces.providers.ChannelFragmentProvider
import com.sendbird.uikit.interfaces.providers.ChannelViewModelProvider
import com.sendbird.uikit.model.DialogListItem
import com.sendbird.uikit.providers.FragmentProviders
import com.sendbird.uikit.providers.ViewModelProviders
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.customization.GroupChannelRepository
import com.sendbird.uikit.utils.MessageUtils
import com.sendbird.uikit.vm.ChannelViewModel
import com.sendbird.uikit.vm.ViewModelFactory
import java.util.Arrays
import java.util.Objects

fun showModerationGroupChannelSample(activity: Activity) {
    setModerationGroupChannelViewModelProviders()

    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
        ChannelFragment.Builder(channelUrl).withArguments(args)
            .setCustomFragment(ModerationGroupChannelFragment())
            .setUseHeader(true)
            .build()
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(
            ChannelActivity.newIntent(activity, channel.url)
        )
    }
}

class ModerationGroupChannelFragment : ChannelFragment() {
    override fun makeMessageContextMenu(message: BaseMessage): MutableList<DialogListItem> {
        val items: MutableList<DialogListItem> = super.makeMessageContextMenu(message)
        val status = message.sendingStatus
        if (status == SendingStatus.PENDING) return items

        val type = MessageViewHolderFactory.getMessageType(message)
        val report = DialogListItem(R.string.text_report, R.drawable.icon_error)

        when (type) {
            MessageType.VIEW_TYPE_USER_MESSAGE_ME -> if (status == SendingStatus.SUCCEEDED) {
                items.add(report)
            }
            MessageType.VIEW_TYPE_USER_MESSAGE_OTHER -> {
                items.add(report)
            }
            else -> {}
        }

        return items
    }

    override fun onMessageContextMenuItemClicked(message: BaseMessage, view: View, position: Int, item: DialogListItem): Boolean {
        val key = item.key

        if (key == R.string.text_report) {
            showSelectReportCategory(message)
            return true
        }

        super.onMessageContextMenuItemClicked(message, view, position, item)

        return false
    }

    private fun showSelectReportCategory(message: BaseMessage) {
        if (context == null) return

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        var category: ReportCategory

        builder.setTitle(getString(R.string.text_choose_report_category_dialog))
            .setNegativeButton(R.string.text_cancel) { dialog, which ->
                dialog.dismiss()
            }
            .setItems(
                arrayOf(
                    getString(R.string.text_report_suspicious),
                    getString(R.string.text_report_harassing),
                    getString(R.string.text_report_spam),
                    getString(R.string.text_report_inappropriate)
                )) { dialog, which ->
                when (which) {
                    0 -> {
                        category = ReportCategory.SUSPICIOUS
                    }

                    1 -> {
                        category = ReportCategory.HARASSING
                    }

                    2 -> {
                        category = ReportCategory.SPAM
                    }

                    else -> {
                        category = ReportCategory.INAPPROPRIATE
                    }
                }

                reportMessage(message, category, "")
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun reportMessage(message: BaseMessage, reportCategory: ReportCategory, reason: String) {
        (viewModel as ModerationGroupChannelViewModel).reportMessage(
            message,
            reportCategory,
            reason
        ) { e: SendbirdException? ->
            if (e == null) {
                toastSuccess(R.string.sb_view_toast_success_description, false)
            } else {
                toastError(R.string.text_report_error, false)
            }
        }
    }
}

class ModerationGroupChannelViewModel(channelUrl: String) : ChannelViewModel(channelUrl, null) {
    fun reportMessage(message: BaseMessage, reportCategory: ReportCategory, reason: String, handler: OnCompleteHandler?) {
        if (channel == null) return
        channel?.let {
            it.reportMessage(message, reportCategory, reason) { e: SendbirdException? -> handler?.onComplete(e) }
        }
    }
}


fun setModerationGroupChannelViewModelProviders() {
    @Suppress("UNCHECKED_CAST")
    class ModerationGroupChannelViewModelFactory(private vararg val params: Any?) : ViewModelFactory(*params) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == ModerationGroupChannelViewModel::class.java) {
                return ModerationGroupChannelViewModel(
                    (Objects.requireNonNull(params)[0] as String)
                ) as T
            }

            return super.create(modelClass)
        }
    }

    ViewModelProviders.channel = ChannelViewModelProvider { owner, channelUrl, _, _ ->
        ViewModelProvider(
            owner,
            ModerationGroupChannelViewModelFactory(channelUrl)
        )[channelUrl, ModerationGroupChannelViewModel::class.java]
    }
}
