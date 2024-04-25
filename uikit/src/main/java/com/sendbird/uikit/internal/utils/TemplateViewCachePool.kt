package com.sendbird.uikit.internal.utils

import android.view.View
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ConcurrentHashMap

/**
 * Be careful that this TemplateViewCachePool is not referenced by multiple objects.
 * Consider java's garbage collection policy so that when a component with a cache pool is freed from memory,
 * this cache pool is also freed.
 */
internal class TemplateViewCachePool {
    private val viewCachePool: MutableMap<String, MutableList<View>> = ConcurrentHashMap()

    /**
     * get scrapped view which is not attached to any parent from cache pool
     */
    internal fun getScrappedView(key: String): View? {
        val views = viewCachePool[key]
        return views?.firstOrNull { it.parent == null }.also {
            Logger.d("key: $key, view cache ${if (it != null) "hit" else "missed"}")
        }
    }

    internal fun cacheView(key: String, view: View) {
        viewCachePool.getOrPut(key) { mutableListOf() }.add(view)
    }
}
