package com.sendbird.uikit.internal.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.SizeType
import com.sendbird.uikit.internal.model.template_messages.ViewLifecycleHandler
import com.sendbird.uikit.internal.ui.messages.MessageTemplateView

internal class CarouselChildViewAdapter(private val maxChildWidth: Int) : RecyclerView.Adapter<CarouselChildViewAdapter.CarouselChildItemViewHolder>() {
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
            contentView.maxWidth = maxChildWidth

            // If width is fill, the width of the parent is not fixed, so we can't set the value because there is no value to base it on. Therefore, we set the maximum size of the parent child item.
            val hasFillWidth = params.hasFillWidth
            val width = if (hasFillWidth) {
                maxChildWidth
            } else {
                LayoutParams.WRAP_CONTENT
            }

            contentView.layoutParams = contentView.layoutParams.apply {
                this.width = width
            }
            contentView.draw(
                params,
                onViewCreated = { view, viewParams -> onChildViewCreated?.invoke(view, viewParams) }
            )
        }

        private val Params.hasFillWidth: Boolean
            get() {
                return this.body.items.any { it.width.type == SizeType.Flex && it.width.value == ViewGroup.LayoutParams.MATCH_PARENT }
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
