package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.dialogs.ConfirmationDialog
import org.skywaves.mediavox.core.dialogs.SecurityDialog
import org.skywaves.mediavox.core.extensions.beGoneIf
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.isExternalStorageManager
import org.skywaves.mediavox.core.extensions.isVisible
import org.skywaves.mediavox.core.helpers.PROTECTION_FINGERPRINT
import org.skywaves.mediavox.core.helpers.SHOW_ALL_TABS
import org.skywaves.mediavox.core.helpers.isRPlus
import org.skywaves.mediavox.databinding.FragmentSecurityBinding
import org.skywaves.mediavox.databinding.FragmentSettingsThemeBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsSecurityFragment : SettingsBaseFragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHiddenItemPasswordProtection()
        setupExcludedItemPasswordProtection()
        setupAppPasswordProtection()
        setupFileDeletionPasswordProtection()
        arrayOf(
            binding.settingsAppPasswordProtection,
            binding.settingsFileDeletionPasswordProtection,
            binding.settingsHiddenItemPasswordProtection,
            binding.settingsExcludedItemPasswordProtection,
        ).forEach {
            it.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)
        }
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.security
    }

    companion object {
        val TAG: String = SettingsSecurityFragment::class.java.simpleName

        val instance: SettingsSecurityFragment
            get() = SettingsSecurityFragment()
    }

    private fun setupHiddenItemPasswordProtection() {
        binding.settingsHiddenItemPasswordProtectionHolder.beGoneIf(isRPlus() && !isExternalStorageManager())
        binding.settingsHiddenItemPasswordProtection.isChecked = requireContext().config.isHiddenPasswordProtectionOn
        binding.settingsHiddenItemPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (requireContext().config.isHiddenPasswordProtectionOn) requireContext().config.hiddenProtectionType else SHOW_ALL_TABS
            SecurityDialog(requireActivity(), requireContext().config.hiddenPasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = requireContext().config.isHiddenPasswordProtectionOn
                    binding.settingsHiddenItemPasswordProtection.isChecked = !hasPasswordProtection
                    requireContext().config.isHiddenPasswordProtectionOn = !hasPasswordProtection
                    requireContext().config.hiddenPasswordHash = if (hasPasswordProtection) "" else hash
                    requireContext().config.hiddenProtectionType = type

                    if (requireContext().config.isHiddenPasswordProtectionOn) {
                        val confirmationTextId = if (requireContext().config.hiddenProtectionType == PROTECTION_FINGERPRINT)
                            org.skywaves.mediavox.core.R.string.fingerprint_setup_successfully else org.skywaves.mediavox.core.R.string.protection_setup_successfully
                        ConfirmationDialog(requireActivity(), "", confirmationTextId, org.skywaves.mediavox.core.R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupExcludedItemPasswordProtection() {
        binding.settingsExcludedItemPasswordProtectionHolder.beGoneIf(binding.settingsHiddenItemPasswordProtectionHolder.isVisible())
        binding.settingsExcludedItemPasswordProtection.isChecked = requireContext().config.isExcludedPasswordProtectionOn
        binding.settingsExcludedItemPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (requireContext().config.isExcludedPasswordProtectionOn) requireContext().config.excludedProtectionType else SHOW_ALL_TABS
            SecurityDialog(requireActivity(), requireContext().config.excludedPasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = requireContext().config.isExcludedPasswordProtectionOn
                    binding.settingsExcludedItemPasswordProtection.isChecked = !hasPasswordProtection
                    requireContext().config.isExcludedPasswordProtectionOn = !hasPasswordProtection
                    requireContext().config.excludedPasswordHash = if (hasPasswordProtection) "" else hash
                    requireContext().config.excludedProtectionType = type

                    if (requireContext().config.isExcludedPasswordProtectionOn) {
                        val confirmationTextId = if (requireContext().config.excludedProtectionType == PROTECTION_FINGERPRINT)
                            org.skywaves.mediavox.core.R.string.fingerprint_setup_successfully else org.skywaves.mediavox.core.R.string.protection_setup_successfully
                        ConfirmationDialog(requireActivity(), "", confirmationTextId, org.skywaves.mediavox.core.R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupAppPasswordProtection() {
        binding.settingsAppPasswordProtection.isChecked = requireContext().config.isAppPasswordProtectionOn
        binding.settingsAppPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (requireContext().config.isAppPasswordProtectionOn) requireContext().config.appProtectionType else SHOW_ALL_TABS
            SecurityDialog(requireActivity(), requireContext().config.appPasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = requireContext().config.isAppPasswordProtectionOn
                    binding.settingsAppPasswordProtection.isChecked = !hasPasswordProtection
                    requireContext().config.isAppPasswordProtectionOn = !hasPasswordProtection
                    requireContext().config.appPasswordHash = if (hasPasswordProtection) "" else hash
                    requireContext().config.appProtectionType = type

                    if (requireContext().config.isAppPasswordProtectionOn) {
                        val confirmationTextId = if (requireContext().config.appProtectionType == PROTECTION_FINGERPRINT)
                            org.skywaves.mediavox.core.R.string.fingerprint_setup_successfully else org.skywaves.mediavox.core.R.string.protection_setup_successfully
                        ConfirmationDialog(requireActivity(), "", confirmationTextId, org.skywaves.mediavox.core.R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupFileDeletionPasswordProtection() {
        binding.settingsFileDeletionPasswordProtection.isChecked = requireContext().config.isDeletePasswordProtectionOn
        binding.settingsFileDeletionPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (requireContext().config.isDeletePasswordProtectionOn) requireContext().config.deleteProtectionType else SHOW_ALL_TABS
            SecurityDialog(requireActivity(), requireContext().config.deletePasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = requireContext().config.isDeletePasswordProtectionOn
                    binding.settingsFileDeletionPasswordProtection.isChecked = !hasPasswordProtection
                    requireContext().config.isDeletePasswordProtectionOn = !hasPasswordProtection
                    requireContext().config.deletePasswordHash = if (hasPasswordProtection) "" else hash
                    requireContext().config.deleteProtectionType = type

                    if (requireContext().config.isDeletePasswordProtectionOn) {
                        val confirmationTextId = if (requireContext().config.deleteProtectionType == PROTECTION_FINGERPRINT)
                            org.skywaves.mediavox.core.R.string.fingerprint_setup_successfully else org.skywaves.mediavox.core.R.string.protection_setup_successfully
                        ConfirmationDialog(requireActivity(), "", confirmationTextId, org.skywaves.mediavox.core.R.string.ok, 0) { }
                    }
                }
            }
        }
    }

}