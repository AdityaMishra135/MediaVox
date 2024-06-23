package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.core.dialogs.ChangeDateTimeFormatDialog
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.helpers.isPiePlus
import org.skywaves.mediavox.core.helpers.isTiramisuPlus
import org.skywaves.mediavox.databinding.FragmentSettingsGeneralBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import java.util.Locale
import kotlin.system.exitProcess

class SettingsGeneralFragment : SettingsBaseFragment() {

    private var _binding: FragmentSettingsGeneralBinding? = null
    private val binding get() = _binding!!

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.general_settings
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUseEnglish()
        setupLanguage()
        setupShowNotch()
        setupChangeDateTimeFormat()
        arrayOf(
            binding.settingsLanguage,
            binding.settingsLanguageLabel,
            binding.settingsChangeDateTimeFormat,
        ).forEach {
            it.setTextColor(requireActivity().getProperTextColor())
        }
        binding.settingsShowNotch.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)
        binding.settingsUseEnglish.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)

    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        val TAG: String = SettingsGeneralFragment::class.java.simpleName

        val instance: SettingsGeneralFragment
            get() = SettingsGeneralFragment()
    }

    private fun setupUseEnglish() {
        binding.settingsUseEnglishHolder.beVisibleIf((requireContext().config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
        binding.settingsUseEnglish.isChecked = requireContext().config.useEnglish
        binding.settingsUseEnglishHolder.setOnClickListener {
            binding.settingsUseEnglish.toggle()
            requireContext().config.useEnglish = binding.settingsUseEnglish.isChecked
            exitProcess(0)
        }
    }

    private fun setupLanguage() {
        binding.settingsLanguage.text = Locale.getDefault().displayLanguage
        binding.settingsLanguageHolder.beVisibleIf(isTiramisuPlus())
        binding.settingsLanguageHolder.setOnClickListener {
            (requireActivity() as SettingsActivity).launchChangeAppLanguageIntent()
        }
    }


    private fun setupShowNotch() {
        binding.settingsShowNotchHolder.beVisibleIf(isPiePlus())
        binding.settingsShowNotch.isChecked = requireContext().config.showNotch
        binding.settingsShowNotchHolder.setOnClickListener {
            binding.settingsShowNotch.toggle()
            requireContext().config.showNotch = binding.settingsShowNotch.isChecked
        }
    }
    private fun setupChangeDateTimeFormat() {
        binding.settingsChangeDateTimeFormatHolder.setOnClickListener {
            ChangeDateTimeFormatDialog(requireActivity()) {}
        }
    }

}