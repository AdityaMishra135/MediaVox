package org.skywaves.mediavox.activities

import PersonalizeFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.extensions.viewBinding
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.databinding.ActivityCustomSettingsBinding
import org.skywaves.mediavox.fragments.settings.ThemeFragment

class CustomSettingsActivity : SimpleActivity() {

    private val binding by viewBinding(ActivityCustomSettingsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateMaterialActivityViews(binding.customSettingsCoordinator, binding.customSettingHolder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.customSettingsNestedScrollview, binding.customSettingsToolbar)

        // Handle clicks on sections
        binding.sectionTheme.setOnClickListener {
            loadFragment(ThemeFragment())
        }

        binding.sectionPersonalize.setOnClickListener {
            loadFragment(PersonalizeFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.customSettingsToolbar, NavigationIcon.Arrow)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Optional: Add to back stack for fragment navigation
            .commit()
    }

}
