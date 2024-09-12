package com.sendbird.uikit.activities.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.message.MessageForm
import com.sendbird.android.message.MessageFormItem
import com.sendbird.uikit.databinding.SbViewFormItemTextBinding
import com.sendbird.uikit.databinding.SbViewFormItemChipBinding
import com.sendbird.uikit.databinding.SbViewFormItemTextareaBinding
import com.sendbird.uikit.internal.extensions.convertToViewType
import com.sendbird.uikit.internal.extensions.isEqualTo
import com.sendbird.uikit.internal.extensions.isSubmittable
import com.sendbird.uikit.internal.extensions.shouldCheckValidation
import com.sendbird.uikit.internal.interfaces.OnFormValidationChangedListener

internal class FormItemAdapter(private val onValidationChangedListener: OnFormValidationChangedListener) : ListAdapter<MessageFormItem, FormItemAdapter.MessageFormItemViewHolder>(diffUtil) {
    private var messageForm: MessageForm? = null
    private var validations: MutableList<Boolean>? = null

    fun isSubmittable(): Boolean {
        return currentList.all { messageFormItem ->
            messageFormItem.isSubmittable
        }
    }

    fun updateValidation() {
        currentList.forEachIndexed { index, messageFormItem ->
            val lastValidation = messageFormItem.shouldCheckValidation
            val validation = messageFormItem.isSubmittable
            messageFormItem.shouldCheckValidation = validation
            if (lastValidation != validation) {
                notifyItemChanged(index)
            }
        }
    }

    fun setMessageForm(form: MessageForm) {
        messageForm = form
        validations = MutableList(form.items.size) { true }
        submitList(form.items)
    }

    private fun updateValidation(index: Int, isValid: Boolean) {
        validations?.set(index, isValid)
        onValidationChangedListener.onValidationChanged(validations?.all { it } == true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageFormItemViewHolder {
        return when (viewType) {
            MessageFormViewType.TEXT.value -> FormItemTextViewHolder(
                SbViewFormItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            MessageFormViewType.TEXTAREA.value -> FormItemTextareaViewHolder(
                SbViewFormItemTextareaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            MessageFormViewType.CHIP.value -> FormItemChipViewHolder(
                SbViewFormItemChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> FormItemTextViewHolder(
                SbViewFormItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).style?.layout?.convertToViewType() ?: MessageFormViewType.TEXT.value
    }

    override fun onBindViewHolder(holder: MessageFormItemViewHolder, position: Int) {
        holder.bind(getItem(position), messageForm?.isSubmitted == false) {
            updateValidation(position, it)
        }
    }

    private class FormItemTextViewHolder(
        val binding: SbViewFormItemTextBinding
    ) : MessageFormItemViewHolder(binding.root) {
        override fun bind(item: MessageFormItem, isEnabled: Boolean, onValidationChangedListener: OnFormValidationChangedListener) {
            binding.formItemView.onValidationListener = onValidationChangedListener
            binding.formItemView.drawFormItem(item, isEnabled, item.shouldCheckValidation)
        }
    }

    private class FormItemTextareaViewHolder(
        val binding: SbViewFormItemTextareaBinding
    ) : MessageFormItemViewHolder(binding.root) {
        override fun bind(item: MessageFormItem, isEnabled: Boolean, onValidationChangedListener: OnFormValidationChangedListener) {
            binding.formItemView.onValidationListener = onValidationChangedListener
            binding.formItemView.drawFormItem(item, isEnabled, item.shouldCheckValidation)
        }
    }

    private class FormItemChipViewHolder(
        val binding: SbViewFormItemChipBinding
    ) : MessageFormItemViewHolder(binding.root) {
        override fun bind(item: MessageFormItem, isEnabled: Boolean, onValidationChangedListener: OnFormValidationChangedListener) {
            binding.formItemView.onValidationListener = onValidationChangedListener
            binding.formItemView.drawFormItem(item, isEnabled, item.shouldCheckValidation)
        }
    }

    abstract class MessageFormItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: MessageFormItem, isEnabled: Boolean, onValidationChangedListener: OnFormValidationChangedListener)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MessageFormItem>() {
            override fun areItemsTheSame(oldItem: MessageFormItem, newItem: MessageFormItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MessageFormItem, newItem: MessageFormItem): Boolean {
                return oldItem.draftValues isEqualTo newItem.draftValues &&
                    oldItem.submittedValues isEqualTo newItem.submittedValues &&
                    !(oldItem.required == false && newItem.required == false)
            }
        }
    }
}

internal enum class MessageFormViewType(val value: Int) {
    TEXT(0),
    TEXTAREA(1),
    CHIP(2),
    UNKNOWN(3)
}
