package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

import com.sendbird.android.Emoji;
import com.sendbird.android.EmojiCategory;
import com.sendbird.android.EmojiContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Manager providing emoji information from Sendbird server.
 *
 * @since 1.1.0
 */
public final class EmojiManager {
    private EmojiManager() {}

    private static class EmojiManagerHolder {
        static final EmojiManager INSTANCE = new EmojiManager();
    }
    /**
     * Returns the {@link EmojiManager} working as a singleton.
     *
     * @return The {@link EmojiManager}
     * @since 1.1.0
     */
    public static EmojiManager getInstance() {
        return EmojiManagerHolder.INSTANCE;
    }

    private String emojiHash = null;
    private final Object emojiLock = new Object();
    private LinkedHashMap<Long, EmojiCategory> emojiCategoryMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Emoji> allEmojiMap = new LinkedHashMap<>();

    public void upsertEmojiContainer(@NonNull EmojiContainer emojiContainer) {
        emojiHash = emojiContainer.getEmojiHash();
        synchronized (emojiLock) {
            emojiCategoryMap = new LinkedHashMap<>();
            allEmojiMap = new LinkedHashMap<>();
            for (EmojiCategory emojiCategory : emojiContainer.getEmojiCategories()) {
                emojiCategoryMap.put(emojiCategory.getId(), emojiCategory);
                for (Emoji emoji : emojiCategory.getEmojis()) {
                    allEmojiMap.put(emoji.getKey(), emoji);
                }
            }
        }
    }

    /**
     * Returns the emoji hash
     *
     * @return The emoji hash
     * @since 1.1.0
     */
    public String getEmojiHash() {
        return emojiHash;
    }

    /**
     * Returns the emoji url corresponding to emoji key
     *
     * @param key emoji key
     * @return The emoji url corresponding to emoji key
     * @since 1.1.0
     */
    public String getEmojiUrl(final String key) {
        synchronized (emojiLock) {
            if (allEmojiMap != null) {
                Emoji emoji = allEmojiMap.get(key);
                if (emoji != null) {
                    return emoji.getUrl();
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link List<EmojiCategory>} registering Sendbird server
     *
     * @return The {@link List<EmojiCategory>} Emoji category list
     * @since 1.1.0
     */
    public List<EmojiCategory> getAllEmojiCategories() {
        List<EmojiCategory> emojiCategoryList = new ArrayList<>(emojiCategoryMap.values());
        return Collections.unmodifiableList(emojiCategoryList);
    }

    /**
     * Returns the {@link List<Emoji>} registering Sendbird server
     *
     * @return The {@link List<Emoji>} registering Sendbird server
     * @since 1.1.0
     */
    public List<Emoji> getAllEmojis() {
        List<Emoji> emojiList = new ArrayList<>(allEmojiMap.values());
        return Collections.unmodifiableList(emojiList);
    }

    /**
     * Returns the {@link List<Emoji>} corresponding to emoji category id
     *
     * @param emojiCategoryId Emoji category id
     * @return The {@link List<Emoji>} corresponding to emoji category id
     * @since 1.1.0
     */
    public List<Emoji> getEmojis(long emojiCategoryId) {
        synchronized (emojiLock) {
            EmojiCategory emojiCategory = emojiCategoryMap.get(emojiCategoryId);
            if (emojiCategory != null) {
                return Collections.unmodifiableList(emojiCategory.getEmojis());
            }
        }
        return null;
    }
}
