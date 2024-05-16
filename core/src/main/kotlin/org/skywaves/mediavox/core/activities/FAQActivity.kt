package org.skywaves.mediavox.core.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.toImmutableList
import org.skywaves.mediavox.core.compose.extensions.enableEdgeToEdgeSimple
import org.skywaves.mediavox.core.compose.screens.FAQScreen
import org.skywaves.mediavox.core.compose.theme.AppThemeSurface
import org.skywaves.mediavox.core.helpers.APP_FAQ
import org.skywaves.mediavox.core.models.FAQItem

class FAQActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val faqItems = remember { intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem> }
                FAQScreen(
                    goBack = ::finish,
                    faqItems = faqItems.toImmutableList()
                )
            }
        }
    }
}
