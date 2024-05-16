package org.skywaves.mediavox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.extensions.updateDirectoryPath
import org.skywaves.mediavox.helpers.MediaFetcher

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureBackgroundThread {
            MediaFetcher(context).getFoldersToScan().forEach {
                context.updateDirectoryPath(it)
            }
        }
    }
}
