package com.sendbird.uikit.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.Sender;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentPhotoViewBinding;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.vm.FileDownloader;

public class PhotoViewFragment extends BaseFragment implements PermissionFragment.IPermissionHandler, LoadingDialogHandler {
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SbFragmentPhotoViewBinding binding;
    private BaseChannel channel;

    private boolean loadComplete = false;

    private String senderId;
    private String fileName;
    private String channelUrl;
    private String url;
    private String mimeType;
    private String senderNickname;
    private long createdAt;
    private long messageId;
    private boolean isDeletableMessage;
    private BaseChannel.ChannelType channelType = BaseChannel.ChannelType.GROUP;
    private LoadingDialogHandler loadingDialogHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_photo_view, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d("PhotoViewFragment::onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        binding.ivClose.setOnClickListener(v -> finish());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.background_700));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LOW_PROFILE |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onReady(User user, ReadyStatus status) {
        onConfigure();

        if (channelType == BaseChannel.ChannelType.GROUP) {
            GroupChannel.getChannel(channelUrl, (channel, e) -> {
                PhotoViewFragment.this.channel = channel;
                onDrawPage();
            });
        } else {
            OpenChannel.getChannel(channelUrl, (channel, e) -> {
                PhotoViewFragment.this.channel = channel;
                onDrawPage();
            });
        }
    }

    protected void onConfigure() {
        Logger.d("PhotoViewFragment::onConfigure()");
        Bundle args = getArguments();
        if (args != null) {
            senderId = args.getString(StringSet.KEY_SENDER_ID);
            fileName = args.getString(StringSet.KEY_MESSAGE_FILENAME);
            channelUrl = args.getString(StringSet.KEY_CHANNEL_URL);
            url = args.getString(StringSet.KEY_IMAGE_URL);
            mimeType = args.getString(StringSet.KEY_MESSAGE_MIMETYPE);
            senderNickname = args.getString(StringSet.KEY_MESSAGE_SENDER_NAME);
            createdAt = args.getLong(StringSet.KEY_MESSAGE_CREATEDAT);
            messageId = args.getLong(StringSet.KEY_MESSAGE_ID);
            isDeletableMessage = args.getBoolean(StringSet.KEY_DELETABLE_MESSAGE, MessageUtils.isMine(senderId));

            if (args.containsKey(StringSet.KEY_CHANNEL_TYPE)) {
                channelType = (BaseChannel.ChannelType) args.getSerializable(StringSet.KEY_CHANNEL_TYPE);
            }
        }

        if (loadingDialogHandler == null) {
            loadingDialogHandler = this;
        }
    }

    private <T> RequestBuilder<T> makeRequestBuilder(@NonNull String url, @NonNull Class<T> clazz) {
        final View loading = binding.loading;
        final RequestManager glide = Glide.with(this);
        return glide.as(clazz).diskCacheStrategy(DiskCacheStrategy.ALL).load(url).thumbnail(0.5f).listener(new RequestListener<T>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<T> target, boolean isFirstResource) {
                if (!isActive()) return false;
                getActivity().runOnUiThread(() -> loading.setVisibility(View.GONE));
                return false;
            }

            @Override
            public boolean onResourceReady(T resource, Object model, Target<T> target, DataSource dataSource, boolean isFirstResource) {
                if (!isActive()) return false;
                getActivity().runOnUiThread(() -> {
                    loadComplete = true;
                    loading.setVisibility(View.GONE);
                });
                return false;
            }
        });
    }

    protected void onDrawPage() {
        Logger.d("PhotoViewFragment::onDrawPage() - nickname:" + senderNickname);
        final ImageView ivPhoto = binding.ivPhoto;
        final ImageView ivDelete = binding.ivDelete;
        final ImageView ivDownload = binding.ivDownload;
        final TextView tvTitle = binding.tvTitle;
        final TextView tvCreatedAt = binding.tvCreatedAt;
        final View loading = binding.loading;
        final String url = this.url;

        tvTitle.setText(senderNickname);
        tvCreatedAt.setText(DateUtils.formatTime(getContext(), this.createdAt));
        loading.setVisibility(View.VISIBLE);

        if (mimeType.toLowerCase().contains(StringSet.gif)) {
            makeRequestBuilder(url, GifDrawable.class).into(ivPhoto);
        } else {
            makeRequestBuilder(url, Bitmap.class).into(ivPhoto);
        }

        if (channel != null && isDeletableMessage) {
            ivDelete.setVisibility(View.VISIBLE);
            ivDelete.setOnClickListener(v -> {
                        if (!loadComplete || getContext() == null || getFragmentManager() == null) return;

                        DialogUtils.buildWarning(
                                getString(R.string.sb_text_dialog_delete_file_message),
                                (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                                getString(R.string.sb_text_button_delete),
                                v1 -> channel.deleteMessage(createDummyMessage(), e -> {
                                    if (e != null) {
                                        toastError(R.string.sb_text_error_delete_message);
                                        return;
                                    }
                                    if (isActive()) {
                                        finish();
                                    }
                                }),
                                getString(R.string.sb_text_button_cancel),
                                cancel -> {
                                    Logger.dev("cancel");
                                }).showSingle(getFragmentManager());
                    }
            );
        } else {
            ivDelete.setVisibility(View.GONE);
            ivDelete.setOnClickListener(null);
        }

        ivDownload.setOnClickListener(v -> {
            if (!loadComplete) return;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                download();
            } else {
                checkPermission(0, this);
            }
        });

        PhotoViewAttacher attacher = new PhotoViewAttacher(ivPhoto);
        attacher.setOnPhotoTapListener((view, x, y) -> {
            togglePhotoActionBar();
        });
    }

    private void togglePhotoActionBar() {
        View vgHeader = binding.vgHeader;
        View vgBottom = binding.vgBottom;

        if (vgHeader.getVisibility() == View.GONE) {
            vgHeader.animate()
                    .setDuration(300)
                    .alpha(1.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            vgHeader.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            vgHeader.animate()
                    .setDuration(300)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            vgHeader.setVisibility(View.GONE);
                        }
                    });
        }

        if (vgBottom.getVisibility() == View.GONE) {
            vgBottom.animate()
                    .setDuration(300)
                    .alpha(1.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            vgBottom.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            vgBottom.animate()
                    .setDuration(300)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            vgBottom.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public String[] getPermissions(int requestCode) {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    public void onPermissionGranted(int requestCode) {
        download();
    }

    private void download() {
        loadingDialogHandler.shouldShowLoadingDialog();
        TaskQueue.addTask(new JobResultTask<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                FileDownloader.getInstance().saveFile(getContext(), url, mimeType, fileName);
                Logger.dev("++ file name : %s", fileName);
                return true;
            }

            @Override
            public void onResultForUiThread(Boolean result, SendBirdException e) {
                loadingDialogHandler.shouldDismissLoadingDialog();
                if (e != null) {
                    Logger.e(e);
                }
                if (result != null && result) {
                    toastSuccess(R.string.sb_text_toast_success_download_file);
                } else {
                    toastError(R.string.sb_text_error_download_file);
                }
            }
        });
    }

    private void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 1.2.5
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        showWaitingDialog();
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 1.2.5
     */
    @Override
    public void shouldDismissLoadingDialog() {
        dismissWaitingDialog();
    }

    private BaseMessage createDummyMessage() {
        return new BaseMessage(channelUrl, messageId, createdAt) {

            @Override
            public String getRequestId() {
                return null;
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            public Sender getSender() {
                return null;
            }
        };
    }

    public static class Builder {
        private final Bundle bundle = new Bundle();
        private LoadingDialogHandler loadingDialogHandler;

        /**
         * Constructor
         *
         * @param senderId sender user id
         * @param fileName the file name
         * @param channelUrl
         * @param url
         * @param mimeType
         * @param senderNickname
         * @param createdAt
         * @param messageId
         * @param channelType
         * @param themeMode
         *
         * @deprecated As of 2.2.0, replaced by {@link Builder(String, String, String, String, String, String, long, long, SendBirdUIKit.ThemeMode, boolean)}
         */
        @Deprecated
        public Builder(String senderId, String fileName, String channelUrl,
                       String url, String mimeType, String senderNickname, long createdAt,
                       long messageId, BaseChannel.ChannelType channelType, SendBirdUIKit.ThemeMode themeMode) {
            this(senderId, fileName, channelUrl, url, mimeType, senderNickname, createdAt, messageId, channelType, themeMode, MessageUtils.isMine(senderId));
        }

        public Builder(String senderId, String fileName, String channelUrl,
                       String url, String mimeType, String senderNickname, long createdAt,
                       long messageId, BaseChannel.ChannelType channelType, SendBirdUIKit.ThemeMode themeMode, boolean isDeletableMessage) {
            bundle.putString(StringSet.KEY_SENDER_ID, senderId);
            bundle.putString(StringSet.KEY_MESSAGE_FILENAME, fileName);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
            bundle.putString(StringSet.KEY_IMAGE_URL, url);
            bundle.putString(StringSet.KEY_MESSAGE_MIMETYPE, mimeType);
            bundle.putString(StringSet.KEY_MESSAGE_SENDER_NAME, senderNickname);
            bundle.putLong(StringSet.KEY_MESSAGE_CREATEDAT, createdAt);
            bundle.putLong(StringSet.KEY_MESSAGE_ID, messageId);
            bundle.putSerializable(StringSet.KEY_CHANNEL_TYPE, channelType);
            bundle.putBoolean(StringSet.KEY_DELETABLE_MESSAGE, isDeletableMessage);
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * @since 1.2.5
         */
        public Builder setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        public PhotoViewFragment build() {
            PhotoViewFragment fragment = new PhotoViewFragment();
            fragment.setArguments(bundle);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
