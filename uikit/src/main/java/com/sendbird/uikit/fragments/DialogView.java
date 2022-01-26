package com.sendbird.uikit.fragments;

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
import androidx.databinding.DataBindingUtil;

import com.sendbird.uikit.R;
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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

class DialogView extends LinearLayout {
    private SbViewDialogBinding binding;

    private int backgroundBottomId;
    private int backgroundAnchorId;
    private OnEditTextResultListener editTextResultListener;

    DialogView(Context context) {
        this(context, null);
    }

    DialogView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_dialog_view_style);
    }

    DialogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DialogView, defStyleAttr, 0);
        try {
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.sb_view_dialog, null, false);
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int backgroundId = a.getResourceId(R.styleable.DialogView_sb_dialog_view_background, R.drawable.sb_rounded_rectangle_light);
            backgroundBottomId = a.getResourceId(R.styleable.DialogView_sb_dialog_view_background_bottom, R.drawable.sb_top_rounded_rectangle_light);
            backgroundAnchorId = a.getResourceId(R.styleable.DialogView_sb_dialog_view_background_anchor, R.drawable.layer_dialog_anchor_background_light);
            int titleAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_title_appearance, R.style.SendbirdH1OnLight01);
            int messageAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_message_appearance, R.style.SendbirdSubtitle2OnLight01);
            int editTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_edit_text_appearance, R.style.SendbirdSubtitle2OnLight01);
            ColorStateList editTextTint = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_edit_text_tint);
            int editTextCussorDrawable = a.getResourceId(R.styleable.DialogView_sb_dialog_view_edit_text_cursor_drawable, R.drawable.sb_message_input_cursor_light);
            ColorStateList editTextHintColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_edit_text_hint_color);

            int positiveButtonTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_positive_button_text_appearance, R.style.SendbirdButtonPrimary300);
            int positiveButtonTextColor = a.getResourceId(R.styleable.DialogView_sb_dialog_view_positive_button_text_color, R.color.sb_button_uncontained_text_color_light);
            int positiveButtonBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_positive_button_background, R.drawable.sb_button_uncontained_background_light);
            int negativeButtonTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_negative_button_text_appearance, R.style.SendbirdButtonPrimary300);
            int negativeButtonTextColor = a.getResourceId(R.styleable.DialogView_sb_dialog_view_negative_button_text_color, R.color.sb_button_uncontained_text_color_light);
            int negativeButtonBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_negative_button_background, R.drawable.sb_button_uncontained_background_light);
            int neutralButtonTextAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_neutral_button_text_appearance, R.style.SendbirdButtonPrimary300);
            int neutralButtonTextColor = a.getResourceId(R.styleable.DialogView_sb_dialog_view_neutral_button_text_color, R.color.sb_button_uncontained_text_color_light);
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
                binding.etInputText.setTextCursorDrawable(editTextCussorDrawable);
            } else {
                Field f = TextView.class.getDeclaredField(StringSet.mCursorDrawableRes);
                f.setAccessible(true);
                f.set(binding.etInputText, editTextCussorDrawable);
            }

            binding.btPositive.setTextAppearance(context, positiveButtonTextAppearance);
            binding.btPositive.setTextColor(AppCompatResources.getColorStateList(context, positiveButtonTextColor));
            binding.btPositive.setBackgroundResource(positiveButtonBackground);
            binding.btNegative.setTextAppearance(context, negativeButtonTextAppearance);
            binding.btNegative.setTextColor(AppCompatResources.getColorStateList(context, negativeButtonTextColor));
            binding.btNegative.setBackgroundResource(negativeButtonBackground);
            binding.btNeutral.setTextAppearance(context, neutralButtonTextAppearance);
            binding.btNeutral.setTextColor(AppCompatResources.getColorStateList(context, neutralButtonTextColor));
            binding.btNeutral.setBackgroundResource(neutralButtonBackground);
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            a.recycle();
        }
    }

    void setTitle(int title) {
        if (title == 0) {
            return;
        }
        binding.tvDialogTitle.setText(title);
        binding.tvDialogTitle.setVisibility(VISIBLE);
    }

    void setTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        binding.tvDialogTitle.setText(title);
        binding.tvDialogTitle.setVisibility(VISIBLE);
    }

    void setMessage(int message) {
        if (message == 0) {
            return;
        }
        binding.tvDialogMessage.setText(message);
        binding.tvDialogMessage.setVisibility(VISIBLE);
    }

    void setEditText(DialogEditTextParams params, OnEditTextResultListener editTextResultListener) {
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

    void setItems(DialogListItem[] items, @NonNull OnItemClickListener<DialogListItem> itemClickListener, boolean isLeft) {
        if (items == null) return;
        binding.rvSelectView.setAdapter(new DialogListAdapter(items, itemClickListener, isLeft) );
        binding.rvSelectView.setVisibility(VISIBLE);
    }

    void setItems(DialogListItem[] items, @NonNull OnItemClickListener<DialogListItem> itemClickListener, boolean isLeft, @DimenRes int nameMarginLeft) {
        if (items == null) return;
        DialogListAdapter adapter = new DialogListAdapter(items, itemClickListener, isLeft);
        adapter.setNameMarginLeft(nameMarginLeft);
        binding.rvSelectView.setAdapter(adapter);
        binding.rvSelectView.setVisibility(VISIBLE);
    }

    void setPositiveButton(String text, @ColorRes int textColor, @NonNull View.OnClickListener clickListener) {
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

    void setNegativeButton(String text, @ColorRes int textColor, @NonNull View.OnClickListener clickListener) {
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

    void setNeutralButton(String text, @ColorRes int textColor, @NonNull View.OnClickListener clickListener) {
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

    void setBackgroundBottom() {
        binding.sbParentPanel.setBackgroundResource(backgroundBottomId);
    }

    void setBackgroundAnchor() {
        binding.sbParentPanel.setBackgroundResource(backgroundAnchorId);
    }

    private String getEditText() {
        Editable text = binding.etInputText.getText();
        return text == null ? "" : text.toString();
    }

    void setContentView(View view) {
        if (view == null) {
            return;
        }
        binding.sbContentViewPanel.addView(view, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }
}
