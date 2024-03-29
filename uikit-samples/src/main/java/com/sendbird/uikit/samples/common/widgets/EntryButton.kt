package com.sendbird.uikit.samples.common.widgets

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.setAppearance
import com.sendbird.uikit.samples.databinding.ViewEntryButtonBinding
import com.sendbird.uikit.samples.utils.toDp
import com.sendbird.uikit.utils.DrawableUtils

class EntryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.widget_entry_button
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewEntryButtonBinding
    var unreadCountVisibility: Int
        get() = binding.unreadCount.visibility
        set(value) {
            binding.unreadCount.visibility = value
        }
    var unreadCount: String
        get() = binding.unreadCount.text.toString()
        set(value) {
            binding.unreadCount.text = value
        }

    init {
        val inflater = LayoutInflater.from(getContext())
        binding = ViewEntryButtonBinding.inflate(inflater, this, true)
        val a = context.obtainStyledAttributes(attrs, R.styleable.EntryButton, defStyleAttr, 0)
        try {
            a.getString(R.styleable.EntryButton_title)?.let { str ->
                binding.title.text = str
            }
            a.getString(R.styleable.EntryButton_android_description)?.let { str ->
                if (str.isNotEmpty()) {
                    binding.description.visibility = View.VISIBLE
                    binding.description.text = str
                } else {
                    binding.description.visibility = View.GONE
                }
            }

            a.getResourceId(
                R.styleable.EntryButton_background_color,
                R.drawable.selector_home_channel_type_button
            ).apply { binding.root.setBackgroundResource(this) }
            a.getColorStateList(R.styleable.EntryButton_icon_tint).apply {
                binding.chevron.setImageDrawable(
                    DrawableUtils.setTintList(
                        getContext(),
                        R.drawable.icon_chevron_right,
                        this
                    )
                )
            }
            a.getResourceId(
                R.styleable.EntryButton_title_text_appearance,
                R.style.EntryButtonTitle
            ).apply { binding.title.setAppearance(context, this) }
            a.getResourceId(
                R.styleable.EntryButton_description_text_appearance,
                R.style.EntryButtonDescription
            ).apply { binding.description.setAppearance(context, this) }
        } finally {
            a.recycle()
        }
        // shadow
        this.setBackgroundResource(R.drawable.selector_home_channel_type_button)
        this.elevation = 4.toDp().toFloat()
        this.outlineProvider = EntryButtonOutlineProvider()
        this.clipToOutline = true
    }

    class EntryButtonOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            outline?.setRoundRect(0, 0, view?.width ?: 0, view?.height ?: 0, 4.toDp().toFloat())
        }
    }
}
