package com.sendbird.uikit.internal.model

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import com.sendbird.android.SendbirdChat
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.utils.ClearableScheduledExecutorService
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

internal class VoiceRecorder(
    context: Context,
    private val onUpdateListener: OnUpdateListener? = null,
    private val onProgressUpdateListener: OnProgressUpdateListener? = null
) {

    enum class Status {
        IDLE, COMPLETED, PREPARING, RECORDING
    }

    interface OnUpdateListener {
        @UiThread
        fun onUpdated(status: Status)
    }

    interface OnProgressUpdateListener {
        @UiThread
        fun onProgressUpdated(status: Status, milliseconds: Int, maxDurationMillis: Int)
    }

    private val recorder: MediaRecorder
    val recordFilePath: String
    var status: Status = Status.IDLE
        private set
    var seekTo: Int = 0
        private set

    private var isRunningOnTest: Boolean = false
    private val progressExecutor by lazy { ClearableScheduledExecutorService() }
    private val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }

    private val maxDurationMillis = TimeUnit.MINUTES.toMillis(10).toInt()

    init {
        recorder = createRecorder(context)
        recordFilePath = createRecordFilePath(context)
    }

    @UiThread
    @Synchronized
    fun record() {
        if (status == Status.RECORDING || status == Status.COMPLETED) {
            Logger.w("Recording already started")
            return
        }
        updateStatus(Status.PREPARING)
        val file = File(recordFilePath)
        if (file.exists() && file.length() > 0) {
            file.delete()
        }
        recorder.run {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(1)
            setAudioSamplingRate(11025)
            setAudioEncodingBitRate(12000)
            setOutputFile(recordFilePath)
            setMaxDuration(maxDurationMillis)
            SendbirdChat.appInfo?.let {
                setMaxFileSize(it.uploadSizeLimit)
            }
            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Logger.i("VoiceRecorder >> MEDIA_RECORDER_INFO_MAX_DURATION_REACHED")
                    this@VoiceRecorder.complete()
                }

                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    Logger.i("VoiceRecorder >> MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED")
                    this@VoiceRecorder.complete()
                }

                if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN ||
                    what == MediaRecorder.MEDIA_ERROR_SERVER_DIED
                ) {
                    Logger.i("VoiceRecorder >> MEDIA_RECORDER_ERROR")
                    this@VoiceRecorder.complete()
                }
            }
            try {
                prepare()
                start()
                updateStatus(Status.RECORDING)
                startRecordTimer()
            } catch (e: Throwable) {
                Logger.w(e)
                this@VoiceRecorder.cancel(true)
                return@run
            }
        }
    }

    @UiThread
    @Synchronized
    fun complete() {
        if (status == Status.COMPLETED) return
        if (status == Status.PREPARING) {
            seekTo = 0
            File(recordFilePath).delete()
        }
        progressExecutor.shutdownNow()
        try {
            recorder.reset()
            recorder.release()
        } catch (e: Throwable) {
            Logger.w(e)
        }
        updateStatus(Status.COMPLETED)
    }

    @UiThread
    @Synchronized
    fun cancel(reset: Boolean = false) {
        if (status == Status.COMPLETED) return
        seekTo = 0
        File(recordFilePath).delete()
        if (!reset) {
            complete()
        } else {
            progressExecutor.cancelAllJobs(true)

            try {
                recorder.reset()
            } catch (e: Throwable) {
                Logger.w(e)
            }
            updateStatus(Status.IDLE)
        }
    }

    @UiThread
    @Synchronized
    private fun updateStatus(status: Status) {
        if (this.status == status) return
        this.status = status
        onUpdateListener?.onUpdated(this.status)
    }

    @UiThread
    @Synchronized
    private fun updateProgress(milliseconds: Int) {
        onProgressUpdateListener?.onProgressUpdated(
            status,
            milliseconds,
            maxDurationMillis
        )
    }

    private fun createRecorder(context: Context): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()

    private fun createRecordFilePath(context: Context): String {
        return "${context.cacheDir?.absolutePath}/record-${System.currentTimeMillis()}.${StringSet.m4a}"
    }

    private fun startRecordTimer() {
        progressExecutor.cancelAllJobs(true)
        progressExecutor.scheduleAtFixedRate({
            runOnUiThread {
                updateProgress(seekTo)
                seekTo += 100
            }
        }, 0, 100, TimeUnit.MILLISECONDS)
    }

    private fun <T> T?.runOnUiThread(block: (T) -> Unit) {
        if (this != null) {
            if (isRunningOnTest) {
                thread { block(this) }
            } else {
                uiThreadHandler.post { block(this) }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    constructor(
        context: Context,
        isRunningOnTest: Boolean,
        onUpdateListener: OnUpdateListener? = null,
        onProgressUpdateListener: OnProgressUpdateListener? = null
    ) : this(context, onUpdateListener, onProgressUpdateListener) {
        this.isRunningOnTest = isRunningOnTest
    }
}
