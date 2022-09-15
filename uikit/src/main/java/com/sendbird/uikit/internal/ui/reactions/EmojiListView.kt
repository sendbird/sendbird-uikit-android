package com.sendbird.uikit.internal.ui.reactions

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction
import com.sendbird.uikit.R
import com.sendbird.uikit.activities.adapter.EmojiListAdapter
import com.sendbird.uikit.databinding.SbViewEmojiListBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import kotlin.math.min

internal class EmojiListView private constructor(context: Context) : FrameLayout(context) {
    private val binding: SbViewEmojiListBinding
    private lateinit var adapter: EmojiListAdapter
    private val maxHeight: Int

    init {
        binding = SbViewEmojiListBinding.inflate(LayoutInflater.from(context), this, true)
        binding.rvEmojiList.setUseDivider(false)
        maxHeight = context.resources.getDimension(R.dimen.sb_emoji_reaction_dialog_max_height).toInt()
    }

    companion object {
        // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
        @JvmStatic
        fun create(
            context: Context,
            emojiList: List<Emoji>,
            reactionList: List<Reaction>? = null,
            showMoreButton: Boolean = false
        ): EmojiListView {
            val emojiListView = EmojiListView(context)
            val adapter = EmojiListAdapter(emojiList, reactionList, showMoreButton)
            emojiListView.adapter = adapter
            emojiListView.binding.rvEmojiList.adapter = adapter
            return emojiListView
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (maxHeight > 0) {
            val hSize = MeasureSpec.getSize(heightMeasureSpec)
            when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.AT_MOST ->
                    heightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(min(hSize, maxHeight), MeasureSpec.AT_MOST)
                MeasureSpec.UNSPECIFIED ->
                    heightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
                MeasureSpec.EXACTLY ->
                    heightMeasureSpec =
                        MeasureSpec.makeMeasureSpec(min(hSize, maxHeight), MeasureSpec.EXACTLY)
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setEmojiClickListener(emojiClickListener: OnItemClickListener<String?>?) {
        adapter.setEmojiClickListener(emojiClickListener)
    }

    fun setMoreButtonClickListener(moreButtonClickListener: OnClickListener?) {
        adapter.setMoreButtonClickListener(moreButtonClickListener)
    }
}
