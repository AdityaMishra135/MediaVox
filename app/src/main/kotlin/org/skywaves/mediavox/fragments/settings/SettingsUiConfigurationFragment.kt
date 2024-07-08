package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.databinding.FragmentSettingsGeneralBinding
import org.skywaves.mediavox.databinding.FragmentSettingsUiConfigurationBinding
import org.skywaves.mediavox.dialogs.ChangeFileThumbnailStyleDialog
import org.skywaves.mediavox.dialogs.ChangeFolderThumbnailStyleDialog
import org.skywaves.mediavox.dialogs.ManageBottomActionsDialog
import org.skywaves.mediavox.dialogs.ManageExtendedDetailsDialog
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.helpers.DEFAULT_BOTTOM_ACTIONS
import org.skywaves.mediavox.helpers.RECYCLE_BIN

class SettingsUiConfigurationFragment : SettingsBaseFragment() {
    private var _binding: FragmentSettingsUiConfigurationBinding? = null
    private val binding get() = _binding!!

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.ui_configuration
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsUiConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomActions()
        setupFileThumbnailStyle()
        setupFolderThumbnailStyle()
        setupShowExtendedDetails()
        setupHideExtendedDetails()
        setupManageExtendedDetails()
        setupManageBottomActions()
        setupShowRecycleBin()
        arrayOf(
            binding.settingsShowRecycleBin,
            binding.settingsHideExtendedDetails,
            binding.settingsShowRecycleBinLast,
            binding.settingsShowExtendedDetails,
            binding.settingsBottomActionsCheckbox,
        ).forEach {
            it.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)
        }

        arrayOf(
            binding.settingsManageBottomActions,
            binding.settingsManageExtendedDetails,
            binding.settingsFileThumbnailStyleLabel,
            binding.settingsFolderThumbnailStyle,
            binding.settingsFolderThumbnailStyleLabel,
        ).forEach {
            it.setTextColor(requireActivity().getProperTextColor())
        }
    }

    companion object {
        val TAG: String = SettingsUiConfigurationFragment::class.java.simpleName

        val instance: SettingsUiConfigurationFragment
            get() = SettingsUiConfigurationFragment()
    }



    private fun setupFileThumbnailStyle() {
        binding.settingsFileThumbnailStyleHolder.setOnClickListener {
            ChangeFileThumbnailStyleDialog(requireActivity() as BaseSimpleActivity)
        }
    }

    private fun setupFolderThumbnailStyle() {
        binding.settingsFolderThumbnailStyle.text = getFolderStyleText()
        binding.settingsFolderThumbnailStyleHolder.setOnClickListener {
            ChangeFolderThumbnailStyleDialog(requireActivity() as BaseSimpleActivity) {
                binding.settingsFolderThumbnailStyle.text = getFolderStyleText()
            }
        }
    }

    private fun getFolderStyleText() = getString(R.string.rounded_corners)





    private fun setupShowExtendedDetails() {
        binding.settingsShowExtendedDetails.isChecked = requireContext().config.showExtendedDetails
        updateExtendedDetailsButtons()
        binding.settingsShowExtendedDetailsHolder.setOnClickListener {
            binding.settingsShowExtendedDetails.toggle()
            requireContext().config.showExtendedDetails = binding.settingsShowExtendedDetails.isChecked
            updateExtendedDetailsButtons()
        }
    }

    private fun setupHideExtendedDetails() {
        binding.settingsHideExtendedDetails.isChecked = requireContext().config.hideExtendedDetails
        binding.settingsHideExtendedDetailsHolder.setOnClickListener {
            binding.settingsHideExtendedDetails.toggle()
            requireContext().config.hideExtendedDetails = binding.settingsHideExtendedDetails.isChecked
        }
    }

    private fun setupManageExtendedDetails() {
        binding.settingsManageExtendedDetailsHolder.setOnClickListener {
            ManageExtendedDetailsDialog(requireActivity() as BaseSimpleActivity) {
                if (requireContext().config.extendedDetails == 0) {
                    binding.settingsShowExtendedDetailsHolder.callOnClick()
                }
            }
        }
    }

    private fun updateExtendedDetailsButtons() {
        binding.settingsManageExtendedDetailsHolder.beVisibleIf(requireContext().config.showExtendedDetails)
        binding.settingsHideExtendedDetailsHolder.beVisibleIf(requireContext().config.showExtendedDetails)
    }






    private fun setupBottomActions() {
        binding.settingsBottomActionsCheckbox.isChecked = requireContext().config.bottomActions
        binding.settingsManageBottomActionsHolder.beVisibleIf(requireContext().config.bottomActions)
        binding.settingsBottomActionsCheckboxHolder.setOnClickListener {
            binding.settingsBottomActionsCheckbox.toggle()
            requireContext().config.bottomActions = binding.settingsBottomActionsCheckbox.isChecked
            binding.settingsManageBottomActionsHolder.beVisibleIf(requireContext().config.bottomActions)
        }
    }

    private fun setupManageBottomActions() {
        binding.settingsManageBottomActionsHolder.setOnClickListener {
            ManageBottomActionsDialog(requireActivity() as BaseSimpleActivity) {
                if (requireContext().config.visibleBottomActions == 0) {
                    binding.settingsBottomActionsCheckboxHolder.callOnClick()
                    requireContext().config.bottomActions = false
                    requireContext().config.visibleBottomActions = DEFAULT_BOTTOM_ACTIONS
                }
            }
        }
    }

    private fun setupShowRecycleBin() {
        binding.settingsShowRecycleBin.isChecked = requireContext().config.showRecycleBinAtFolders
        binding.settingsShowRecycleBinHolder.beVisibleIf(requireContext().config.useRecycleBin)
        binding.settingsShowRecycleBinHolder.setOnClickListener {
            binding.settingsShowRecycleBin.toggle()
            requireContext().config.showRecycleBinAtFolders = binding.settingsShowRecycleBin.isChecked
            updateRecycleBinButtons()
        }
    }


    private fun updateRecycleBinButtons() {
        binding.settingsShowRecycleBinLastHolder.beVisibleIf(requireContext().config.useRecycleBin && requireContext().config.showRecycleBinAtFolders)
        binding.settingsShowRecycleBinHolder.beVisibleIf(requireContext().config.useRecycleBin)
    }
}