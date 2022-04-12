package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.R;
import com.sendbird.uikit.databinding.SbViewMessageRecyclerViewBinding;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;

public class MessageRecyclerView extends FrameLayout {
    private final SbViewMessageRecyclerViewBinding binding;

    public MessageRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public MessageRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageListView, defStyle, 0);
        try {
            this.binding = SbViewMessageRecyclerViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int dividerColor = a.getColor(R.styleable.MessageListView_sb_pager_recycler_view_divide_line_color, context.getResources().getColor(android.R.color.transparent));
            float dividerHeight = a.getDimension(R.styleable.MessageListView_sb_pager_recycler_view_divide_line_height, 0);
            int recyclerViewBackground = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_background, R.color.background_50);
            int tooltipBackground = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_tooltip_background, R.drawable.selector_tooltip_background_light);
            int tooltipTextAppearance = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_tooltip_textappearance, R.style.SendbirdCaption1Primary300);
            int typingIndicatorTextAppearance = a.getResourceId(R.styleable.MessageListView_sb_message_typing_indicator_textappearance, R.style.SendbirdCaption1OnLight02);
            int bannerBackground = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_banner_background, R.drawable.sb_shape_channel_information_bg);
            int bannerTextAppearance = a.getResourceId(R.styleable.MessageListView_sb_message_recyclerview_banner_textappearance, R.style.SendbirdCaption2OnLight01);

            int scrollBottomBackground = a.getResourceId(R.styleable.MessageListView_sb_message_scroll_bottom_background, R.drawable.selector_scroll_bottom_light);
            int scrollBottomIcon = a.getResourceId(R.styleable.MessageListView_sb_message_scroll_bottom_icon, R.drawable.icon_chevron_down);
            ColorStateList scrollBottomTintColor = a.getColorStateList(R.styleable.MessageListView_sb_message_scroll_bottom_icon_tint);

            this.setBackgroundResource(android.R.color.transparent);
            binding.rvMessageList.setBackgroundResource(recyclerViewBackground);
            binding.rvMessageList.setOnTouchListener((v, event) -> {
                SoftInputUtils.hideSoftKeyboard(this);
                v.performClick();
                return false;
            });
            binding.rvMessageList.setUseDivider(false);
            binding.rvMessageList.setDividerColor(dividerColor);
            binding.rvMessageList.setDividerHeight(dividerHeight);

            binding.tvTooltipText.setBackgroundResource(tooltipBackground);
            binding.tvTooltipText.setTextAppearance(context, tooltipTextAppearance);
            binding.tvTypingIndicator.setTextAppearance(context, typingIndicatorTextAppearance);

            binding.ivScrollBottomIcon.setBackgroundResource(scrollBottomBackground);
            binding.ivScrollBottomIcon.setImageResource(scrollBottomIcon);
            binding.ivScrollBottomIcon.setImageTintList(scrollBottomTintColor);

            binding.tvBanner.setBackgroundResource(bannerBackground);
            binding.tvBanner.setTextAppearance(context, bannerTextAppearance);
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

    @NonNull
    public View getLayout() {
        return binding.getRoot();
    }

    @NonNull
    public PagerRecyclerView getRecyclerView() {
        return binding.rvMessageList;
    }

    @NonNull
    public View getTooltipView() {
        return binding.tvTooltipText;
    }

    @NonNull
    public View getScrollBottomView() {
        return binding.ivScrollBottomIcon;
    }

    @NonNull
    public View getTypingIndicator() {
        return binding.tvTypingIndicator;
    }

    @NonNull
    public View getBannerView() { return binding.tvBanner; }

    public void setBannerText(@Nullable String text) {
        binding.tvBanner.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        binding.tvBanner.setText(text);
    }
}
