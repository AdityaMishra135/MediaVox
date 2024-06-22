package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.CustomSettingsActivity
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
        view.findViewById<View>(R.id.nowPlayingSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsNowPlayingFragment.instance)
            }
        }
        view.findViewById<View>(R.id.audioSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsAudioFragment.instance)
            }
        }
        view.findViewById<View>(R.id.contributorsSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsContributorsFragment.instance)
            }
        }
        view.findViewById<View>(R.id.donationSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsDonationFragment.instance)
            }
        }
        view.findViewById<View>(R.id.aboutSettings).setOnClickListener { v: View ->
            v.postOnAnimation {
                openSettingsFragment(SettingsAboutFragment.instance)
            }
        }
    }

    private fun openSettingsFragment(fragment: SettingsBaseFragment) {
        if (mListener is CustomSettingsActivity) {
            mListener.changeFragment(fragment)
        }
    }

    companion object {
        val TAG: String = SettingsMainFragment::class.java.simpleName

        val instance: SettingsMainFragment
            get() = SettingsMainFragment()
    }
}