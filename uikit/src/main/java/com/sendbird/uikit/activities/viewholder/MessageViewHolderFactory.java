package com.sendbird.uikit.activities.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.UserMessage;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewAdminMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyFileImageMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyFileMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyFileVideoMessageBinding;
import com.sendbird.uikit.databinding.SbViewMyUserMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelAdminMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileImageMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileVideoMessageBinding;
import com.sendbird.uikit.databinding.SbViewOpenChannelUserMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherFileImageMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherFileMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherFileVideoMessageBinding;
import com.sendbird.uikit.databinding.SbViewOtherUserMessageBinding;
import com.sendbird.uikit.databinding.SbViewTimeLineMessageBinding;
import com.sendbird.uikit.model.TimelineMessage;
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
     */
    @NonNull
    public static MessageViewHolder createOpenChannelViewHolder(@NonNull LayoutInflater inflater,
                                                                @NonNull ViewGroup parent,
                                                                @NonNull MessageType viewType,
                                                                boolean useMessageGroupUI) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_FILE_MESSAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OpenChannelFileMessageViewHolder(SbViewOpenChannelFileMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OpenChannelImageFileMessageViewHolder(SbViewOpenChannelFileImageMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OpenChannelVideoFileMessageViewHolder(SbViewOpenChannelFileVideoMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(SbViewTimeLineMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                holder = new OpenChannelAdminMessageViewHolder(SbViewOpenChannelAdminMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_USER_MESSAGE_ME:
            case VIEW_TYPE_USER_MESSAGE_OTHER:
            default:
                // user message type & unknown message type
                holder = new OpenChannelUserMessageViewHolder(SbViewOpenChannelUserMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
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
     */
    @NonNull
    public static MessageViewHolder createViewHolder(@NonNull LayoutInflater inflater,
                                                     @NonNull ViewGroup parent,
                                                     @NonNull MessageType viewType,
                                                     boolean useMessageGroupUI) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:

                holder = new MyUserMessageViewHolder(SbViewMyUserMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                holder = new OtherUserMessageViewHolder(SbViewOtherUserMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_ME:
                holder = new MyFileMessageViewHolder(SbViewMyFileMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OtherFileMessageViewHolder(SbViewOtherFileMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                holder = new MyImageFileMessageViewHolder(SbViewMyFileImageMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OtherImageFileMessageViewHolder(SbViewOtherFileImageMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                holder = new MyVideoFileMessageViewHolder(SbViewMyFileVideoMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OtherVideoFileMessageViewHolder(SbViewOtherFileVideoMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(SbViewTimeLineMessageBinding.inflate(inflater, parent, false), false);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                holder = new AdminMessageViewHolder(SbViewAdminMessageBinding.inflate(inflater, parent, false), false);
                break;
            default:
                // unknown message type
                if (viewType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME) {
                    holder = new MyUserMessageViewHolder(SbViewMyUserMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
                } else {
                    holder = new OtherUserMessageViewHolder(SbViewOtherUserMessageBinding.inflate(inflater, parent, false), useMessageGroupUI);
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

        if (message instanceof UserMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (message instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) message;
            String mimeType = fileMessage.getType().toLowerCase();
            if (mimeType.startsWith(StringSet.image)) {
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
        } else if (message instanceof TimelineMessage) {
            type = MessageType.VIEW_TYPE_TIME_LINE;
        } else if (message instanceof AdminMessage) {
            type = MessageType.VIEW_TYPE_ADMIN_MESSAGE;
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
