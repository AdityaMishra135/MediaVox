package org.skywaves.mediavox.core.compose.theme.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
interface CommonTheme {
    val primaryColorInt: Int
    val backgroundColorInt: Int
    val textColorInt: Int

    val primaryColor get() = Color(primaryColorInt)
    val backgroundColor get() = Color(backgroundColorInt)
    val textColor get() = Color(textColorInt)

}
