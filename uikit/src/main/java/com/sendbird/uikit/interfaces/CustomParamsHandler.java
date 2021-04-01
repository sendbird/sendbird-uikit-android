package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.OpenChannelParams;
import com.sendbird.android.UserMessageParams;

/**
 * Interface definition for a callback to be invoked before when each operation called.
 *
 * @since 1.2.2
 */
public interface CustomParamsHandler {

    /**
     * It will be called before creating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelParams}.
     * @since 1.2.2
     */
    void onBeforeCreateGroupChannel(@NonNull GroupChannelParams params);

    /**
     * It will be called before updating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelParams}.
     * @since 1.2.2
     */
    void onBeforeUpdateGroupChannel(@NonNull GroupChannelParams params);

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     * @since 1.2.2
     */
    void onBeforeSendUserMessage(@NonNull UserMessageParams params);

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of file message. Refer to {@link FileMessageParams}.
     * @since 1.2.2
     */
    void onBeforeSendFileMessage(@NonNull FileMessageParams params);

    /**
     * It will be called before updating message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     * @since 1.2.2
     */
    void onBeforeUpdateUserMessage(@NonNull UserMessageParams params);

    /**
     * It will be called before updating open channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link OpenChannelParams}.
     * @since 2.0.0
     */
    void onBeforeUpdateOpenChannel(@NonNull OpenChannelParams params);
}
