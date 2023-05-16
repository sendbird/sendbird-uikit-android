package com.sendbird.uikit.internal.queries

import com.sendbird.android.SendbirdChat.createApplicationUserListQuery
import com.sendbird.android.params.ApplicationUserListQueryParams
import com.sendbird.android.user.User
import com.sendbird.android.user.query.ApplicationUserListQuery
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.interfaces.OnListResultHandler
import com.sendbird.uikit.interfaces.PagedQueryHandler
import com.sendbird.uikit.interfaces.UserInfo
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.utils.UserUtils

internal class DefaultUserListQuery @JvmOverloads constructor(private val exceptMe: Boolean = true) :
    PagedQueryHandler<UserInfo> {
    private var query: ApplicationUserListQuery? = null
    override fun loadInitial(handler: OnListResultHandler<UserInfo>) {
        query = createApplicationUserListQuery(ApplicationUserListQueryParams().apply { limit = 30 })
        loadMore(handler)
    }

    override fun loadMore(handler: OnListResultHandler<UserInfo>) {
        query?.next { queryResult, e ->
            var userInfoList: List<UserInfo>? = null
            queryResult?.let { userInfoList = toUserInfoList(queryResult) }
            e?.also { Logger.e(e) }
            handler.onResult(userInfoList, e)
        }
    }

    override fun hasMore(): Boolean {
        return query?.hasNext ?: false
    }

    private fun toUserInfoList(users: List<User>): List<UserInfo> {
        val userInfoList: MutableList<UserInfo> = ArrayList()
        for (user in users) {
            if (exceptMe) {
                val userId = SendbirdUIKit.getAdapter()?.userInfo?.userId
                if (userId == user.userId) {
                    continue
                }
            }
            userInfoList.add(UserUtils.toUserInfo(user))
        }
        return userInfoList
    }
}
