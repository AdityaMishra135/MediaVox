package org.skywaves.mediavox.activities

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.extensions.viewBinding
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.databinding.ActivityCustomSettingsBinding
import org.skywaves.mediavox.fragments.settings.SettingsMainFragment
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.fragments.settings.base.SettingsFragmentsListener


class CustomSettingsActivity : SimpleActivity(), SettingsFragmentsListener {

    private val binding by viewBinding(ActivityCustomSettingsBinding::inflate)
    private var mPulseToolbar: Toolbar? = null
    private var mFragmentManager: FragmentManager? = null
    private val mRestartDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mFragmentManager = supportFragmentManager
        mPulseToolbar = binding.settingsToolbar
        setToolbarTitle(org.skywaves.mediavox.core.R.string.settings)
        mPulseToolbar!!.setNavigationOnClickListener { v -> onBackPressed() }

        updateMaterialActivityViews(binding.customSetting, binding.customHolder, useTransparentNavigation = true, useTopSearchMenu = false)

        if (null == savedInstanceState) {
            //Set up the main fragment when activity is first created
            mFragmentManager!!.beginTransaction()
                .replace(R.id.settings_content_container, SettingsMainFragment.getInstance(), SettingsMainFragment.TAG)
                .commit()
        }

    }

    override fun changeFragment(fragment: SettingsBaseFragment?) {
        mFragmentManager!!.beginTransaction()
            .replace(R.id.settings_content_container, fragment!!, fragment.getFragmentTag())
            .addToBackStack(null)
            .commit();
    }

    override fun setToolbarTitle(titleId: Int) {
        mPulseToolbar!!.setTitle(getString(titleId));
    }

    override fun requiresActivityRestart() {
        recreate();
    }

    override fun requiresApplicationRestart(shouldStopPlayback: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        if (mFragmentManager!!.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            mPulseToolbar!!.title = getString(org.skywaves.mediavox.core.R.string.settings)
            mFragmentManager!!.popBackStack()
        }
    }

    override fun onResume() {
        setupToolbar(mPulseToolbar!!, NavigationIcon.Arrow)
        super.onResume()
    }

}
