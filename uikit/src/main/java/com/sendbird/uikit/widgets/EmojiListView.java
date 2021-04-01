package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.Emoji;
import com.sendbird.android.Reaction;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.EmojiListAdapter;
import com.sendbird.uikit.databinding.SbViewEmojiListBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;

import java.util.List;

public class EmojiListView extends FrameLayout {
    private SbViewEmojiListBinding binding;
    private EmojiListAdapter adapter;
    private int maxHeight = Integer.MAX_VALUE;

    public EmojiListView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_emoji_reaction_style);
    }

    public EmojiListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(@NonNull Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.sb_view_emoji_list, this, true);
        maxHeight = (int) context.getResources().getDimension(R.dimen.sb_emoji_reaction_dialog_max_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight > 0){
            int hSize = MeasureSpec.getSize(heightMeasureSpec);
            int hMode = MeasureSpec.getMode(heightMeasureSpec);

            switch (hMode){
                case MeasureSpec.AT_MOST:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.EXACTLY:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.EXACTLY);
                    break;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void initAdapter(@NonNull Params params) {
        adapter = new EmojiListAdapter(params.emojiList, params.reactionList, params.showMoreButton);
        binding.rvEmojiList.setAdapter(adapter);
    }

    public void setEmojiClickListener(OnItemClickListener<String> emojiClickListener) {
        if (adapter != null) {
            adapter.setEmojiClickListener(emojiClickListener);
        }
    }

    public void setMoreButtonClickListener(OnClickListener moreButtonClickListener) {
        if (adapter != null) {
            adapter.setMoreButtonClickListener(moreButtonClickListener);
        }
    }

    public static class Builder {
        Params params;

        public Builder(Context context) {
            params = new Params(context);
        }

        public Builder setReactionList(List<Reaction> reactionList) {
            params.reactionList = reactionList;
            return this;
        }

        public Builder setEmojiList(List<Emoji> emojiList) {
            params.emojiList = emojiList;
            return this;
        }

        public Builder setShowMoreButton(boolean showMoreButton) {
            params.showMoreButton = showMoreButton;
            return this;
        }

        public EmojiListView create() {
            EmojiListView emojiListView = new EmojiListView(params.context);
            emojiListView.initAdapter(params);
            return emojiListView;
        }
    }

    static class Params {
        Context context;
        List<Emoji> emojiList;
        List<Reaction> reactionList;
        boolean showMoreButton;

        Params(Context context) {
            this.context = context;
        }
    }
}
