package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sendbird.uikit.R
import com.sendbird.uikit.databinding.SbViewMessageInputDialogBinding
import com.sendbird.uikit.databinding.SbViewMessageInputDialogHelperBinding
import com.sendbird.uikit.interfaces.OnInputModeChangedListener
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.utils.SoftInputUtils
import com.sendbird.uikit.widgets.MessageInputView


internal class MessageInputDialogWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : KeyboardDetectLayout(context, attrs, defStyle), OnKeyboardDetectListener {
    private val binding: SbViewMessageInputDialogHelperBinding
    private val dialogRootBinding: SbViewMessageInputDialogBinding
    private val dialogCustomView: FrameLayout
    private var messageInputView: MessageInputView? = null
    private val bottomSheetDialog: BottomSheetDialog

    init {
        binding = SbViewMessageInputDialogHelperBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        dialogRootBinding = SbViewMessageInputDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        dialogCustomView = dialogRootBinding.messageInputPanel
        bottomSheetDialog =
            BottomSheetDialog(context, R.style.Sendbird_BottomSheetDialogStyle_MessageInputDialogWrapper)
        bottomSheetDialog.setContentView(dialogRootBinding.root)
        bottomSheetDialog.setCanceledOnTouchOutside(true)
        setInputAlwaysHiddenWhenDialogDismissed()
        listener = this
    }

    private fun setInputAlwaysHiddenWhenDialogDismissed() {
        val mode = SoftInputUtils.getSoftInputMode(context)
        SoftInputUtils.setSoftInputMode(
            context,
            mode or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
    }

    fun initInputView(messageInputView: MessageInputView) {
        this.messageInputView = messageInputView
        deactivateInputView(messageInputView)
        setOnInputModeChangedListenerToDismissDialog(messageInputView)
        bottomSheetDialog.setOnDismissListener {
            deactivateInputView(messageInputView)
        }
    }

    override fun onKeyboardShown() {
        Logger.d(">> onKeyboardShown()")
        if (isInputViewActivated()) return
        messageInputView?.let { activateInputView(it) }
    }

    override fun onKeyboardHidden() {
        Logger.d(">> onKeyboardHidden()")
    }

    private fun isInputViewActivated(): Boolean {
        return bottomSheetDialog.isShowing
    }

    private fun activateInputView(messageInputView: MessageInputView) {
        setPillarView(messageInputView)
        detachInputViewFromParent(messageInputView)
        attachInputViewToDialog(messageInputView)
        bottomSheetDialog.show()
        SoftInputUtils.showSoftKeyboard(messageInputView.inputEditText)
    }

    private fun deactivateInputView(messageInputView: MessageInputView) {
        removePillarViews()
        detachInputViewFromParent(messageInputView)
        attachInputViewToDisplayView(messageInputView)
    }

    private fun setOnInputModeChangedListenerToDismissDialog(messageInputView: MessageInputView) {
        val defaultInputModeChangedListener = messageInputView.onInputModeChangedListener
        messageInputView.onInputModeChangedListener = OnInputModeChangedListener { before, current ->
            defaultInputModeChangedListener?.onInputModeChanged(before, current)
        }
    }

    private fun detachInputViewFromParent(messageInputView: MessageInputView) {
        if (messageInputView.parent == null) return
        (messageInputView.parent as ViewGroup).removeView(messageInputView)
    }

    private fun attachInputViewToDialog(messageInputView: MessageInputView) {
        dialogCustomView.addView(
            messageInputView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        messageInputView.inputEditText.requestFocus()
    }

    private fun attachInputViewToDisplayView(messageInputView: MessageInputView) {
        binding.contentView.addView(
            messageInputView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        messageInputView.inputEditText.requestFocus()
    }

    private fun setPillarView(messageInputView: MessageInputView) {
        removePillarViews()
        val view = View(context)
        view.layoutParams = LayoutParams(
            messageInputView.measuredWidth,
            messageInputView.measuredHeight
        )
        view.background = messageInputView
            .binding.messageInputParent.background
        binding.pillarView.addView(view)
    }

    private fun removePillarViews() {
        binding.pillarView.removeAllViews()
    }
}
