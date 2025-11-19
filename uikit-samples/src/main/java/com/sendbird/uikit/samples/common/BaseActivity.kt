package com.sendbird.uikit.samples.common

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.common.extensions.authenticate
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.utils.ContextUtils
import com.sendbird.uikit.utils.DialogUtils
import java.util.UUID

open class BaseActivity : AppCompatActivity() {

    private val connectionHandlerId = this.javaClass.getSimpleName() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8)

    private var connectionDelayedDialog: AlertDialog? = null
    private var connectionDelayed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        addConnectionDelayedHandler()
    }

    override fun onPause() {
        super.onPause()
        SendbirdChat.removeConnectionHandler(connectionHandlerId)
    }

    private fun addConnectionDelayedHandler() {
        SendbirdChat.addConnectionHandler(connectionHandlerId, object: ConnectionHandler {

            override fun onConnected(userId: String) {}

            override fun onConnectionDelayed(retryAfter: Long) {
                super.onConnectionDelayed(retryAfter)
                Logger.dev(">> BaseViewModel::onConnectionDelayed() retryAfter: %d", retryAfter)
                connectionDelayed = true
                showConnectionDelayedDialog(retryAfter)
            }

            override fun onDisconnected(userId: String) {}

            override fun onReconnectFailed() {}

            override fun onReconnectStarted() {}

            override fun onReconnectSucceeded() {
                WaitingDialog.dismiss()

                if (connectionDelayed) {
                    onConnectedAfterDelay()
                    connectionDelayed = false
                }
            }
        });
    }

    /**
     * Show a dialog that notifies the user that the connection is delayed.
     * Called when [ConnectionHandler.onConnectionDelayed] is invoked.
     *
     * @param retryAfter The time in seconds after which the next reconnection attempt will be made.
     */
    protected open fun showConnectionDelayedDialog(retryAfter: Long) {
        Logger.d("retryAfter: $retryAfter")
        WaitingDialog.dismiss()
        hideConnectionDelayedDialog()

        connectionDelayedDialog = DialogUtils.showConnectionDelayedDialog(
            this@BaseActivity,
            retryAfter
        ) {
            onClickConnectionRefresh()
        }
    }

    /**
     * Hide the connection delayed dialog if it is showing.
     * Called when the connection is re-established after a delay.
     */
    protected open fun hideConnectionDelayedDialog() {
        Logger.d("showing: ${connectionDelayedDialog?.isShowing}")
        if (connectionDelayedDialog?.isShowing == true) {
            connectionDelayedDialog?.dismiss()
        }
        connectionDelayedDialog = null
    }

    /**
     * Called when the user clicks the "Refresh" button in the connection delayed dialog.
     * Attempts to re-authenticate the user and re-establish the connection.
     */
    protected open fun onClickConnectionRefresh() {
        WaitingDialog.show(this)

        authenticate { _, exception ->
            WaitingDialog.dismiss()
            if (exception != null) {
                ContextUtils.toastError(this@BaseActivity, "${exception.message}")
            } else {
                onConnectedAfterDelay()
            }
        }
    }

    /**
     * Called when the connection is successfully re-established after a delay.
     * Dismisses the waiting dialog and hides the connection delayed dialog.
     */
    @CallSuper
    protected open fun onConnectedAfterDelay() {
        WaitingDialog.dismiss()
        hideConnectionDelayedDialog()
    }
}
