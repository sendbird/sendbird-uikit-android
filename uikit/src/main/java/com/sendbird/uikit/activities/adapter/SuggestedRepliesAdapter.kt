package com.sendbird.uikit.activities.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.consts.SuggestedRepliesDirection
import com.sendbird.uikit.databinding.SbViewSuggestedReplyBinding
import com.sendbird.uikit.interfaces.OnItemClickListener

internal class SuggestedRepliesAdapter(
    internal val direction: SuggestedRepliesDirection
) : BaseAdapter<String, SuggestedRepliesAdapter.SuggestedReplyViewHolder>() {
    var onItemClickListener: OnItemClickListener<String>? = null
    var suggestedReplies: List<String> = listOf()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(SuggestedReplyDiffCallback(field, value))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedReplyViewHolder {
        return SuggestedReplyViewHolder(
            SbViewSuggestedReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ).apply {
            this.binding.root.updateLayoutParams {
                this.width = when (direction) {
                    SuggestedRepliesDirection.HORIZONTAL -> ViewGroup.LayoutParams.WRAP_CONTENT
                    SuggestedRepliesDirection.VERTICAL -> ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

            this.binding.suggestedReplyView.setOnClickListener {
                val item = getItem(absoluteAdapterPosition)
                val index = suggestedReplies.indexOf(item)
                if (index == -1) return@setOnClickListener
                onItemClickListener?.onItemClick(binding.root, index, item)
            }
        }
    }

    override fun onBindViewHolder(holder: SuggestedReplyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return suggestedReplies.size
    }

    override fun getItem(position: Int): String {
        return suggestedReplies[position]
    }

    override fun getItems(): List<String> {
        return suggestedReplies.toList()
    }

    internal class SuggestedReplyViewHolder(
        val binding: SbViewSuggestedReplyBinding
    ) : BaseViewHolder<String>(binding.root) {
        override fun bind(item: String) {
            binding.suggestedReplyView.drawSuggestedReplies(item)
        }
    }

    private class SuggestedReplyDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
