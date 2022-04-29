package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

public class MentionSuggestion {
    @NonNull
    private final String keyword;
    @NonNull
    private final List<User> suggestionList = new ArrayList<>();

    public MentionSuggestion(@NonNull String keyword) {
        this.keyword = keyword;
    }

    @NonNull
    public String getKeyword() {
        return keyword;
    }

    @NonNull
    public List<User> getSuggestionList() {
        return suggestionList;
    }

    public void append(@NonNull List<User> suggestionList) {
        this.suggestionList.addAll(suggestionList);
    }

    public void clear() {
        this.suggestionList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MentionSuggestion that = (MentionSuggestion) o;

        if (!keyword.equals(that.keyword)) return false;
        return suggestionList.equals(that.suggestionList);
    }

    @Override
    public int hashCode() {
        int result = keyword.hashCode();
        result = 31 * result + suggestionList.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MentionSuggestion{" +
                "keyword='" + keyword + '\'' +
                ", suggestionList=" + suggestionList +
                '}';
    }
}
