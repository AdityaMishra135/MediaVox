package org.skywaves.mediavox.core.models

import androidx.compose.runtime.Immutable

@Immutable
data class License(val id: Long, val titleId: Int, val textId: Int, val urlId: Int)
