package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewSearchBarBinding;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.interfaces.OnSearchEventListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.DrawableUtils;

import java.lang.reflect.Field;

public class SearchBarView extends FrameLayout {
    private SbViewSearchBarBinding binding;
    private OnSearchEventListener searchEventListener;
    private OnInputTextChangedListener textChangedListener;

    public SearchBarView(Context context) {
        this(context, null);
    }

    public SearchBarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_search_bar_style);
    }

    public SearchBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SearchBar, defStyle, 0);
        try {
            this.binding = SbViewSearchBarBinding.inflate(LayoutInflater.from(getContext()));
            addView(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            int itemBackground = a.getResourceId(R.styleable.SearchBar_sb_search_bar_background, R.color.background_50);
            int dividerColor = a.getResourceId(R.styleable.SearchBar_sb_search_divider_color, R.color.onlight_04);
            int inputBackground = a.getResourceId(R.styleable.SearchBar_sb_search_bar_text_input_background, R.drawable.sb_shape_search_background);
            int textAppearance = a.getResourceId(R.styleable.SearchBar_sb_search_bar_text_appearance, R.style.SendbirdBody3OnLight01);
            int hintText = a.getResourceId(R.styleable.SearchBar_sb_search_bar_hint_text, R.string.sb_text_button_search);
            int hintTextColor = a.getResourceId(R.styleable.SearchBar_sb_search_bar_hint_text_color, R.color.onlight_03);
            int clearIcon = a.getResourceId(R.styleable.SearchBar_sb_search_bar_clear_icon, R.drawable.icon_remove);
            int clearIconTintColor = a.getResourceId(R.styleable.SearchBar_sb_search_bar_clear_icon_tint_color, R.color.onlight_03);
            int searchButtonText = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_text, R.string.sb_text_button_search);
            int searchTextAppearance = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_text_appearance, R.style.SendbirdButtonPrimary300);
            int searchTextColor = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_text_color, R.color.sb_button_uncontained_text_color_light);
            int searchTextBackground = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_text_background, R.drawable.sb_button_uncontained_background_light);
            int cursorDrawable = a.getResourceId(R.styleable.SearchBar_sb_search_bar_cursor_drawable, R.drawable.sb_message_input_cursor_light);
            int searchIcon = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_icon, R.drawable.icon_search);
            int searchIconTintColor = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_icon_tint_color, R.color.onlight_03);

            binding.searchBar.setBackgroundResource(itemBackground);
            binding.ivDivider.setBackgroundResource(dividerColor);
            binding.searchEditBox.setBackgroundResource(inputBackground);
            binding.etInputText.setTextAppearance(context, textAppearance);
            binding.etInputText.setHint(hintText);
            binding.etInputText.setHintTextColor(ContextCompat.getColor(context, hintTextColor));
            binding.ivClear.setImageDrawable(DrawableUtils.setTintList(context, clearIcon, clearIconTintColor));
            binding.tvSearch.setText(searchButtonText);
            binding.tvSearch.setTextAppearance(context, searchTextAppearance);
            binding.tvSearch.setTextColor(AppCompatResources.getColorStateList(context, searchTextColor));
            binding.tvSearch.setBackgroundResource(searchTextBackground);
            binding.ivSearchIcon.setImageDrawable(DrawableUtils.setTintList(context, searchIcon, searchIconTintColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.etInputText.setTextCursorDrawable(cursorDrawable);
            } else {
                Field f = TextView.class.getDeclaredField(StringSet.mCursorDrawableRes);
                f.setAccessible(true);
                f.set(binding.etInputText, cursorDrawable);
            }

            binding.etInputText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (searchEventListener != null) {
                        searchEventListener.onSearchRequested(binding.etInputText.getText().toString());
                    }
                    return true;
                }
                return false;
            });

            binding.etInputText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    binding.ivClear.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                    if (textChangedListener != null) {
                        textChangedListener.onInputTextChanged(s, start, before, count);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            binding.ivClear.setOnClickListener(v -> binding.etInputText.setText(""));
            binding.tvSearch.setOnClickListener(v -> {
                if (searchEventListener != null) {
                    searchEventListener.onSearchRequested(binding.etInputText.getText().toString());
                }
            });
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            a.recycle();
        }
    }

    public View getLayout() {
        return this;
    }

    public SbViewSearchBarBinding getBinding() {
        return binding;
    }

    public TextView getSearchButton() {
        return binding.tvSearch;
    }

    public void setText(CharSequence text) {
        binding.etInputText.setText(text);
    }

    public void setHintText(CharSequence hint) {
        binding.etInputText.setHint(hint);
    }

    public void setOnSearchEventListener(OnSearchEventListener listener) {
        this.searchEventListener = listener;
    }

    public void setOnInputTextChangedListener(OnInputTextChangedListener textChangedListener) {
        this.textChangedListener = textChangedListener;
    }
}
