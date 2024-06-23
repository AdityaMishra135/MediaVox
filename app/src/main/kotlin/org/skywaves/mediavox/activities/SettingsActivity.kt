package org.skywaves.mediavox.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.extensions.viewBinding
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.databinding.ActivitySettingsBinding
import org.skywaves.mediavox.fragments.ExcludeFoldersFragment
import org.skywaves.mediavox.fragments.HiddenFoldersFragment
import org.skywaves.mediavox.fragments.IncludeFoldersFragment
import org.skywaves.mediavox.fragments.settings.SettingsMainFragment
import org.skywaves.mediavox.fragments.settings.SettingsThemeFragment
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.fragments.settings.base.SettingsFragmentsListener


class SettingsActivity : SimpleActivity(), SettingsFragmentsListener {

    val binding by viewBinding(ActivitySettingsBinding::inflate)
    public var mPulseToolbar: Toolbar? = null
    private var mFragmentManager: FragmentManager? = null
    private val mRestartDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mFragmentManager = supportFragmentManager
        mPulseToolbar = binding.settingsToolbar
        setSupportActionBar(mPulseToolbar)
        setToolbarTitle(org.skywaves.mediavox.core.R.string.settings)
        mPulseToolbar!!.setNavigationOnClickListener { v -> onBackPressed() }

        if (null == savedInstanceState) {
            //Set up the main fragment when activity is first created
            mFragmentManager!!.beginTransaction()
                .replace(R.id.settings_content_container, SettingsMainFragment.instance, SettingsMainFragment.TAG)
                .commit()
        }

    }

    override fun changeFragment(fragment: SettingsBaseFragment?) {
        mFragmentManager!!.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
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
        val currentFragment = supportFragmentManager.findFragmentById(R.id.settings_content_container)
        if ((supportFragmentManager.findFragmentById(R.id.settings_content_container)) is SettingsThemeFragment){
            (currentFragment as SettingsThemeFragment).handleBackPressed()
        }
        if ((supportFragmentManager.findFragmentById(R.id.settings_content_container)) is HiddenFoldersFragment){
            (currentFragment as HiddenFoldersFragment).handleBackPressed()
        }
        if ((supportFragmentManager.findFragmentById(R.id.settings_content_container)) is ExcludeFoldersFragment){
            (currentFragment as ExcludeFoldersFragment).handleBackPressed()
        }
        if ((supportFragmentManager.findFragmentById(R.id.settings_content_container)) is IncludeFoldersFragment){
            (currentFragment as IncludeFoldersFragment).handleBackPressed()
        }
        else if (mFragmentManager!!.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            mPulseToolbar!!.title = getString(org.skywaves.mediavox.core.R.string.settings)
            mFragmentManager!!.popBackStack()
        }
    }

    override fun onResume() {
        setupToolbar2(mPulseToolbar!!, NavigationIcon.Arrow)
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(org.skywaves.mediavox.core.R.menu.menu_customization, menu)
        return true
    }
}
