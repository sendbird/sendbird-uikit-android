package com.sendbird.uikit.widgets;

import android.text.Editable;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.MentionSpan;
import com.sendbird.uikit.model.UserMentionConfig;

class MentionWatcher {
    private final String trigger;
    private final String delimiter;
    private final EditText editText;
    private final OnMentionTextChanges handler;

    interface OnMentionTextChanges {
        void onMentionTextDetectStateChanged(boolean isDetected, @Nullable CharSequence detectedKeyword);
    }

    public MentionWatcher(@NonNull EditText editText, @NonNull UserMentionConfig mentionConfig, @NonNull OnMentionTextChanges handler) {
        this.trigger = mentionConfig.getTrigger();
        this.delimiter = mentionConfig.getDelimiter();
        this.editText = editText;
        this.handler = handler;
    }

    public void findMention(@NonNull Editable editable) {
        final String src = editable.toString();
        int cursorStart = this.editText.getSelectionStart();
        int cursorEnd = this.editText.getSelectionEnd();
        if (cursorStart == cursorEnd) {
            int index = findTriggerIndex(this.editText, this.trigger, this.delimiter, cursorStart);
            CharSequence keyword = null;
            if (index >= 0 && index + this.trigger.length() <= cursorStart) {
                keyword = src.subSequence(index + this.trigger.length(), cursorStart);
            }
            Logger.d("++ found index = %d, keyword=%s", index, keyword);
            this.handler.onMentionTextDetectStateChanged(keyword != null, keyword);
        }
    }

    public static int findTriggerIndex(@NonNull EditText editText, @NonNull String trigger, @NonNull String delimiter, int cursorPosition) {
        final Editable editable = editText.getText();

        // select word -> insert a new character -> MentionSapn is not removed if there exists
        MentionSpan[] currentSpan = editable.getSpans(cursorPosition, cursorPosition, MentionSpan.class);
        if (currentSpan.length > 0) {
            int currentSpanStart = editable.getSpanStart(currentSpan[0]);
            int currentSpanEnd = editable.getSpanEnd(currentSpan[0]);
            if (!currentSpan[0].getDisplayText().contentEquals(editable.subSequence(currentSpanStart, currentSpanEnd))) {
                editable.removeSpan(currentSpan[0]);
            }
        }

        // skip the previous spannable string.
        final MentionSpan[] spans = editable.getSpans(0, cursorPosition, MentionSpan.class);
        int spanCount = spans.length;
        int from = 0;
        if (spanCount > 0) {
            final MentionSpan lastSpan = spans[spanCount - 1];
            from = editable.getSpanEnd(lastSpan);
        }

        int result = -1;
        if (from >= cursorPosition) return result;

        final String src = editable.toString().substring(from, cursorPosition);
        // 1. splits a string by delimiter, new line
        final String[] words = src.split("[\\n" + delimiter + "]");
        // 2. loops the split strings from the last
        for (int i = words.length; i-- > 0; ) {
            final String targetWord = words[i];
            // 3. finds the first token in a string.
            int triggerIndex = targetWord.indexOf(trigger);
            if (triggerIndex != -1) {
                final int wordIndex = src.lastIndexOf(targetWord);
                result = from + wordIndex + triggerIndex;
                break;
            }
        }
        return result;
    }
}
