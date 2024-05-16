package org.skywaves.mediavox.activities

import android.content.Intent
import org.skywaves.mediavox.core.activities.BaseSplashActivity
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.favoritesDB
import org.skywaves.mediavox.extensions.getFavoriteFromPath
import org.skywaves.mediavox.extensions.mediaDB
import org.skywaves.mediavox.models.Favorite

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        // check if previously selected favorite items have been properly migrated into the new Favorites table
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
