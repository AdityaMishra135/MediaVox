package org.skywaves.mediavox.core.interfaces

interface HashListener {
    fun receivedHash(hash: String, type: Int)
}
