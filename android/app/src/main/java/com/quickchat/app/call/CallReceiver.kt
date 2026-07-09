package com.quickchat.app.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callManager: WebRtcCallManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_END_CALL") {
            callManager.endCall()
        }
    }
}
