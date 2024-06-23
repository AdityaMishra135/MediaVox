package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.databinding.FragmentSecurityBinding
import org.skywaves.mediavox.databinding.FragmentSettingsMediaOperationBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.handleMediaManagementPrompt
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsMediaOperationFragment : SettingsBaseFragment() {

    private var _binding: FragmentSettingsMediaOperationBinding? = null
    private val binding get() = _binding!!

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.media_operations
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsMediaOperationBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchAllFiles()
        setupDeleteEmptyFolders()
        setupKeepLastModified()
        setupSkipDeleteConfirmation()
        setupUseRecycleBin()
        arrayOf(
            binding.settingsDeleteEmptyFolders,
            binding.settingsSearchAllFiles,
            binding.settingsUseRecycleBin,
            binding.settingsKeepLastModified,
            binding.settingsSkipDeleteConfirmation,
        ).forEach {
            it.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)
        }
    }


    companion object {
        val TAG: String = SettingsMediaOperationFragment::class.java.simpleName

        val instance: SettingsMediaOperationFragment
            get() = SettingsMediaOperationFragment()
    }


    private fun setupSearchAllFiles() {
        binding.settingsSearchAllFiles.isChecked = requireContext().config.searchAllFilesByDefault
        binding.settingsSearchAllFilesHolder.setOnClickListener {
            binding.settingsSearchAllFiles.toggle()
            requireContext().config.searchAllFilesByDefault = binding.settingsSearchAllFiles.isChecked
        }
    }



    private fun setupDeleteEmptyFolders() {
        binding.settingsDeleteEmptyFolders.isChecked = requireContext().config.deleteEmptyFolders
        binding.settingsDeleteEmptyFoldersHolder.setOnClickListener {
            binding.settingsDeleteEmptyFolders.toggle()
            requireContext().config.deleteEmptyFolders = binding.settingsDeleteEmptyFolders.isChecked
        }
    }


    private fun setupKeepLastModified() {
        binding.settingsKeepLastModified.isChecked = requireContext().config.keepLastModified
        binding.settingsKeepLastModifiedHolder.setOnClickListener {
            (requireActivity() as BaseSimpleActivity).handleMediaManagementPrompt {
                binding.settingsKeepLastModified.toggle()
                requireContext().config.keepLastModified = binding.settingsKeepLastModified.isChecked
            }
        }
    }

    private fun setupSkipDeleteConfirmation() {
        binding.settingsSkipDeleteConfirmation.isChecked = requireContext().config.skipDeleteConfirmation
        binding.settingsSkipDeleteConfirmationHolder.setOnClickListener {
            binding.settingsSkipDeleteConfirmation.toggle()
            requireContext().config.skipDeleteConfirmation = binding.settingsSkipDeleteConfirmation.isChecked
        }
    }

    private fun setupUseRecycleBin() {
        binding.settingsUseRecycleBin.isChecked = requireContext().config.useRecycleBin
        binding.settingsUseRecycleBinHolder.setOnClickListener {
            binding.settingsUseRecycleBin.toggle()
            requireContext().config.useRecycleBin = binding.settingsUseRecycleBin.isChecked
        }
    }
}