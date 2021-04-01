package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewMessageRecyclerViewBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.SoftInputUtils;

public class MessageRecyclerView extends FrameLayout {
    private SbViewMessageRecyclerViewBinding binding;

    public MessageRecyclerView(Context context) {
        this(context, null);
    }

    public MessageRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_channel_message_list_style);
    }

    public MessageRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageListView, defStyle, 0);
        try {
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_message_recycler_view, this, true);
            boolean useDividerLine = a.getBoolean(R.styleable.MessageListView_sb_pager_recycler_view_use_divide_line, false);
            int dividerColor = a.getColor(R.styleable.MessageListView_sb_pager_recycler_view_divide_line_color, context.getResources().getColor(android.R.color.transparent));
            float dividerHeight = a.getDimension(R.styleable.MessageListView_sb_pager_recycler_view_divide_line_height, 0);
            int recyclerViewBackground = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_background, R.color.background_50);
            int tooltipBackground = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_tooltip_background, R.drawable.selector_tooltip_background_light);
            int tooltipTextAppearance = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_tooltip_textappearance, R.style.SendbirdCaption1Primary300);
            int typingIndicatorTextAppearance = a.getResourceId(R.styleable.MessageListView_sb_message_typing_indicator_textappearance, R.style.SendbirdCaption1OnLight02);

            int scrollBottomBackground = a.getResourceId(R.styleable.MessageListView_sb_message_scroll_bottom_background, R.drawable.selector_scroll_bottom_light);
            int scrollBottomIcon = a.getResourceId(R.styleable.MessageListView_sb_message_scroll_bottom_icon, R.drawable.icon_chevron_down);
            int scrollBottomTintColor = a.getResourceId(R.styleable.MessageListView_sb_message_scroll_bottom_icon_tint, R.color.primary_300);

            binding.rvMessageList.setBackgroundResource(recyclerViewBackground);
            binding.rvMessageList.setOnTouchListener((v, event) -> {
                SoftInputUtils.hideSoftKeyboard(this);
                v.performClick();
                return false;
            });
            binding.rvMessageList.setUseDivider(useDividerLine);
            binding.rvMessageList.setDividerColor(dividerColor);
            binding.rvMessageList.setDividerHeight(dividerHeight);

            binding.tvTooltipText.setBackgroundResource(tooltipBackground);
            binding.tvTooltipText.setTextAppearance(context, tooltipTextAppearance);
            binding.tvTypingIndicator.setTextAppearance(context, typingIndicatorTextAppearance);

            binding.ivScrollBottomIcon.setBackgroundResource(scrollBottomBackground);
            binding.ivScrollBottomIcon.setImageDrawable(DrawableUtils.setTintList(getContext(), scrollBottomIcon, scrollBottomTintColor));
        } finally {
            a.recycle();
        }
    }

    public void showTypingIndicator(@NonNull String text) {
        binding.tvTypingIndicator.setVisibility(View.VISIBLE);
        binding.tvTypingIndicator.setText(text);
    }

    public void hideTypingIndicator() {
        binding.tvTypingIndicator.setVisibility(View.GONE);
    }

    public void showNewMessageTooltip(@NonNull String text) {
        binding.vgTooltipBox.setVisibility(View.VISIBLE);
        binding.tvTooltipText.setText(text);
    }

    public void showScrollBottomButton() {
        binding.ivScrollBottomIcon.setVisibility(View.VISIBLE);
    }

    public void hideScrollBottomButton() {
        binding.ivScrollBottomIcon.setVisibility(View.GONE);
    }

    public void hideNewMessageTooltip() {
        binding.vgTooltipBox.setVisibility(View.GONE);
    }

    public View getLayout() {
        return binding.getRoot();
    }

    public PagerRecyclerView getRecyclerView() {
        return binding.rvMessageList;
    }

    public View getTooltipView() {
        return binding.tvTooltipText;
    }

    public View getScrollBottomView() {
        return binding.ivScrollBottomIcon;
    }

    public View getTypingIndicator() {
        return binding.tvTypingIndicator;
    }
}
