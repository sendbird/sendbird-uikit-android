package com.sendbird.uikit.samples.customization.userlist

import android.app.Activity
import com.sendbird.uikit.activities.CreateChannelActivity
import com.sendbird.uikit.activities.adapter.CreateChannelUserListAdapter
import com.sendbird.uikit.consts.CreatableChannelType
import com.sendbird.uikit.interfaces.UserInfo
import com.sendbird.uikit.interfaces.providers.CreateChannelUserListAdapterProvider
import com.sendbird.uikit.providers.AdapterProviders

/**
 * In this sample, the user selection screen only shows users whose nicknames include 'e'.
 *
 * step 1. Create a custom user list adapter by extending the adapter where you want to filter the user list.
 * step 2. Set [CreateChannelUserListAdapterProvider] to [AdapterProviders.createChannelUserList].
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showUserItemFilteringSample(activity: Activity) {
    AdapterProviders.createChannelUserList = CreateChannelUserListAdapterProvider {
        CustomFilterCreateChannelUserListAdapter()
    }

    val intent = CreateChannelActivity.newIntent(activity.applicationContext, CreatableChannelType.Normal)
    activity.startActivity(intent)
}

/**
 * This class demonstrates how to filter the user list.
 *
 * step 1. Inherit [CreateChannelUserListAdapter] and define new userList field.
 * step 2. Filter the user list from [userList] to new userList.
 * step 3. Override [getItem] and [getItemCount] to return the filtered user list.
 */
class CustomFilterCreateChannelUserListAdapter : CreateChannelUserListAdapter() {

    private val filteredUserList: List<UserInfo>
        get() = userList.filter { it.nickname?.contains("E", ignoreCase = true) == true }

    override fun getItem(position: Int): UserInfo {
        return filteredUserList[position]
    }

    override fun getItemCount(): Int {
        return filteredUserList.count()
    }
}

