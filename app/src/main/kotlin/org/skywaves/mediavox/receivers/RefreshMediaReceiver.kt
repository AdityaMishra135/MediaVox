package org.skywaves.mediavox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.skywaves.mediavox.core.helpers.REFRESH_PATH
import org.skywaves.mediavox.extensions.addPathToDB

class RefreshMediaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra(REFRESH_PATH) ?: return
        context.addPathToDB(path)
    }
}
