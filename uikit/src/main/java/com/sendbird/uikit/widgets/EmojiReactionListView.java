package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.sendbird.android.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.EmojiReactionListAdapter;
import com.sendbird.uikit.databinding.SbViewEmojiReactionListBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;

import java.util.List;

public class EmojiReactionListView extends FrameLayout {
    private SbViewEmojiReactionListBinding binding;
    private EmojiReactionListAdapter adapter;
    private GridLayoutManager layoutManager;
    private final static int MAX_SPAN_SIZE = 4;

    public EmojiReactionListView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiReactionListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_emoji_reaction_style);
    }

    public EmojiReactionListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_emoji_reaction_list, this, true);
        layoutManager = new GridLayoutManager(getContext(), MAX_SPAN_SIZE);
        binding.rvEmojiReactionList.setLayoutManager(layoutManager);
        binding.rvEmojiReactionList.setHasFixedSize(true);
        adapter = new EmojiReactionListAdapter();
        binding.rvEmojiReactionList.setAdapter(adapter);
    }

    public void setReactionList(List<Reaction> reactionList) {
        if (adapter != null) {
            adapter.setReactionList(reactionList);
            resetSpanSize();
        }
    }

    private void resetSpanSize() {
        int itemSize = adapter.getItemCount();
        if (itemSize > 0) {
            layoutManager.setSpanCount(Math.min(itemSize, MAX_SPAN_SIZE));
        }
    }

    public void setEmojiReactionClickListener(@Nullable OnItemClickListener<String> emojiReactionClickListener) {
        if (adapter != null) {
            adapter.setEmojiReactionClickListener(emojiReactionClickListener);
        }
    }

    public void setEmojiReactionLongClickListener(@Nullable OnItemLongClickListener<String> emojiReactionLongClickListener) {
        if (adapter != null) {
            adapter.setEmojiReactionLongClickListener(emojiReactionLongClickListener);
        }
    }

    public void setMoreButtonClickListener(@Nullable OnClickListener moreButtonClickListener) {
        if (adapter != null) {
            adapter.setMoreButtonClickListener(moreButtonClickListener);
        }
    }

    public void setUseMoreButton(boolean useMoreButton) {
        if (adapter != null) {
            adapter.setUseMoreButton(useMoreButton);
        }
    }

    public boolean useMoreButton() {
        if (adapter != null) {
            return adapter.useMoreButton();
        }
        return true;
    }

    public void refresh() {
        if (adapter != null) {
            resetSpanSize();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        if (adapter != null) {
            adapter.setClickable(clickable);
        }
    }

    @Override
    public void setLongClickable(boolean clickable) {
        super.setLongClickable(clickable);
        if (adapter != null) {
            adapter.setClickable(clickable);
        }
    }

    public EmojiReactionListView getLayout() {
        return this;
    }

    public SbViewEmojiReactionListBinding getBinding() {
        return binding;
    }
}
