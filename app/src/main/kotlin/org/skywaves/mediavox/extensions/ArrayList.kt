package org.skywaves.mediavox.extensions

import org.skywaves.mediavox.helpers.*
import org.skywaves.mediavox.models.Medium

fun ArrayList<Medium>.getDirMediaTypes(): Int {
    var types = 0

    if (any { it.isVideo() }) {
        types += TYPE_VIDEOS
    }

    if (any { it.isAudio() }) {
        types += TYPE_AUDIOS
    }

    return types
}
