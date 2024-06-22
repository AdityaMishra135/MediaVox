package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.databinding.FragmentSettingsThemeBinding
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsThemeFragment : SettingsBaseFragment() {
    private var _binding: FragmentSettingsThemeBinding? = null
    private val binding get() = _binding!!
    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.theme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsThemeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



    companion object {
        val TAG: String = SettingsThemeFragment::class.java.simpleName

        val instance: SettingsThemeFragment
            get() = SettingsThemeFragment()
    }
}