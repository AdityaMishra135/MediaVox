package org.skywaves.mediavox.activities

import android.os.Bundle
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.core.helpers.isRPlus
import org.skywaves.mediavox.core.interfaces.RefreshRecyclerViewListener
import org.skywaves.mediavox.R
import org.skywaves.mediavox.adapters.ManageFoldersAdapter
import org.skywaves.mediavox.databinding.ActivityManageFoldersBinding
import org.skywaves.mediavox.extensions.config

class ExcludedFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityManageFoldersBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title = getString(org.skywaves.mediavox.core.R.string.excluded_folders)

        updateMaterialActivityViews(binding.manageFoldersCoordinator, binding.manageFoldersList, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.manageFoldersList, binding.manageFoldersToolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        val folders = ArrayList<String>()
        config.excludedFolders.mapTo(folders) { it }
        var placeholderText = getString(R.string.excluded_activity_placeholder)
        binding.manageFoldersPlaceholder.apply {
            beVisibleIf(folders.isEmpty())
            setTextColor(getProperTextColor())

            if (isRPlus() && !isExternalStorageManager()) {
                placeholderText = placeholderText.substringBefore("\n")
            }

            text = placeholderText
        }

        val adapter = ManageFoldersAdapter(this, folders, true, this, binding.manageFoldersList) {}
        binding.manageFoldersList.adapter = adapter
    }

    private fun setupOptionsMenu() {
        binding.manageFoldersToolbar.setOnMenuItemClickListener { menuItem ->
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
            activity = this,
            internalStoragePath,
            pickFile = false,
            config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            enforceStorageRestrictions = false,
        ) {
            config.lastFilepickerPath = it
            config.addExcludedFolder(it)
            updateFolders()
        }
    }
}
