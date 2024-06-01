package org.skywaves.mediavox.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.skywaves.mediavox.core.R
import org.skywaves.mediavox.core.extensions.baseConfig
import org.skywaves.mediavox.core.extensions.isUsingSystemDarkTheme
import org.skywaves.mediavox.core.extensions.viewBinding
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.databinding.ActivitySplashBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.favoritesDB
import org.skywaves.mediavox.extensions.getFavoriteFromPath
import org.skywaves.mediavox.extensions.mediaDB
import org.skywaves.mediavox.models.Favorite

class SplashActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivitySplashBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        baseConfig.apply {
            if (isUsingAutoTheme) {
                val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
                textColor = resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color)
                backgroundColor = resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color)
            }
        }

        if (config.wereFavoritesMigrated) {
            launchActivity()
        } else {
            if (config.appRunCount == 0) {
                config.wereFavoritesMigrated = true
                launchActivity()
            } else {
                config.wereFavoritesMigrated = true
                ensureBackgroundThread {
                    val favorites = ArrayList<Favorite>()
                    val favoritePaths = mediaDB.getFavorites().map { it.path }.toMutableList() as ArrayList<String>
                    favoritePaths.forEach {
                        favorites.add(getFavoriteFromPath(it))
                    }
                    favoritesDB.insertAll(favorites)

                    runOnUiThread {
                        launchActivity()
                    }
                }
            }
        }
    }

    private fun launchActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
