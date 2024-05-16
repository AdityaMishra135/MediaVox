package org.skywaves.mediavox.extensions

import org.skywaves.mediavox.core.helpers.SORT_DESCENDING

fun Int.isSortingAscending() = this and SORT_DESCENDING == 0
