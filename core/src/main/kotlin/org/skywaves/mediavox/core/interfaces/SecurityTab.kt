package org.skywaves.mediavox.core.interfaces

import androidx.biometric.auth.AuthPromptHost
import org.skywaves.mediavox.core.views.MyScrollView

interface SecurityTab {
    fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    )

    fun visibilityChanged(isVisible: Boolean)
}
