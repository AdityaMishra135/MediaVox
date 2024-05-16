package org.skywaves.mediavox.core.compose.extensions

import android.app.Activity
import android.content.Context
import org.skywaves.mediavox.core.R
import org.skywaves.mediavox.core.extensions.baseConfig
import org.skywaves.mediavox.core.extensions.redirectToRateUs
import org.skywaves.mediavox.core.extensions.toast
import org.skywaves.mediavox.core.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)

fun Activity.rateStarsRedirectAndThankYou(stars: Int) {
    if (stars == 5) {
        redirectToRateUs()
    }
    toast(R.string.thank_you)
    baseConfig.wasAppRated = true
}
