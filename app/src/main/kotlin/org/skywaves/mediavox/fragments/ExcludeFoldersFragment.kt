package org.skywaves.mediavox.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.adapters.ManageFoldersAdapter
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getColoredMaterialStatusBarColor
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.internalStoragePath
import org.skywaves.mediavox.core.extensions.isExternalStorageManager
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.core.helpers.isRPlus
import org.skywaves.mediavox.core.interfaces.RefreshRecyclerViewListener
import org.skywaves.mediavox.databinding.FragmentExcludeFoldersBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class ExcludeFoldersFragment : SettingsBaseFragment(), RefreshRecyclerViewListener {


    private var _binding: FragmentExcludeFoldersBinding? = null
    private val binding get() = _binding!!
    private var adeHandler = Handler()


    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.exclude_folder
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExcludeFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFolders()
        setupOptionsMenu()
        refreshMenuItems()
    }

    companion object {
        val TAG: String = ExcludeFoldersFragment::class.java.simpleName

        val instance: ExcludeFoldersFragment
            get() = ExcludeFoldersFragment()
    }



    override fun onResume() {
        super.onResume()
        refreshMenuItems()
        (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Arrow, requireActivity().getColoredMaterialStatusBarColor())
    }

    private fun updateFolders() {
        val folders = ArrayList<String>()
        requireContext().config.excludedFolders.mapTo(folders) { it }
        var placeholderText = getString(R.string.excluded_activity_placeholder)
        binding.manageFoldersPlaceholder.apply {
            beVisibleIf(folders.isEmpty())
            setTextColor(requireContext().getProperTextColor())

            if (isRPlus() && !isExternalStorageManager()) {
                placeholderText = placeholderText.substringBefore("\n")
            }

            text = placeholderText
        }

        val adapter = ManageFoldersAdapter(requireActivity() as BaseSimpleActivity, folders, true, this, binding.manageFoldersList) {}
        binding.manageFoldersList.adapter = adapter
    }

    private fun refreshMenuItems() {
        if (((requireActivity() as SettingsActivity).supportFragmentManager.findFragmentById(R.id.settings_content_container)) is ExcludeFoldersFragment){
            (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(R.id.add_folder).isVisible = true
        }
    }

    private fun setupOptionsMenu() {
        (requireActivity() as SettingsActivity).binding.settingsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_folder -> addFolder()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun refreshItems() {
        updateFolders()
    }

    private fun addFolder() {
        FilePickerDialog(
            activity = requireActivity() as BaseSimpleActivity,
            requireContext().internalStoragePath,
            pickFile = false,
            requireContext().config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            enforceStorageRestrictions = false,
        ) {
            requireContext().config.lastFilepickerPath = it
            requireContext().config.addExcludedFolder(it)
            updateFolders()
        }
    }

    fun handleBackPressed() {
        if (((requireActivity() as SettingsActivity).supportFragmentManager.findFragmentById(R.id.settings_content_container)) is ExcludeFoldersFragment){
            adeHandler.postDelayed({
                (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(R.id.add_folder).isVisible = false
            }, 170)
            (requireActivity() as SettingsActivity).supportFragmentManager.popBackStack()
        }
    }
}
