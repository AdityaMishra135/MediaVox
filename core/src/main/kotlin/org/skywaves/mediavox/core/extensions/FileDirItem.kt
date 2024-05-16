package org.skywaves.mediavox.core.extensions

import android.content.Context
import org.skywaves.mediavox.core.models.FileDirItem

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
