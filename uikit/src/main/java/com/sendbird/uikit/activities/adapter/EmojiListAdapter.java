package com.sendbird.uikit.activities.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.Emoji;
import com.sendbird.android.Reaction;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.activities.viewholder.EmojiMoreViewHolder;
import com.sendbird.uikit.activities.viewholder.EmojiViewHolder;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.widgets.EmojiView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapters provide a binding from a {@link Emoji} set to views that are displayed
 * within a {@link RecyclerView}.
 *
 * @since 1.1.0
 */
public class EmojiListAdapter extends BaseAdapter<Emoji, BaseViewHolder<Emoji>> {
    private static final int VIEW_EMOJI = 0;
    private static final int VIEW_EMOJI_MORE = 1;

    private final List<Emoji> emojiList;
    private final Map<String, List<String>> reactionUserMap = new HashMap<>();
    private OnItemClickListener<String> emojiClickListener;
    private View.OnClickListener moreButtonClickListener;
    private final boolean showMoreButton;

    /**
     * Constructor
     *
     * @param emojiList The {@link List<Emoji>} that contains the data needed for this adapter
     * @param reactionList The {@link List<Reaction>} that contains the data needed for this adapter
     * @param showMoreButton <code>true</code> if the more button is showed,
     *                       <code>false</code> otherwise.
     * @since 1.1.0
     */
    public EmojiListAdapter(List<Emoji> emojiList,
                            List<Reaction> reactionList,
                            boolean showMoreButton) {
        this.emojiList = emojiList;
        if (reactionList != null) {
            for (Reaction reaction : reactionList) {
                reactionUserMap.put(reaction.getKey(), reaction.getUserIds());
            }
        }
        this.showMoreButton = showMoreButton;
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<Emoji>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<Emoji>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * @since 1.1.0
     */
    @NonNull
    @Override
    public BaseViewHolder<Emoji> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_EMOJI_MORE) {
            return new EmojiMoreViewHolder(new EmojiView(parent.getContext()));
        } else {
            return new EmojiViewHolder(DataBindingUtil.inflate(inflater,
                    R.layout.sb_view_emoji, parent, false));
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<Emoji>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @since 1.1.0
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<Emoji> holder, int position) {
        Emoji current = getItem(position);
        int type = getItemViewType(position);

        if (type == VIEW_EMOJI_MORE) {
            holder.itemView.setOnClickListener(v -> {
                if (moreButtonClickListener != null) {
                    moreButtonClickListener.onClick(v);
                }
            });
        } else {
            if (reactionUserMap != null && !reactionUserMap.isEmpty() && current != null) {
                List<String> userIds = reactionUserMap.get(current.getKey());
                if (userIds != null && SendBird.getCurrentUser() != null && userIds.contains(SendBird.getCurrentUser().getUserId())) {
                    holder.itemView.setSelected(true);
                } else {
                    holder.itemView.setSelected(false);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                Emoji emoji = getItem(holder.getAdapterPosition());
                if (emojiClickListener != null && emoji != null) {
                    emojiClickListener.onItemClick(v,
                            holder.getAdapterPosition(),
                            emoji.getKey());
                }
            });
        }

        holder.bind(current);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * @since 1.1.0
     */
    @Override
    public int getItemCount() {
        if (emojiList == null) {
            return 0;
        }

        if (showMoreButton) {
            return emojiList.size()+1;
        } else {
            return emojiList.size();
        }
    }

    /**
     * Returns the {@link Emoji} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link Emoji} to retrieve the position of in this adapter.
     * @since 1.1.0
     */
    @Override
    public Emoji getItem(int position) {
        if (emojiList == null || position >= emojiList.size()) {
            return null;
        }
        return emojiList.get(position);
    }

    /**
     * Returns the {@link List<Emoji>} in the data set held by the adapter.
     *
     * @return The {@link List<Emoji>} in this adapter.
     * @since 1.1.0
     */
    @Override
    public List<Emoji> getItems() {
        return emojiList;
    }

    /**
     * Return the view type of the {@link BaseViewHolder<Emoji>} at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * @since 1.1.0
     */
    @Override
    public int getItemViewType(int position) {
        if (showMoreButton && position >= emojiList.size()) {
            return VIEW_EMOJI_MORE;
        } else {
            return VIEW_EMOJI;
        }
    }

    /**
     * Register a callback to be invoked when the emoji is clicked and held.
     *
     * @param emojiClickListener The callback that will run
     * @since 1.1.0
     */
    public void setEmojiClickListener(@Nullable OnItemClickListener<String> emojiClickListener) {
        this.emojiClickListener = emojiClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji more button is clicked and held.
     *
     * @param moreButtonClickListener The callback that will run
     * @since 1.1.0
     */
    public void setMoreButtonClickListener(@Nullable View.OnClickListener moreButtonClickListener) {
        this.moreButtonClickListener = moreButtonClickListener;
    }
}
