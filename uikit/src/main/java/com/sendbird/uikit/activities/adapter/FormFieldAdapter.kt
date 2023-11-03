package com.sendbird.uikit.activities.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.databinding.SbViewFormFieldBinding
import com.sendbird.uikit.internal.model.Form
import com.sendbird.uikit.internal.model.FormField

internal class FormFieldAdapter : BaseAdapter<FormField, BaseViewHolder<FormField>>() {
    private val formFields: MutableList<FormField> = mutableListOf()

    fun isReadyToSubmit(): Boolean {
        return formFields.all { it.isReadyToSubmit() }
    }

    fun updateValidation() {
        formFields.forEachIndexed { index, formField ->
            val lastValidation = formField.lastValidation
            val validation = formField.isReadyToSubmit()
            formField.lastValidation = validation
            if (lastValidation != validation) {
                notifyItemChanged(index)
            }
        }
    }

    fun setFormFields(form: Form) {
        val newFormFields = if (form.isAnswered) {
            form.formFields.filter { it.answer != null }
        } else {
            form.formFields
        }
        val diffResult = DiffUtil.calculateDiff(FormFieldDiffCallback(formFields, newFormFields))
        formFields.clear()
        formFields.addAll(newFormFields)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<FormField> {
        return FormFieldViewHolder(
            SbViewFormFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return formFields.size
    }

    override fun getItem(position: Int): FormField {
        return formFields[position]
    }

    override fun getItems(): List<FormField> {
        return formFields.toList()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FormField>, position: Int) {
        holder.bind(getItem(position))
    }

    internal class FormFieldViewHolder(
        val binding: SbViewFormFieldBinding
    ) : BaseViewHolder<FormField>(binding.root) {
        override fun bind(item: FormField) {
            binding.formFieldView.drawFormField(item)
        }
    }

    private class FormFieldDiffCallback(
        private val oldList: List<FormField>,
        private val newList: List<FormField>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.formFieldKey == newItem.formFieldKey &&
                oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
