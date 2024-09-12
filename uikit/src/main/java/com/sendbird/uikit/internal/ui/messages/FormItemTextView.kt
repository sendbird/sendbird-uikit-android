package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.sendbird.android.message.MessageFormItem
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.databinding.SbViewFormItemTextComponentBinding
import com.sendbird.uikit.internal.extensions.shouldCheckValidation
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.interfaces.OnFormValidationChangedListener
import java.util.concurrent.atomic.AtomicBoolean

internal class FormItemTextView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewFormItemTextComponentBinding = SbViewFormItemTextComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )
    override val layout: View
        get() = binding.root

    private val etFormItemBackground = if (SendbirdUIKit.isDarkMode()) {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_item_normal_dark, null)
    } else {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_item_normal_light, null)
    }

    private val etFormItemFocusedBackground = if (SendbirdUIKit.isDarkMode()) {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_item_focused_dark, null)
    } else {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_item_focused_light, null)
    }

    private val etFormItemBackgroundError = if (SendbirdUIKit.isDarkMode()) {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_item_invalid_dark, null)
    } else {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_item_invalid_light, null)
    }

    private val tvFormItemTitleOptionalAppearance = if (SendbirdUIKit.isDarkMode()) {
        R.style.SendbirdCaption3OnDark03
    } else {
        R.style.SendbirdCaption3OnLight03
    }

    private var textWatcher: FormItemTextWatcher? = null
    private var isValidationChecked: AtomicBoolean = AtomicBoolean(false)
    private var messageFormItem: MessageFormItem? = null

    var onValidationListener: OnFormValidationChangedListener? = null

    init {
        val isDarkMode = SendbirdUIKit.isDarkMode()
        binding.tvFormItemTitle.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption3OnDark02
            else R.style.SendbirdCaption3OnLight02
        )

        binding.etFormItem.background = etFormItemBackground

        binding.etFormItem.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdBody3OnDark01
            else R.style.SendbirdBody3OnLight01
        )

        binding.etFormItem.setHintTextColor(
            ContextCompat.getColor(context, if (isDarkMode) R.color.ondark_text_low_emphasis else R.color.onlight_text_low_emphasis)
        )

        binding.tvFormItemError.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption4Error200
            else R.style.SendbirdCaption4Error300
        )

        binding.answeredLayout.background = if (isDarkMode) {
            ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_round_rect_background_onlight_04, null)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_round_rect_background_ondark_02, null)
        }
        binding.iconDone.setColorFilter(
            ContextCompat.getColor(context, if (isDarkMode) R.color.secondary_main else R.color.secondary_light)
        )

        binding.etAnswer.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdBody3OnDark01
            else R.style.SendbirdBody3OnLight01
        )

        binding.etAnswer.setHintTextColor(
            ContextCompat.getColor(context, if (isDarkMode) R.color.ondark_text_low_emphasis else R.color.onlight_text_low_emphasis)
        )

        binding.etAnswer.background = null
    }

    fun drawFormItem(messageFormItem: MessageFormItem, isEnabled: Boolean, shouldCheckValidation: Boolean?) {
        this.messageFormItem = messageFormItem
        textWatcher?.let { binding.etFormItem.removeTextChangedListener(it) }
        textWatcher = null
        binding.etFormItem.onFocusChangeListener = null
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

        if (!isEnabled) {
            binding.unansweredLayout.visibility = GONE
            binding.answeredLayout.visibility = VISIBLE
            val hasResponse = !(messageFormItem.submittedValues.isNullOrEmpty())
            if (hasResponse) {
                binding.etAnswer.setText(messageFormItem.submittedValues?.firstOrNull() ?: "")
            } else {
                binding.etAnswer.hint = context.getString(R.string.sb_forms_empty_response)
            }
            updateFormItemState(true)
        } else {
            binding.unansweredLayout.visibility = VISIBLE
            binding.answeredLayout.visibility = GONE
            binding.etFormItem.transformationMethod = null
            binding.etFormItem.setText(messageFormItem.draftValues?.firstOrNull() ?: "")
            textWatcher = FormItemTextWatcher(messageFormItem).also {
                binding.etFormItem.addTextChangedListener(it)
            }
            binding.etFormItem.onFocusChangeListener = FormItemFocusChangeListener(messageFormItem)
            messageFormItem.placeholder?.let { binding.etFormItem.hint = it }
            updateFormItemState(shouldCheckValidation ?: true)
        }
    }

    private fun updateFormItemState(isValid: Boolean) {
        onValidationListener?.onValidationChanged(isValid)
        if (!isValid) {
            isValidationChecked.set(true)
            binding.etFormItem.background = etFormItemBackgroundError
            val hasResponse = binding.etFormItem.text.toString().isNotEmpty()
            if (messageFormItem?.required == true && !hasResponse) {
                binding.tvFormItemError.text = context.getString(R.string.sb_forms_required_form_item)
            } else {
                binding.tvFormItemError.text = context.getString(R.string.sb_forms_invalid_form_item)
            }
            binding.tvFormItemError.visibility = VISIBLE
        } else {
            binding.etFormItem.background = if (binding.etFormItem.hasFocus()) etFormItemFocusedBackground else etFormItemBackground
            binding.tvFormItemError.visibility = GONE
        }
    }

    internal fun setDraftValues(inputValue: String, isValidationChecked: Boolean, formItem: MessageFormItem) {
        if (inputValue.isEmpty()) {
            formItem.draftValues = null
            formItem.shouldCheckValidation = null
            updateFormItemState(true)
            return
        }
        if (isValidationChecked) {
            val isValid = formItem.isValid(inputValue)
            formItem.shouldCheckValidation = isValid
            updateFormItemState(isValid)
        }
        formItem.draftValues = listOf(inputValue)
    }

    private inner class FormItemTextWatcher(
        private val formItem: MessageFormItem
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            setDraftValues(s.toString(), isValidationChecked.get(), formItem)
        }
    }

    private inner class FormItemFocusChangeListener(
        private val formItem: MessageFormItem
    ) : OnFocusChangeListener {
        override fun onFocusChange(v: View?, hasFocus: Boolean) {
            if (hasFocus) {
                v?.background = etFormItemFocusedBackground
            } else {
                isValidationChecked.set(true)
                val text = binding.etFormItem.text.toString()
                setDraftValues(text, true, formItem)
            }
        }
    }
}
