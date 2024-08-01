package com.sendbird.uikit.internal.extensions

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View

internal enum class MarkdownType {
    BOLD, LINK
}

internal fun CharSequence.applyMarkdown(
    types: List<MarkdownType> = listOf(MarkdownType.BOLD, MarkdownType.LINK),
    onLinkClickListener: ((link: String) -> Unit)?
): SpannableStringBuilder {
    val spannableStringBuilder = SpannableStringBuilder(this)
    types.forEach { type ->
        when (type) {
            MarkdownType.BOLD -> {
                spannableStringBuilder.convertMarkdownBoldPatterns().applySpan()
            }
            MarkdownType.LINK -> {
                spannableStringBuilder.convertMarkdownLinkPatterns().applySpan(onLinkClickListener)
            }
        }
    }

    return spannableStringBuilder
}

internal fun CharSequence.removeMarkdownFormatting(
    types: List<MarkdownType> = listOf(MarkdownType.BOLD, MarkdownType.LINK)
): SpannableStringBuilder {
    val spannableStringBuilder = SpannableStringBuilder(this)

    // Remove bold patterns
    if (MarkdownType.BOLD in types) {
        spannableStringBuilder.convertMarkdownBoldPatterns()
    }

    // Remove link patterns
    if (MarkdownType.LINK in types) {
        spannableStringBuilder.convertMarkdownLinkPatterns()
    }

    return spannableStringBuilder
}

private fun SpannableStringBuilder.convertMarkdownBoldPatterns(): MarkdownResult {
    val boldPatterns = listOf(Regex("\\*\\*(.*?)\\*\\*"), Regex("__(.*?)__")) // **bold** or __bold__
    val ranges = mutableListOf<Range>()
    for (pattern in boldPatterns) {
        do {
            val match = pattern.find(this)
            if (match != null) {
                val start = match.range.first
                val end = match.range.last + 1
                ranges.add(Range(start, end - 4))
                delete(end - 2, end) // Remove last '**' or '__'
                delete(start, start + 2) // Remove first '**' or '__'
            }
        } while (match != null)
    }
    return MarkdownResult(MarkdownType.BOLD, this, ranges)
}

private fun SpannableStringBuilder.convertMarkdownLinkPatterns(): MarkdownResult {
    val linkPattern = Regex("\\[(.*?)\\]\\((.*?)\\)")
    val ranges = mutableListOf<Range>()
    val urls = mutableListOf<String>()
    do {
        val match = linkPattern.find(this)
        if (match != null) {
            val start = match.range.first
            val end = match.range.last + 1
            val linkText = match.groups[1]?.value ?: ""
            val url = match.groups[2]?.value ?: ""
            ranges.add(Range(start, start + linkText.length))
            urls.add(url)
            delete(start + linkText.length + 2, end) // remove '(url)' of '[linkText](url)'
            delete(start + linkText.length + 1, start + linkText.length + 2) // remove ']' of  '[linkText]'
            delete(start, start + 1) // remove '[' of  '[linkText]'
        }
    } while (match != null)
    return MarkdownResult(MarkdownType.LINK, this, ranges, urls)
}

private data class MarkdownResult(val type: MarkdownType, val src: SpannableStringBuilder, val ranges: List<Range>, val results: List<String>? = null)

private fun MarkdownResult.applySpan(onLinkClickListener: ((link: String) -> Unit)? = null) {
    when (type) {
        MarkdownType.BOLD -> {
            ranges.forEach {
                src.setSpan(StyleSpan(Typeface.BOLD), it.start, it.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        MarkdownType.LINK -> {
            ranges.forEachIndexed { index, range ->
                val url = results?.get(index) ?: ""
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onLinkClickListener?.invoke(url)
                    }
                }
                src.setSpan(StyleSpan(Typeface.BOLD), range.start, range.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                src.setSpan(clickableSpan, range.start, range.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}

private data class Range(val start: Int, val end: Int)
