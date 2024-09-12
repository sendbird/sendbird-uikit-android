package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.sendbird.android.message.MessageFormItem
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.databinding.SbViewFormItemChipComponentBinding
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.extensions.shouldCheckValidation
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.interfaces.OnFormValidationChangedListener
import com.sendbird.uikit.internal.ui.widgets.TextChip

internal class FormItemChipView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewFormItemChipComponentBinding = SbViewFormItemChipComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )
    override val layout: View
        get() = binding.root

    private val tvFormItemTitleOptionalAppearance = if (SendbirdUIKit.isDarkMode()) {
        R.style.SendbirdCaption3OnDark03
    } else {
        R.style.SendbirdCaption3OnLight03
    }

    var onValidationListener: OnFormValidationChangedListener? = null

    init {
        val isDarkMode = SendbirdUIKit.isDarkMode()
        binding.tvFormItemTitle.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption3OnDark02
            else R.style.SendbirdCaption3OnLight02
        )

        binding.tvFormItemError.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption4Error200
            else R.style.SendbirdCaption4Error300
        )
    }

    fun drawFormItem(messageFormItem: MessageFormItem, isEnabled: Boolean, shouldCheckValidation: Boolean?) {
        val name = if (messageFormItem.required == true) {
            messageFormItem.name
        } else {
            val title = "${messageFormItem.name} ${context.getString(R.string.sb_forms_optional)}"
            SpannableString(title).apply {
                setSpan(
                    TextAppearanceSpan(context, tvFormItemTitleOptionalAppearance),
                    messageFormItem.name?.length ?: 0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        binding.tvFormItemTitle.text = name

        messageFormItem.style?.resultCount?.max?.let { max ->
            binding.chipGroupFormItem.isSingleSelection = max <= 1
        }

        val selectedItems = mutableListOf<String>()
        if (!isEnabled) {
            binding.chipGroupFormItem.setOnCheckedChangeListener(null)
            selectedItems.addAll(messageFormItem.submittedValues ?: emptyList())
            updateFormItemState(true)
        } else {
            binding.chipGroupFormItem.setOnCheckedChangeListener(FormItemOnCheckedStateChangeListener(messageFormItem))
            selectedItems.addAll(messageFormItem.draftValues ?: messageFormItem.style?.defaultOptions ?: emptyList())
            if (selectedItems.isNotEmpty()) {
                messageFormItem.draftValues = selectedItems
            }
            updateFormItemState(shouldCheckValidation ?: true)
        }

        binding.chipGroupFormItem.removeAllViews()
        messageFormItem.style?.options?.forEach {
            binding.chipGroupFormItem.addView(createChip(it, selectedItems, !isEnabled))
        }
    }

    private fun createChip(chipText: String, selectedItems: List<String>, isSubmitted: Boolean): Chip {
        val isSelected = selectedItems.contains(chipText)
        return TextChip(ContextThemeWrapper(context, R.style.Theme_MaterialComponents)).apply {
            text = chipText
            minHeight = resources.intToDp(32)
            isChipEnabled = !isSubmitted
            isChipSelected = isSelected
        }
    }

    private fun updateFormItemState(isValid: Boolean) {
        onValidationListener?.onValidationChanged(isValid)
        if (!isValid) {
            binding.tvFormItemError.visibility = VISIBLE
        } else {
            binding.tvFormItemError.visibility = GONE
        }
    }

    private inner class FormItemOnCheckedStateChangeListener(
        private val formItem: MessageFormItem
    ) : ChipGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(chipGroup: ChipGroup?, id: Int) {
            val checkedIds = chipGroup?.checkedChipIds
            formItem.draftValues = checkedIds?.map { "${findViewById<Chip>(it).text}" } ?: emptyList()
            val isValid = formItem.draftValues?.all { formItem.isValid(it) } ?: true
            formItem.shouldCheckValidation = isValid
            updateFormItemState(isValid)
        }
    }
}
