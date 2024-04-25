package com.sendbird.uikit.samples.customization.userlist

import android.app.Activity
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.user.Member
import com.sendbird.uikit.activities.MemberListActivity
import com.sendbird.uikit.fragments.MemberListFragment
import com.sendbird.uikit.interfaces.providers.MemberListFragmentProvider
import com.sendbird.uikit.model.DialogListItem
import com.sendbird.uikit.providers.FragmentProviders
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.customization.GroupChannelRepository

fun showCustomMemberContextMenuSample(activity: Activity) {
    GroupChannelRepository.getRandomChannel(activity) { channel ->
        FragmentProviders.memberList = MemberListFragmentProvider { channelUrl, _ ->
            MemberListFragment.Builder(channelUrl)
                .setCustomFragment(CustomMemberListFragment())
                .build()
        }
        activity.startActivity(MemberListActivity.newIntent(activity, channel.url))
    }
}

class CustomMemberListFragment : MemberListFragment() {
    override fun getActionContextMenuTitle(member: Member, channel: GroupChannel?): String {
        return "Custom Context Menu"
    }

    override fun makeActionContextMenu(member: Member, channel: GroupChannel?): MutableList<DialogListItem> {
        return super.makeActionContextMenu(member, channel).apply {
            add(DialogListItem(R.string.text_menu_thumbs_up, R.drawable.icon_good))
            add(DialogListItem(R.string.text_menu_thumbs_down, R.drawable.icon_bad))
        }
    }

    override fun onActionContextMenuItemClicked(member: Member, item: DialogListItem, channel: GroupChannel?): Boolean {
        return when (item.key) {
            R.string.text_menu_thumbs_up -> {
                println(">>>>>> Thumbs Up")
                true
            }
            R.string.text_menu_thumbs_down -> {
                println(">>>>>> Thumbs Down")
                true
            }
            else -> super.onActionContextMenuItemClicked(member, item, channel)
        }
    }
}
