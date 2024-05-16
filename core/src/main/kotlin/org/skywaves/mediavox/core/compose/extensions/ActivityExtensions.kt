package org.skywaves.mediavox.core.compose.extensions

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.skywaves.mediavox.core.R
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.core.helpers.isOreoMr1Plus
import org.skywaves.mediavox.core.models.Release

const val DEVELOPER_PLAY_STORE_URL = "https://play.google.com/store/apps/dev?id=7297838378654322558"
const val FAKE_VERSION_APP_LABEL =
    "You are using a fake version of the app. For your own safety download the original one from www.fossify.org. Thanks"

fun Context.fakeVersionCheck(
    showConfirmationDialog: () -> Unit
) {
    if (!packageName.startsWith("org.skywaves.", true)) {
        if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
            showConfirmationDialog()
        }
    }
}

fun ComponentActivity.appOnSdCardCheckCompose(
    showConfirmationDialog: () -> Unit
) {
    if (!baseConfig.wasAppOnSDShown && isAppInstalledOnSDCard()) {
        baseConfig.wasAppOnSDShown = true
        showConfirmationDialog()
    }
}

