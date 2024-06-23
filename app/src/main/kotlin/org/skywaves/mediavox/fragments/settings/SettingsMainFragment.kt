package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsMainFragment : SettingsBaseFragment() {
    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.settings
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.generalSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsGeneralFragment.instance)
            }
        }
        view.findViewById<View>(R.id.themeSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsThemeFragment.instance)
            }
        }
        view.findViewById<View>(R.id.uiCOnfigurationSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsUiConfigurationFragment.instance)
            }
        }
        view.findViewById<View>(R.id.nowPlayingSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsMediaIndexerFragment.instance)
            }
        }
        view.findViewById<View>(R.id.audioSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsMediaConfigurationFragment.instance)
            }
        }
        view.findViewById<View>(R.id.contributorsSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsMediaOperationFragment.instance)
            }
        }
        view.findViewById<View>(R.id.donationSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsSecurityFragment.instance)
            }
        }
        view.findViewById<View>(R.id.migrationSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsMigrationFragment.instance)
            }
        }
        view.findViewById<View>(R.id.aboutSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsAboutFragment.instance)
            }
        }
    }

    private fun openSettingsFragment(fragment: SettingsBaseFragment) {
        if (mListener is SettingsActivity) {
            mListener.changeFragment(fragment)
        }
    }

    companion object {
        val TAG: String = SettingsMainFragment::class.java.simpleName

        val instance: SettingsMainFragment
            get() = SettingsMainFragment()
    }
}