package com.sendbird.uikit.widgets;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewMessageInputBinding;
import com.sendbird.uikit.interfaces.OnInputModeChangedListener;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.utils.ViewUtils;

import java.lang.reflect.Field;

public class MessageInputView extends FrameLayout {
    private SbViewMessageInputBinding binding;

    private KeyboardDisplayType displayType = KeyboardDisplayType.Plane;
    private OnClickListener sendClickListener;
    private OnClickListener addClickListener;
    private OnClickListener editCancelClickListener;
    private OnClickListener editSaveClickListener;
    private OnClickListener replyCloseButtonClickListener;
    private OnInputTextChangedListener inputTextChangedListener;
    private OnInputTextChangedListener editModeTextChangedListener;
    private OnInputModeChangedListener inputModeChangedListener;
    private Mode mode = Mode.DEFAULT;
    private int addButtonVisibility = VISIBLE;
    private boolean showSendButtonAlways;
    private boolean useOverlay = false;

    public enum Mode {
        /**
         * A mode to be able to send a message normally.
         */
        DEFAULT,

        /**
         * A mode to edit current message.
         */
        EDIT,

        /**
         * A mode to send a reply message about current message.
         */
        QUOTE_REPLY
    }

    public MessageInputView(@NonNull Context context) {
        this(context, null);
    }

    public MessageInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageInputComponent, defStyleAttr, 0);
        try {
            this.binding = SbViewMessageInputBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int backgroundId = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_background, R.color.background_50);
            int textBackgroundId = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_text_background, R.drawable.sb_message_input_text_background_light);
            int textAppearance = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_text_appearance, R.style.SendbirdBody3OnLight01);
            String hint = a.getString(R.styleable.MessageInputComponent_sb_message_input_text_hint);
            ColorStateList hintColor = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_text_hint_color);
            int textCursorDrawable = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_text_cursor_drawable, R.drawable.sb_message_input_cursor_light);

            int leftButtonIcon = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_left_button_icon, R.drawable.icon_add);
            ColorStateList leftButtonTint = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_left_button_tint);
            int leftButtonBackground = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_left_button_background, R.drawable.sb_button_uncontained_background_light);
            int rightButtonIcon = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_right_button_icon, R.drawable.icon_send);
            ColorStateList rightButtonTint = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_right_button_tint);
            int rightButtonBackground = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_right_button_background, R.drawable.sb_button_uncontained_background_light);
            int editSaveButtonTextAppearance = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_edit_save_button_text_appearance, R.style.SendbirdButtonOnDark01);
            ColorStateList editSaveButtonTextColor = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_edit_save_button_text_color);
            int editSaveButtonBackground = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_edit_save_button_background, R.drawable.sb_button_contained_background_light);
            int editCancelButtonTextAppearance = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_text_appearance, R.style.SendbirdButtonPrimary300);
            ColorStateList editCancelButtonTextColor = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_text_color);
            int editCancelButtonBackground = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_background, R.drawable.sb_button_uncontained_background_light);

            int replyTitleAppearance = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_quote_reply_title_text_appearance, R.style.SendbirdCaption1OnLight01);
            int replyMessageAppearance = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_quoted_message_text_appearance, R.style.SendbirdCaption2OnLight03);
            int replyRightButtonIcon = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon, R.drawable.icon_close);
            ColorStateList replyRightButtonTint = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon_tint);
            int replyRightButtonBackground = a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon_background, R.drawable.sb_button_uncontained_background_light);

            binding.messageInputParent.setBackgroundResource(backgroundId);
            binding.etInputText.setBackgroundResource(textBackgroundId);
            binding.etInputText.setTextAppearance(context, textAppearance);
            if (hint != null) {
                setInputTextHint(hint);
            }
            if (hintColor != null) {
                binding.etInputText.setHintTextColor(hintColor);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.etInputText.setTextCursorDrawable(textCursorDrawable);
            } else {
                Field f = TextView.class.getDeclaredField(StringSet.mCursorDrawableRes);
                f.setAccessible(true);
                f.set(binding.etInputText, textCursorDrawable);
            }

            setEnabled(true);

            binding.ibtnAdd.setBackgroundResource(leftButtonBackground);
            setAddImageResource(leftButtonIcon);
            binding.ibtnAdd.setImageTintList(leftButtonTint);
            binding.ibtnSend.setBackgroundResource(rightButtonBackground);
            setSendImageResource(rightButtonIcon);
            binding.ibtnSend.setImageTintList(rightButtonTint);
            binding.btnSave.setTextAppearance(context, editSaveButtonTextAppearance);
            if (editSaveButtonTextColor != null) {
                binding.btnSave.setTextColor(editSaveButtonTextColor);
            }
            binding.btnSave.setBackgroundResource(editSaveButtonBackground);
            binding.btnCancel.setTextAppearance(context, editCancelButtonTextAppearance);
            if (editCancelButtonTextColor != null) {
                binding.btnCancel.setTextColor(editCancelButtonTextColor);
            }
            binding.btnCancel.setBackgroundResource(editCancelButtonBackground);

            binding.ivQuoteReplyMessageImage.setRadius(getResources().getDimensionPixelSize(R.dimen.sb_size_8));
            binding.tvQuoteReplyTitle.setTextAppearance(context, replyTitleAppearance);
            binding.tvQuoteReplyMessage.setTextAppearance(context, replyMessageAppearance);
            binding.ivQuoteReplyClose.setImageResource(replyRightButtonIcon);
            binding.ivQuoteReplyClose.setImageTintList(replyRightButtonTint);
            binding.ivQuoteReplyClose.setBackgroundResource(replyRightButtonBackground);
            final int dividerColor = SendbirdUIKit.isDarkMode() ? R.color.ondark_04 : R.color.onlight_04;
            binding.ivReplyDivider.setBackgroundColor(getResources().getColor(dividerColor));
            binding.etInputText.setOnClickListener(v -> showKeyboard());

            binding.etInputText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (!TextUtils.isEmpty(s) && Mode.EDIT != getInputMode() || showSendButtonAlways) {
                        setSendButtonVisibility(View.VISIBLE);
                    } else {
                        setSendButtonVisibility(View.GONE);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (editModeTextChangedListener != null && Mode.EDIT == getInputMode()) {
                        editModeTextChangedListener.onInputTextChanged(s, start, before, count);
                    }
                    if (inputTextChangedListener != null && Mode.EDIT != getInputMode()) {
                        inputTextChangedListener.onInputTextChanged(s, start, before, count);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!TextUtils.isEmpty(s) && Mode.EDIT != getInputMode() || showSendButtonAlways) {
                        setSendButtonVisibility(View.VISIBLE);
                    } else {
                        setSendButtonVisibility(View.GONE);
                    }
                }
            });
            binding.etInputText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            a.recycle();
        }
    }

    public void setInputMode(@NonNull final Mode mode) {
        final Mode before = this.mode;
        this.mode = mode;
        if (Mode.EDIT == mode) {
            setQuoteReplyPanelVisibility(GONE);
            setEditPanelVisibility(VISIBLE);
            binding.ibtnAdd.setVisibility(GONE);
        } else if (Mode.QUOTE_REPLY == mode) {
            setQuoteReplyPanelVisibility(VISIBLE);
            setEditPanelVisibility(GONE);
            setAddButtonVisibility(addButtonVisibility);
        } else {
            setQuoteReplyPanelVisibility(GONE);
            setEditPanelVisibility(GONE);
            setAddButtonVisibility(addButtonVisibility);
        }

        if (inputModeChangedListener != null) {
            inputModeChangedListener.onInputModeChanged(before, mode);
        }
    }

    public void showKeyboard() {
        if (displayType == KeyboardDisplayType.Dialog) {
            showInputDialog();
        } else {
            SoftInputUtils.showSoftKeyboard(binding.etInputText);
        }
    }

    public void drawMessageToReply(@NonNull BaseMessage message) {
        String displayMessage = message.getMessage();
        if (message instanceof FileMessage) {
            final FileMessage fileMessage = (FileMessage) message;
            ViewUtils.drawFileMessageIconToReply(binding.ivQuoteReplyMessageIcon, fileMessage);
            ViewUtils.drawThumbnail(binding.ivQuoteReplyMessageImage, fileMessage);
            binding.ivQuoteReplyMessageIcon.setVisibility(VISIBLE);
            binding.ivQuoteReplyMessageImage.setVisibility(VISIBLE);

            if (fileMessage.getType().contains(StringSet.gif)) {
                displayMessage = StringSet.gif.toUpperCase();
            } else if (fileMessage.getType().startsWith(StringSet.image)) {
                displayMessage = TextUtils.capitalize(StringSet.photo);
            } else if (fileMessage.getType().startsWith(StringSet.video)) {
                displayMessage = TextUtils.capitalize(StringSet.video);
            } else if (fileMessage.getType().startsWith(StringSet.audio)) {
                displayMessage = TextUtils.capitalize(StringSet.audio);
            } else {
                displayMessage = fileMessage.getName();
            }
        } else {
            binding.ivQuoteReplyMessageIcon.setVisibility(GONE);
            binding.ivQuoteReplyMessageImage.setVisibility(GONE);
        }
        if (null != message.getSender()) {
            binding.tvQuoteReplyTitle.setText(String.format(getContext().getString(R.string.sb_text_reply_to), message.getSender().getNickname()));
        }
        binding.tvQuoteReplyMessage.setText(displayMessage);
    }

    @NonNull
    public SbViewMessageInputBinding getBinding() {
        return binding;
    }

    @NonNull
    public View getLayout() {
        return this;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        binding.ibtnAdd.setEnabled(enabled);
        binding.etInputText.setEnabled(enabled);
        binding.ibtnSend.setEnabled(enabled);
    }

    public void setSendButtonVisibility(int visibility) {
        binding.ibtnSend.setVisibility(visibility);
    }

    public void setOnSendClickListener(@Nullable OnClickListener sendClickListener) {
        this.sendClickListener = sendClickListener;
        binding.ibtnSend.setOnClickListener(sendClickListener);
    }

    public void setSendImageResource(@DrawableRes int sendImageResource) {
        binding.ibtnSend.setImageResource(sendImageResource);
    }

    public void setSendImageDrawable(@Nullable Drawable drawable) {
        binding.ibtnSend.setImageDrawable(drawable);
    }

    public void setSendImageButtonTint(@Nullable ColorStateList tint) {
        binding.ibtnSend.setImageTintList(tint);
    }

    public void showSendButtonAlways(boolean always) {
        showSendButtonAlways = always;
    }

    public void setAddButtonVisibility(int visibility) {
        addButtonVisibility = visibility;
        binding.ibtnAdd.setVisibility(visibility);
    }

    public void setOnInputModeChangedListener(@NonNull OnInputModeChangedListener inputModeChangedListener) {
        this.inputModeChangedListener = inputModeChangedListener;
    }

    public void setOnAddClickListener(@Nullable OnClickListener addClickListener) {
        this.addClickListener = addClickListener;
        binding.ibtnAdd.setOnClickListener(addClickListener);
    }

    public void setAddImageResource(@DrawableRes int addImageResource) {
        binding.ibtnAdd.setImageResource(addImageResource);
    }

    public void setAddImageDrawable(@Nullable Drawable drawable) {
        binding.ibtnAdd.setImageDrawable(drawable);
    }

    public void setAddImageButtonTint(@Nullable ColorStateList tint) {
        binding.ibtnAdd.setImageTintList(tint);
    }

    public void setEditPanelVisibility(int visibility) {
        binding.editPanel.setVisibility(visibility);
    }

    public void setQuoteReplyPanelVisibility(int visibility) {
        binding.quoteReplyPanel.setVisibility(visibility);
        binding.ivReplyDivider.setVisibility(visibility);
    }

    public void setOnEditCancelClickListener(@Nullable OnClickListener editCancelClickListener) {
        this.editCancelClickListener = editCancelClickListener;
        binding.btnCancel.setOnClickListener(editCancelClickListener);
    }

    public void setOnEditSaveClickListener(@Nullable OnClickListener editSaveClickListener) {
        this.editSaveClickListener = editSaveClickListener;
        binding.btnSave.setOnClickListener(editSaveClickListener);
    }

    public void setOnReplyCloseClickListener(@Nullable OnClickListener replyCloseButtonClickListener) {
        this.replyCloseButtonClickListener = replyCloseButtonClickListener;
        binding.ivQuoteReplyClose.setOnClickListener(replyCloseButtonClickListener);
    }

    public void setOnInputTextChangedListener(@Nullable OnInputTextChangedListener inputTextChangedListener) {
        this.inputTextChangedListener = inputTextChangedListener;
    }

    public void setOnEditModeTextChangedListener(@Nullable OnInputTextChangedListener inputTextChangedListener) {
        this.editModeTextChangedListener = inputTextChangedListener;
    }

    public void setInputText(@Nullable CharSequence text) {
        binding.etInputText.setText(text);
        if (text != null) {
            binding.etInputText.setSelection(text.length());
        }
    }

    public void setUseOverlay(boolean useOverlay) {
        this.useOverlay = useOverlay;
    }

    @Nullable
    public String getInputText() {
        Editable editable = binding.etInputText.getText();
        return editable != null ? editable.toString().trim() : null;
    }

    public void setInputTextHint(@Nullable CharSequence hint) {
        binding.etInputText.setHint(hint);
    }

    @NonNull
    public EditText getInputEditText() {
        return binding.etInputText;
    }

    public void setKeyboardDisplayType(@NonNull KeyboardDisplayType displayType) {
        this.displayType = displayType;
    }

    @NonNull
    public Mode getInputMode() {
        return this.mode;
    }

    private void showInputDialog() {
        MessageInputView messageInputView = createDialogInputView();

        int themeResId;
        if (useOverlay) {
            themeResId = R.style.Widget_Sendbird_Overlay_DialogView;
        } else {
            themeResId = SendbirdUIKit.isDarkMode() ? R.style.Widget_Sendbird_Dark_DialogView : R.style.Widget_Sendbird_DialogView;
        }
        final Context themeWrapperContext = new ContextThemeWrapper(getContext(), themeResId);
        final DialogView dialogView = new DialogView(themeWrapperContext);
        dialogView.setContentView(messageInputView);
        dialogView.setBackgroundBottom();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Sendbird_Dialog_Bottom);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        final Context context = messageInputView.getContext();
        final int prevSoftInputMode = SoftInputUtils.getSoftInputMode(context);
        SoftInputUtils.setSoftInputMode(context, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        if (sendClickListener != null) {
            messageInputView.setOnSendClickListener(v -> {
                dialog.dismiss();
                binding.ibtnSend.postDelayed(() -> {
                    sendClickListener.onClick(binding.ibtnSend);
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode);
                }, 200);
            });
        }

        if (addClickListener != null) {
            messageInputView.setOnAddClickListener(v -> {
                dialog.dismiss();
                binding.ibtnAdd.postDelayed(() -> {
                    addClickListener.onClick(binding.ibtnAdd);
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode);
                }, 200);
            });
        }

        if (editSaveClickListener != null) {
            messageInputView.setOnEditSaveClickListener(v -> {
                setInputText(messageInputView.getInputText());
                dialog.dismiss();
                binding.btnSave.postDelayed(() -> {
                    editSaveClickListener.onClick(binding.btnSave);
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode);
                }, 200);
            });
        }

        if (editCancelClickListener != null) {
            messageInputView.setOnEditCancelClickListener(v -> {
                dialog.dismiss();
                binding.btnCancel.postDelayed(() -> {
                    editCancelClickListener.onClick(binding.btnCancel);
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode);
                }, 200);
            });
        }

        if (replyCloseButtonClickListener != null) {
            messageInputView.setOnReplyCloseClickListener(v -> {
                dialog.dismiss();
                binding.ivQuoteReplyClose.postDelayed(() -> {
                    replyCloseButtonClickListener.onClick(binding.ivQuoteReplyClose);
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode);
                }, 200);
            });
        }

        messageInputView.setOnInputTextChangedListener((s, start, before, count) -> {
            if (editModeTextChangedListener != null && Mode.EDIT == getInputMode()) {
                editModeTextChangedListener.onInputTextChanged(s, start, before, count);
            }
            if (inputTextChangedListener != null && Mode.EDIT != getInputMode()) {
                inputTextChangedListener.onInputTextChanged(s, start, before, count);
            }
            if (Mode.EDIT != getInputMode()) {
                setInputText(s.toString());
            }
        });

        dialog.setOnCancelListener(dialog1 -> {
            setInputMode(Mode.DEFAULT);
            binding.getRoot().postDelayed(() -> SoftInputUtils.setSoftInputMode(context, prevSoftInputMode), 200);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
        messageInputView.showKeyboard();
    }

    @NonNull
    private MessageInputView createDialogInputView() {
        final MessageInputView messageInputView = new MessageInputView(getContext());
        if (showSendButtonAlways) messageInputView.setSendButtonVisibility(VISIBLE);
        messageInputView.showSendButtonAlways(showSendButtonAlways);

        messageInputView.setInputMode(mode);
        if (Mode.EDIT == mode) {
            messageInputView.setInputText(getInputText());
        } else if (Mode.QUOTE_REPLY == mode) {
            messageInputView.getBinding().ivQuoteReplyMessageIcon.setVisibility(binding.ivQuoteReplyMessageIcon.getVisibility());
            messageInputView.getBinding().ivQuoteReplyMessageImage.setVisibility(binding.ivQuoteReplyMessageImage.getVisibility());
            messageInputView.getBinding().ivQuoteReplyMessageIcon.setImageDrawable(binding.ivQuoteReplyMessageIcon.getDrawable());
            messageInputView.getBinding().ivQuoteReplyMessageImage.getContent().setImageDrawable(binding.ivQuoteReplyMessageImage.getContent().getDrawable());
            messageInputView.getBinding().tvQuoteReplyTitle.setText(binding.tvQuoteReplyTitle.getText());
            messageInputView.getBinding().tvQuoteReplyMessage.setText(binding.tvQuoteReplyMessage.getText());
        }

        messageInputView.getBinding().ibtnSend.setImageDrawable(binding.ibtnSend.getDrawable());
        messageInputView.getBinding().ibtnAdd.setImageDrawable(binding.ibtnAdd.getDrawable());
        messageInputView.getBinding().etInputText.setHint(binding.etInputText.getHint());

        return messageInputView;
    }
}
