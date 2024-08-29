package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.activities.adapter.SuggestedRepliesAdapter
import com.sendbird.uikit.consts.SuggestedRepliesDirection
import com.sendbird.uikit.databinding.SbViewSuggestedRepliesComponentBinding
import com.sendbird.uikit.interfaces.OnItemClickListener

internal class SuggestedRepliesView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    var onItemClickListener: OnItemClickListener<String>? = null
    private var suggestedRepliesAdapter: SuggestedRepliesAdapter? = null
    override val binding: SbViewSuggestedRepliesComponentBinding = SbViewSuggestedRepliesComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )

    override val layout: View
        get() = binding.root

    init {
        binding.rvSuggestedReplies.layoutManager = LinearLayoutManager(context)
        val spacing = resources.getDimensionPixelSize(R.dimen.sb_size_8)
        binding.rvSuggestedReplies.addItemDecoration(LinearLayoutManagerItemDecoration(spacing))
        binding.rvSuggestedReplies.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    fun drawSuggestedReplies(message: BaseMessage, direction: SuggestedRepliesDirection) {
        val isDirectionChanged = suggestedRepliesAdapter?.direction != direction
        val adapter = suggestedRepliesAdapter?.takeIf { it.direction == direction } ?: SuggestedRepliesAdapter(direction).also {
            this.suggestedRepliesAdapter = it
            binding.rvSuggestedReplies.adapter = it
        }

        adapter.onItemClickListener = OnItemClickListener { v, position, data ->
            onItemClickListener?.onItemClick(v, position, data)
        }

        adapter.suggestedReplies = message.suggestedReplies

        if (isDirectionChanged) {
            val layoutManager = binding.rvSuggestedReplies.layoutManager as? LinearLayoutManager
            val layoutParams = layoutParams as ConstraintLayout.LayoutParams
            when (direction) {
                SuggestedRepliesDirection.VERTICAL -> {
                    layoutManager?.orientation = LinearLayoutManager.VERTICAL

                    binding.rvSuggestedReplies.setPaddingRelative(
                        0,
                        binding.rvSuggestedReplies.paddingTop,
                        binding.rvSuggestedReplies.paddingEnd,
                        binding.rvSuggestedReplies.paddingBottom
                    )

                    layoutParams.setMargins(
                        resources.getDimensionPixelSize(R.dimen.sb_size_42),
                        resources.getDimensionPixelSize(R.dimen.sb_size_20),
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin
                    )
                }
                SuggestedRepliesDirection.HORIZONTAL -> {
                    layoutManager?.orientation = LinearLayoutManager.HORIZONTAL

                    binding.rvSuggestedReplies.setPaddingRelative(
                        resources.getDimensionPixelSize(R.dimen.sb_size_38),
                        binding.rvSuggestedReplies.paddingTop,
                        binding.rvSuggestedReplies.paddingEnd,
                        binding.rvSuggestedReplies.paddingBottom
                    )

                    layoutParams.setMargins(
                        layoutParams.leftMargin,
                        resources.getDimensionPixelSize(R.dimen.sb_size_8),
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin
                    )
                }
            }
            binding.rvSuggestedReplies.clipToPadding = false
        }
    }

    private class LinearLayoutManagerItemDecoration(
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            if (parent.layoutManager is LinearLayoutManager) {
                val layoutManager = parent.layoutManager as LinearLayoutManager
                if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                    outRect.bottom = spacing
                } else {
                    outRect.right = spacing
                }
            }
        }
    }
}
