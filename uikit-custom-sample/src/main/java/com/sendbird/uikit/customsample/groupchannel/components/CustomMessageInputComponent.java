package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.Emoji;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.databinding.ViewCustomChannelInputBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.modules.components.MessageInputComponent;
import com.sendbird.uikit.widgets.MessageInputView;

import java.util.List;

/**
 * Implements the customized <code>MessageInputComponent</code>.
 */
public class CustomMessageInputComponent extends MessageInputComponent {
    private final static String MODE_MENU = "MODE_MENU";
    private final static String MODE_EMOJI = "MODE_EMOJI";
    @Nullable
    private GroupChannel channel;

    private ViewCustomChannelInputBinding binding;
    @Nullable
    private CompoundButton.OnCheckedChangeListener onHighlightCheckedListener;
    @Nullable
    private View.OnClickListener onMenuCameraClickListener;
    @Nullable
    private View.OnClickListener onMenuPhotoClickListener;
    @Nullable
    private View.OnClickListener onMenuFileClickListener;
    @Nullable
    private View.OnClickListener onVoiceMessageClickListener;
    @Nullable
    private OnItemClickListener<String> onEmojiClickListener;
    @NonNull
    private MessageInputView.Mode mode = MessageInputView.Mode.DEFAULT;
    @NonNull
    private final EmojiAdapter adapter = new EmojiAdapter();
    private boolean isLeftClosed = false;

    public CustomMessageInputComponent() {
        super();
    }

    @Nullable
    @Override
    public View getRootView() {
        return binding.getRoot();
    }


    @Nullable
    @Override
    public EditText getEditTextView() {
        return binding.input;
    }

    @Override
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        super.notifyChannelChanged(channel);
        this.channel = channel;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        binding = ViewCustomChannelInputBinding.inflate(inflater, null, false);
        binding.emojiPanel.setLayoutManager(new GridLayoutManager(context, 6));
        binding.emojiPanel.setAdapter(adapter);
        binding.sendButton.setOnClickListener(this::onInputRightButtonClicked);
        binding.leftButton.setOnClickListener(v -> {
            if (isLeftClosed) {
                requestInputMode(MessageInputView.Mode.DEFAULT);
                if (channel != null) {
                    notifyDataChanged(null, channel);
                }
            } else {
                requestInputMode(MODE_MENU);
            }
        });
        binding.emojiButton.setOnClickListener(v -> {
            if (isLeftClosed) {
                requestInputMode(MessageInputView.Mode.DEFAULT);
                if (channel != null) {
                    notifyDataChanged(null, channel);
                }
            } else {
                requestInputMode(MODE_EMOJI);
            }
        });
        binding.highlightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onHighlightCheckedListener != null) onHighlightCheckedListener.onCheckedChanged(buttonView, isChecked);
        });
        binding.camera.setOnClickListener(v -> {
            if (onMenuCameraClickListener != null) onMenuCameraClickListener.onClick(v);
        });
        binding.photo.setOnClickListener(v -> {
            if (onMenuPhotoClickListener != null) onMenuPhotoClickListener.onClick(v);
        });
        binding.file.setOnClickListener(v -> {
            if (onMenuFileClickListener != null) onMenuFileClickListener.onClick(v);
        });
        binding.voiceMessageButton.setOnClickListener(v -> {
            if (onVoiceMessageClickListener != null) onVoiceMessageClickListener.onClick(v);
        });
        binding.input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onInputTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter.emojiClickListener = onEmojiClickListener;
        return binding.getRoot();
    }

    @Override
    public void notifyDataChanged(@Nullable BaseMessage message, @NonNull GroupChannel channel, @NonNull String defaultValue) {
        super.notifyDataChanged(message, channel, defaultValue);

        if (mode == MessageInputView.Mode.QUOTE_REPLY) {
            if (message != null) {
                binding.replyPanel.setText(message.getMessage());
                binding.replyPanel.setVisibility(View.VISIBLE);
            }
        } else if (mode == MessageInputView.Mode.EDIT) {
            if (message != null) {
                String customType = message.getCustomType();
                if (!TextUtils.isEmpty(customType) &&
                        customType.equals(StringSet.highlight)) {
                    binding.highlightSwitch.setChecked(true);
                }
                binding.input.setText(message.getMessage());
            }
        } else {
            binding.input.setText(defaultValue);
        }
    }

    @Override
    public void requestInputMode(@NonNull MessageInputView.Mode mode) {
        final MessageInputView.Mode before = this.mode;
        this.mode = mode;

        if (mode == MessageInputView.Mode.QUOTE_REPLY) {
            binding.sendButton.setOnClickListener(this::onInputRightButtonClicked);
            binding.emojiPanel.setVisibility(View.GONE);
            binding.highlightSwitch.setChecked(false);
            binding.menuPanel.setVisibility(View.GONE);
            setLeftButton(true);
        } else if (mode == MessageInputView.Mode.EDIT) {
            binding.sendButton.setOnClickListener(this::onEditModeSaveButtonClicked);
            binding.replyPanel.setVisibility(View.GONE);
            binding.emojiPanel.setVisibility(View.GONE);
            binding.menuPanel.setVisibility(View.GONE);
            setLeftButton(false);
        } else {
            binding.sendButton.setOnClickListener(this::onInputRightButtonClicked);
            binding.replyPanel.setVisibility(View.GONE);
            binding.emojiPanel.setVisibility(View.GONE);
            binding.menuPanel.setVisibility(View.GONE);
            binding.highlightSwitch.setChecked(false);
            setLeftButton(false);
        }

        onInputModeChanged(before, mode);
    }

    public void requestInputMode(@NonNull String mode) {
        if (mode.equals(MODE_EMOJI)) {
            binding.replyPanel.setVisibility(View.GONE);
            binding.menuPanel.setVisibility(View.GONE);
            binding.highlightSwitch.setChecked(false);
            binding.emojiPanel.setVisibility(View.VISIBLE);
        } else if (mode.equals(MODE_MENU)) {
            binding.replyPanel.setVisibility(View.GONE);
            binding.emojiPanel.setVisibility(View.GONE);
            binding.highlightSwitch.setChecked(false);
            binding.menuPanel.setVisibility(View.VISIBLE);
        }
        setLeftButton(true);
    }

    public void setOnHighlightCheckedListener(@Nullable CompoundButton.OnCheckedChangeListener onHighlightCheckedListener) {
        this.onHighlightCheckedListener = onHighlightCheckedListener;
    }

    public void setOnMenuCameraClickListener(@Nullable View.OnClickListener onMenuCameraClickListener) {
        this.onMenuCameraClickListener = onMenuCameraClickListener;
    }

    public void setOnMenuPhotoClickListener(@Nullable View.OnClickListener onMenuPhotoClickListener) {
        this.onMenuPhotoClickListener = onMenuPhotoClickListener;
    }

    public void setOnMenuFileClickListener(@Nullable View.OnClickListener onMenuFileClickListener) {
        this.onMenuFileClickListener = onMenuFileClickListener;
    }

    public void setOnVoiceMessageClickListener(@Nullable View.OnClickListener onVoiceMessageClickListener) {
        this.onVoiceMessageClickListener = onVoiceMessageClickListener;
    }

    public void setOnEmojiClickListener(@Nullable OnItemClickListener<String> onEmojiClickListener) {
        this.onEmojiClickListener = onEmojiClickListener;
        adapter.emojiClickListener = onEmojiClickListener;
    }

    private void setLeftButton(final boolean isLeftClosed) {
        this.isLeftClosed = isLeftClosed;
        if (isLeftClosed) {
            binding.leftButton.setImageResource(R.drawable.icon_close);
        } else {
            binding.leftButton.setImageResource(R.drawable.icon_add);
        }
    }

    private static class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {
        @NonNull
        private final List<Emoji> emojis = EmojiManager.getAllEmojis();
        @Nullable
        private OnItemClickListener<String> emojiClickListener;

        @NonNull
        @Override
        public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EmojiViewHolder(new AppCompatImageView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
            final String emojiUrl = emojis.get(position).getUrl();
            holder.bind(emojiUrl);
            holder.itemView.setOnClickListener(v -> {
                if (emojiClickListener != null) {
                    emojiClickListener.onItemClick(v, position, emojiUrl);
                }
            });
        }

        @Override
        public int getItemCount() {
            return emojis.size();
        }

        static class EmojiViewHolder extends RecyclerView.ViewHolder {
            public EmojiViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public void bind(@NonNull String emojiUrl) {
                Glide.with(itemView)
                        .load(emojiUrl)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into((AppCompatImageView) itemView);
            }
        }
    }
}
