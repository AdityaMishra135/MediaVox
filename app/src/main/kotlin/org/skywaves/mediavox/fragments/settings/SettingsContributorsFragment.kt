package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsContributorsFragment : SettingsBaseFragment() {
    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.contributors
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_contributors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        val TAG: String = SettingsContributorsFragment::class.java.simpleName

        val instance: SettingsContributorsFragment
            get() = SettingsContributorsFragment()
    }
}