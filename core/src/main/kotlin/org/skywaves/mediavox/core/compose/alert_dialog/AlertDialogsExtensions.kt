package org.skywaves.mediavox.core.compose.alert_dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import org.skywaves.mediavox.core.R
import org.skywaves.mediavox.core.compose.theme.LocalTheme
import org.skywaves.mediavox.core.compose.theme.Shapes
import org.skywaves.mediavox.core.compose.theme.SimpleTheme
import org.skywaves.mediavox.core.compose.theme.light_grey_stroke
import org.skywaves.mediavox.core.compose.theme.model.Theme
import org.skywaves.mediavox.core.extensions.baseConfig
import org.skywaves.mediavox.core.helpers.isSPlus

val dialogContainerColor
    @ReadOnlyComposable
    @Composable get() = when (LocalTheme.current) {
        is Theme.BlackAndWhite -> Color.Black
        is Theme.SystemDefaultMaterialYou -> if (isSPlus()) colorResource(R.color.you_dialog_background_color) else SimpleTheme.colorScheme.surface
        else -> {
            val context = LocalContext.current
            Color(context.baseConfig.backgroundColor)
        }
    }

val Modifier.dialogBackgroundShapeAndBorder: Modifier
    @ReadOnlyComposable
    @Composable get() = then(
        Modifier
            .fillMaxWidth()
            .background(dialogContainerColor, dialogShape)
            .dialogBorder
    )

val dialogShape = Shapes.extraLarge

val dialogElevation = 0.dp

val dialogTextColor @Composable @ReadOnlyComposable get() = SimpleTheme.colorScheme.onSurface

val Modifier.dialogBorder: Modifier
    @ReadOnlyComposable
    @Composable get() =
        when (LocalTheme.current) {
            is Theme.BlackAndWhite -> then(Modifier.border(1.dp, light_grey_stroke, dialogShape))
            else -> Modifier
        }

@Composable
fun DialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .dialogBorder,
        shape = dialogShape,
        color = dialogContainerColor,
        tonalElevation = dialogElevation,
    ) {
        content()
    }
}

@Composable
fun ShowKeyboardWhenDialogIsOpenedAndRequestFocus(
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    focusRequester: FocusRequester?
) {
    LaunchedEffect(Unit) {
        //await two frames to render the scrim and the dialog
        awaitFrame()
        awaitFrame()
        keyboardController?.show()
        focusRequester?.requestFocus()
    }
}
