package com.sendbird.uikit.widgets;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.DialogListAdapter;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewDialogBinding;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;

import java.lang.reflect.Field;

public class DialogView extends LinearLayout {
    private SbViewDialogBinding binding;

    private int backgroundBottomId;
    private int backgroundAnchorId;
    private OnEditTextResultListener editTextResultListener;

    public DialogView(@NonNull Context context) {
        this(context, null);
    }

    public DialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialogView, defStyleAttr, 0);
        try {
            binding = SbViewDialogBinding.inflate(LayoutInflater.from(context), null, false);
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int backgroundId = a.getResourceId(R.styleable.DialogView_sb_dialog_view_background, R.drawable.sb_rounded_rectangle_light);
            backgroundBottomId = a.getResourceId(R.styleable.DialogView_sb_dialog_view_background_bottom, R.drawable.sb_top_rounded_rectangle_light);
            backgroundAnchorId = a.getResourceId(R.styleable.DialogView_sb_dialog_view_background_anchor, R.drawable.layer_dialog_anchor_background_light);
            int titleAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_title_appearance, R.style.SendbirdH1OnLight01);
            int messageAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_message_appearance, R.style.SendbirdSubtitle2OnLight01);
            int editTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_edit_text_appearance, R.style.SendbirdSubtitle2OnLight01);
            ColorStateList editTextTint = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_edit_text_tint);
            int editTextCursorDrawable = a.getResourceId(R.styleable.DialogView_sb_dialog_view_edit_text_cursor_drawable, R.drawable.sb_message_input_cursor_light);
            ColorStateList editTextHintColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_edit_text_hint_color);

            int positiveButtonTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_positive_button_text_appearance, R.style.SendbirdButtonPrimary300);
            ColorStateList positiveButtonTextColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_positive_button_text_color);
            int positiveButtonBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_positive_button_background, R.drawable.sb_button_uncontained_background_light);
            int negativeButtonTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_negative_button_text_appearance, R.style.SendbirdButtonPrimary300);
            ColorStateList negativeButtonTextColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_negative_button_text_color);
            int negativeButtonBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_negative_button_background, R.drawable.sb_button_uncontained_background_light);
            int neutralButtonTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_neutral_button_text_appearance, R.style.SendbirdButtonPrimary300);
            ColorStateList neutralButtonTextColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_neutral_button_text_color);
            int neutralButtonBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_neutral_button_background, R.drawable.sb_button_uncontained_background_light);

            binding.sbParentPanel.setBackgroundResource(backgroundId);

            binding.tvDialogTitle.setTextAppearance(context, titleAppearance);
            binding.tvDialogMessage.setTextAppearance(context, messageAppearance);

            binding.etInputText.setTextAppearance(context, editTextAppearance);
            binding.etInputText.setBackground(DrawableUtils.setTintList(binding.etInputText.getBackground(), editTextTint));
            if (editTextHintColor != null) {
                binding.etInputText.setHintTextColor(editTextHintColor);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.etInputText.setTextCursorDrawable(editTextCursorDrawable);
            } else {
                Field f = TextView.class.getDeclaredField(StringSet.mCursorDrawableRes);
                f.setAccessible(true);
                f.set(binding.etInputText, editTextCursorDrawable);
            }

            binding.btPositive.setTextAppearance(context, positiveButtonTextAppearance);
            if (positiveButtonTextColor != null) {
                binding.btPositive.setTextColor(positiveButtonTextColor);
            }
            binding.btPositive.setBackgroundResource(positiveButtonBackground);
            binding.btNegative.setTextAppearance(context, negativeButtonTextAppearance);
            if (negativeButtonTextColor != null) {
                binding.btNegative.setTextColor(negativeButtonTextColor);
            }
            binding.btNegative.setBackgroundResource(negativeButtonBackground);
            binding.btNeutral.setTextAppearance(context, neutralButtonTextAppearance);
            if (neutralButtonTextColor != null) {
                binding.btNeutral.setTextColor(neutralButtonTextColor);
            }
            binding.btNeutral.setBackgroundResource(neutralButtonBackground);
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            a.recycle();
        }
    }

    public void setTitle(int title) {
        if (title == 0) {
            return;
        }
        binding.tvDialogTitle.setText(title);
        binding.tvDialogTitle.setVisibility(VISIBLE);
    }

    public void setTitle(@Nullable CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        binding.tvDialogTitle.setText(title);
        binding.tvDialogTitle.setVisibility(VISIBLE);
    }

    public void setMessage(int message) {
        if (message == 0) {
            return;
        }
        binding.tvDialogMessage.setText(message);
        binding.tvDialogMessage.setVisibility(VISIBLE);
    }

    public void setEditText(@Nullable DialogEditTextParams params, @Nullable OnEditTextResultListener editTextResultListener) {
        if (params == null) {
            return;
        }

        binding.etInputText.setVisibility(VISIBLE);
        String hintText = params.getHintText();
        if (!TextUtils.isEmpty(hintText)) {
            binding.etInputText.setHint(hintText);
        }

        String text = params.getText();
        if (!TextUtils.isEmpty(text)) {
            binding.etInputText.setHint(text);
        }

        binding.etInputText.setSingleLine(params.enabledSingleLine());
        android.text.TextUtils.TruncateAt ellipsis = params.getEllipsis();
        if (ellipsis != null) {
            binding.etInputText.setEllipsize(ellipsis);
        }

        Editable data = binding.etInputText.getText();
        int selection = params.getSelection();
        if (selection > 0 && data != null) {
            if (data.length() > selection) {
                binding.etInputText.setSelection(selection);
            }
        }

        SoftInputUtils.showSoftKeyboard(binding.etInputText);
        this.editTextResultListener = editTextResultListener;
    }

    public void setItems(@Nullable DialogListItem[] items, @NonNull OnItemClickListener<DialogListItem> itemClickListener, boolean isLeft) {
        if (items == null) return;
        binding.rvSelectView.setAdapter(new DialogListAdapter(items, isLeft, itemClickListener) );
        binding.rvSelectView.setVisibility(VISIBLE);
    }

    public void setItems(@Nullable DialogListItem[] items, @NonNull OnItemClickListener<DialogListItem> itemClickListener, boolean isLeft, @DimenRes int nameMarginLeft) {
        if (items == null) return;
        DialogListAdapter adapter = new DialogListAdapter(items, isLeft, itemClickListener);
        adapter.setNameMarginLeft(nameMarginLeft);
        binding.rvSelectView.setAdapter(adapter);
        binding.rvSelectView.setVisibility(VISIBLE);
    }

    public void setPositiveButton(@Nullable String text, @ColorRes int textColor, @NonNull View.OnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        binding.btPositive.setText(text);
        if (textColor != 0) {
            binding.btPositive.setTextColor(AppCompatResources.getColorStateList(getContext(), textColor));
        }
        binding.btPositive.setOnClickListener(v -> {
            if (editTextResultListener != null) {
                editTextResultListener.onResult(getEditText());
            }

            clickListener.onClick(v);
        });
        binding.sbButtonPanel.setVisibility(VISIBLE);
        binding.btPositive.setVisibility(VISIBLE);
    }

    public void setNegativeButton(@Nullable String text, @ColorRes int textColor, @NonNull View.OnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        binding.btNegative.setText(text);
        if (textColor != 0) {
            binding.btNegative.setTextColor(AppCompatResources.getColorStateList(getContext(), textColor));
        }
        binding.btNegative.setOnClickListener(clickListener);
        binding.sbButtonPanel.setVisibility(VISIBLE);
        binding.btNegative.setVisibility(VISIBLE);
    }

    public void setNeutralButton(@Nullable String text, @ColorRes int textColor, @NonNull View.OnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        binding.btNeutral.setText(text);
        if (textColor != 0) {
            binding.btNeutral.setTextColor(AppCompatResources.getColorStateList(getContext(), textColor));
        }
        binding.btNeutral.setOnClickListener(clickListener);
        binding.sbButtonPanel.setVisibility(VISIBLE);
        binding.btNeutral.setVisibility(VISIBLE);
    }

    public void setBackgroundBottom() {
        binding.sbParentPanel.setBackgroundResource(backgroundBottomId);
    }

    public void setBackgroundAnchor() {
        binding.sbParentPanel.setBackgroundResource(backgroundAnchorId);
    }

    @NonNull
    private String getEditText() {
        Editable text = binding.etInputText.getText();
        return text == null ? "" : text.toString();
    }

    public void setContentView(@Nullable View view) {
        if (view == null) {
            return;
        }
        binding.sbContentViewPanel.addView(view, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }
}
