package com.sendbird.uikit.internal.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.uikit.R
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.SizeType
import com.sendbird.uikit.internal.model.template_messages.ViewLifecycleHandler
import com.sendbird.uikit.internal.ui.messages.MessageTemplateView

internal class CarouselChildViewAdapter : RecyclerView.Adapter<CarouselChildViewAdapter.CarouselChildItemViewHolder>() {
    private val childTemplateParams: MutableList<Params> = mutableListOf()
    internal var onChildViewCreated: ViewLifecycleHandler? = null

    fun setChildTemplateParams(newParams: List<Params>) {
        val oldParams = childTemplateParams.toList()
        val diffResult = DiffUtil.calculateDiff(
            ParamsDiffCallback(oldParams, newParams)
        )
        childTemplateParams.clear()
        childTemplateParams.addAll(newParams)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselChildItemViewHolder {
        return CarouselChildItemViewHolder(parent.context)
    }

    override fun getItemCount(): Int {
        return childTemplateParams.size
    }

    override fun onBindViewHolder(holder: CarouselChildItemViewHolder, position: Int) {
        holder.bind(childTemplateParams[position])
    }

    inner class CarouselChildItemViewHolder(
        context: Context,
        private val contentView: MessageTemplateView = MessageTemplateView(
            context,
            autoAdjustHeightWhenInvisible = false
        )
    ) : RecyclerView.ViewHolder(contentView) {
        fun bind(params: Params) {
            val maxChildFixedWidthSize = params.maxChildFixedWidthSize
            val width = if (maxChildFixedWidthSize != null) {
                contentView.context.resources.intToDp(maxChildFixedWidthSize)
            } else {
                contentView.context.resources.getDimensionPixelSize(R.dimen.sb_message_max_width)
            }

            contentView.layoutParams = contentView.layoutParams.apply {
                this.width = width
            }

            contentView.draw(
                params,
                onViewCreated = { view, viewParams -> onChildViewCreated?.invoke(view, viewParams) }
            )
        }

        private val Params.maxChildFixedWidthSize: Int?
            get() {
                return this.body.items
                    .filter { it.width.type == SizeType.Fixed }
                    .takeIf { it.isNotEmpty() }
                    ?.maxOf { it.width.value }
            }
    }

    private class ParamsDiffCallback(
        private val oldParams: List<Params>,
        private val newParams: List<Params>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldParams.size

        override fun getNewListSize(): Int = newParams.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldParams[oldItemPosition] == newParams[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldParams[oldItemPosition] == newParams[newItemPosition]
        }
    }
}
