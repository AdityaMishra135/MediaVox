package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsAudioFragment : SettingsBaseFragment() {
    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return R.string.audios
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_audio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val TAG: String = SettingsAudioFragment::class.java.simpleName

        val instance: SettingsAudioFragment
            get() = SettingsAudioFragment()
    }
}