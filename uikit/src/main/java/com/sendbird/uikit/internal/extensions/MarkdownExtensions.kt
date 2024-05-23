package com.sendbird.uikit.internal.extensions

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View

internal fun CharSequence.applyMarkdown(onLinkClickListener: ((link: String) -> Unit)?): CharSequence {
    return SpannableStringBuilder(this)
        .applyMarkdownBold()
        .applyMarkdownLink(onLinkClickListener)
}

private fun SpannableStringBuilder.applyMarkdownBold(): SpannableStringBuilder = apply {
    val boldPatterns = listOf(Regex("\\*\\*(.*?)\\*\\*"), Regex("__(.*?)__")) // **bold** or __bold__
    for (boldPattern in boldPatterns) {
        do {
            val match = boldPattern.find(this)
            if (match != null) {
                val start = match.range.first
                val end = match.range.last + 1
                this.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this.delete(end - 2, end) // Remove last '**' or '__'
                this.delete(start, start + 2) // Remove first '**' or '__'
            }
        } while (match != null)
    }
}

private fun SpannableStringBuilder.applyMarkdownLink(
    onLinkClickListener: ((url: String) -> Unit)?
): SpannableStringBuilder = apply {
    val linkPattern = Regex("\\[(.*?)\\]\\((.*?)\\)")
    do {
        val match = linkPattern.find(this)
        if (match != null) {
            val start = match.range.first
            val end = match.range.last + 1
            val linkText = match.groups[1]?.value ?: ""
            val url = match.groups[2]?.value ?: ""
            this.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            this.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onLinkClickListener?.invoke(url)
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            this.delete(start + linkText.length + 2, end) // remove '(url)' of '[linkText](url)'
            this.delete(start + linkText.length + 1, start + linkText.length + 2) // remove ']' of  '[linkText]'
            this.delete(start, start + 1) // remove '[' of  '[linkText]'
        }
    } while (match != null)
}
