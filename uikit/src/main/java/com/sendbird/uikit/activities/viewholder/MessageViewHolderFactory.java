package com.sendbird.uikit.activities.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.message.AdminMessage;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.MultipleFilesMessage;
import com.sendbird.android.message.UserMessage;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewAdminMessageBinding;
import com.sendbird.uikit.databinding.SbViewFormMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyFileImageMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyFileMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyFileVideoMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyMultipleFilesMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyUserMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyVoiceMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelAdminMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileImageMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileVideoMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelUserMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherFileImageMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherFileMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherFileVideoMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherMultipleFilesMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherTemplateMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherUserMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherVoiceMessageBinding;
import com.sendbird.uikit.databinding.SbViewParentMessageInfoHolderBinding;
import com.sendbird.uikit.databinding.SbViewSuggestedRepliesMessageBinding;
import com.sendbird.uikit.databinding.SbViewTimeLineMessageBinding;
import com.sendbird.uikit.databinding.SbViewTypingIndicatorMessageBinding;
import com.sendbird.uikit.internal.extensions.MessageExtensionsKt;
import com.sendbird.uikit.internal.extensions.MessageTemplateExtensionsKt;
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus;
import com.sendbird.uikit.internal.ui.viewholders.AdminMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.FormMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.MyFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.MyImageFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.MyMultipleFilesMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.MyUserMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.MyVideoFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.MyVoiceMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OpenChannelAdminMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OpenChannelFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OpenChannelImageFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OpenChannelUserMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OpenChannelVideoFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherImageFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherMultipleFilesMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherTemplateMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherUserMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherVideoFileMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.OtherVoiceMessageViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.ParentMessageInfoViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.SuggestedRepliesViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.TimelineViewHolder;
import com.sendbird.uikit.internal.ui.viewholders.TypingIndicatorViewHolder;
import com.sendbird.uikit.model.MessageListUIParams;
import com.sendbird.uikit.model.SuggestedRepliesMessage;
import com.sendbird.uikit.model.TimelineMessage;
import com.sendbird.uikit.model.TypingIndicatorMessage;
import com.sendbird.uikit.utils.MessageUtils;

/**
 * A Factory manages a type of messages.
 */
public class MessageViewHolderFactory {
    /**
     * Create a view holder that matches {@link MessageType} for {@code OpenChannel}.
     *
     * @param inflater Inflater that creates a view
     * @param parent The parent view to which the view holder is attached
     * @param viewType The type of message you want to create
     * @param useMessageGroupUI Whether to show the view holder as a grouped message UI
     * @return Returns {@link MessageViewHolder} that matches {@link MessageType}.
     * @deprecated 3.3.0
     */
    @NonNull
    @Deprecated
    public static MessageViewHolder createOpenChannelViewHolder(@NonNull LayoutInflater inflater,
                                                                @NonNull ViewGroup parent,
                                                                @NonNull MessageType viewType,
                                                                boolean useMessageGroupUI) {
        return createOpenChannelViewHolder(
            inflater,
            parent,
            viewType,
            new MessageListUIParams.Builder().setUseMessageGroupUI(useMessageGroupUI).build()
        );
    }

    /**
     * Create a view holder that matches {@link MessageType} for {@code OpenChannel}.
     *
     * @param inflater Inflater that creates a view
     * @param parent The parent view to which the view holder is attached
     * @param viewType The type of message you want to create
     * @param messageListUIParams The {@link MessageListUIParams} that contains drawing parameters
     * @return Returns {@link MessageViewHolder} that matches {@link MessageType}.
     * since 3.3.0
     */
    @NonNull
    public static MessageViewHolder createOpenChannelViewHolder(@NonNull LayoutInflater inflater,
                                                                @NonNull ViewGroup parent,
                                                                @NonNull MessageType viewType,
                                                                @NonNull MessageListUIParams messageListUIParams) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_FILE_MESSAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OpenChannelFileMessageViewHolder(SbViewOpenChannelFileMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OpenChannelImageFileMessageViewHolder(SbViewOpenChannelFileImageMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OpenChannelVideoFileMessageViewHolder(SbViewOpenChannelFileVideoMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(SbViewTimeLineMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                holder = new OpenChannelAdminMessageViewHolder(SbViewOpenChannelAdminMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_USER_MESSAGE_ME:
            case VIEW_TYPE_USER_MESSAGE_OTHER:
            default:
                // user message type & unknown message type
                holder = new OpenChannelUserMessageViewHolder(SbViewOpenChannelUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
        }
        return holder;
    }

    /**
     * Create a view holder that matches {@link MessageType} for {@code GroupChannel}.
     *
     * @param inflater Inflater that creates a view
     * @param parent The parent view to which the view holder is attached
     * @param viewType The type of message you want to create
     * @param useMessageGroupUI Whether to show the view holder as a grouped message UI
     * @return Returns {@link MessageViewHolder} that matches {@link MessageType}.
     * @deprecated 3.3.0
     */
    @NonNull
    @Deprecated
    public static MessageViewHolder createViewHolder(@NonNull LayoutInflater inflater,
                                                     @NonNull ViewGroup parent,
                                                     @NonNull MessageType viewType,
                                                     boolean useMessageGroupUI) {
        return createViewHolder(
            inflater,
            parent,
            viewType,
            new MessageListUIParams.Builder().setUseMessageGroupUI(useMessageGroupUI).build()
        );
    }

    /**
     * Create a view holder that matches {@link MessageType} for {@code GroupChannel}.
     *
     * @param inflater Inflater that creates a view
     * @param parent The parent view to which the view holder is attached
     * @param viewType The type of message you want to create
     * @param messageListUIParams The {@link MessageListUIParams} that contains drawing parameters
     * @return Returns {@link MessageViewHolder} that matches {@link MessageType}.
     * since 3.3.0
     */
    @NonNull
    public static MessageViewHolder createViewHolder(@NonNull LayoutInflater inflater,
                                                     @NonNull ViewGroup parent,
                                                     @NonNull MessageType viewType,
                                                     @NonNull MessageListUIParams messageListUIParams) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                holder = new MyUserMessageViewHolder(SbViewMyUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                holder = new OtherUserMessageViewHolder(SbViewOtherUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_ME:
                holder = new MyFileMessageViewHolder(SbViewMyFileMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OtherFileMessageViewHolder(SbViewOtherFileMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                holder = new MyImageFileMessageViewHolder(SbViewMyFileImageMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OtherImageFileMessageViewHolder(SbViewOtherFileImageMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                holder = new MyVideoFileMessageViewHolder(SbViewMyFileVideoMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OtherVideoFileMessageViewHolder(SbViewOtherFileVideoMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME:
                holder = new MyMultipleFilesMessageViewHolder(SbViewMyMultipleFilesMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_MULTIPLE_FILES_MESSAGE_OTHER:
                holder = new OtherMultipleFilesMessageViewHolder(SbViewOtherMultipleFilesMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(SbViewTimeLineMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                holder = new AdminMessageViewHolder(SbViewAdminMessageBinding.inflate(inflater, parent, false), new MessageListUIParams.Builder().setUseMessageGroupUI(false).build());
                break;
            case VIEW_TYPE_PARENT_MESSAGE_INFO:
                holder = new ParentMessageInfoViewHolder(SbViewParentMessageInfoHolderBinding.inflate(inflater, parent, false));
                break;
            case VIEW_TYPE_VOICE_MESSAGE_ME:
                holder = new MyVoiceMessageViewHolder(SbViewMyVoiceMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_VOICE_MESSAGE_OTHER:
                holder = new OtherVoiceMessageViewHolder(SbViewOtherVoiceMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_SUGGESTED_REPLIES:
                holder = new SuggestedRepliesViewHolder(SbViewSuggestedRepliesMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_FORM_TYPE_MESSAGE:
                holder = new FormMessageViewHolder(SbViewFormMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_TYPING_INDICATOR:
                holder = new TypingIndicatorViewHolder(SbViewTypingIndicatorMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            case VIEW_TYPE_TEMPLATE_MESSAGE_OTHER:
                holder = new OtherTemplateMessageViewHolder(SbViewOtherTemplateMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                break;
            default:
                // unknown message type
                if (viewType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME) {
                    holder = new MyUserMessageViewHolder(SbViewMyUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                } else {
                    holder = new OtherUserMessageViewHolder(SbViewOtherUserMessageBinding.inflate(inflater, parent, false), messageListUIParams);
                }
        }
        return holder;
    }

    /**
     * Return the type of message as an integer.
     *
     * @param message Message to know the type.
     * @return Type of message as an integer.
     */
    public static int getViewType(@NonNull BaseMessage message) {
        return getMessageType(message).getValue();
    }

    /**
     * Return the type of message as {@link MessageType}.
     *
     * @param message Message to know the type.
     * @return Type of message as {@link MessageType}.
     */
    @NonNull
    public static MessageType getMessageType(@NonNull BaseMessage message) {
        MessageType type;

        MessageTemplateStatus messageTemplateStatus = MessageTemplateExtensionsKt.getMessageTemplateStatus(message);
        if (messageTemplateStatus != null) {
            switch (messageTemplateStatus) {
                case CACHED:
                case LOADING:
                case FAILED_TO_FETCH:
                case FAILED_TO_PARSE:
                    return MessageType.VIEW_TYPE_TEMPLATE_MESSAGE_OTHER;
                case NOT_APPLICABLE:
                    break;
            }
        }

        if (message.getChannelType() == ChannelType.GROUP && !message.getForms().isEmpty()) {
            return MessageType.VIEW_TYPE_FORM_TYPE_MESSAGE;
        }

        if (message instanceof UserMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (message instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) message;
            String mimeType = fileMessage.getType().toLowerCase();
            if (MessageUtils.isVoiceMessage(fileMessage)) {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_VOICE_MESSAGE_ME;
                } else {
                    type = MessageType.VIEW_TYPE_VOICE_MESSAGE_OTHER;
                }
            } else if (mimeType.startsWith(StringSet.image)) {
                if (mimeType.contains(StringSet.svg)) {
                    if (MessageUtils.isMine(message)) {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
                    } else {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
                    }
                } else {
                    // If the sender is current user
                    if (MessageUtils.isMine(message)) {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                    } else {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                    }
                }
            } else if (mimeType.startsWith(StringSet.video)) {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
                } else {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
                }
            } else {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
                } else {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
                }
            }
        } else if (message instanceof MultipleFilesMessage && MessageExtensionsKt.containsOnlyImageFiles((MultipleFilesMessage) message)) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_MULTIPLE_FILES_MESSAGE_OTHER;
            }
        } else if (message instanceof TimelineMessage) {
            type = MessageType.VIEW_TYPE_TIME_LINE;
        } else if (message instanceof AdminMessage) {
            type = MessageType.VIEW_TYPE_ADMIN_MESSAGE;
        } else if (message instanceof SuggestedRepliesMessage) {
            type = MessageType.VIEW_TYPE_SUGGESTED_REPLIES;
        }  else if (message instanceof TypingIndicatorMessage) {
            type = MessageType.VIEW_TYPE_TYPING_INDICATOR;
        } else {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
            }
        }

        return type;
    }
}
