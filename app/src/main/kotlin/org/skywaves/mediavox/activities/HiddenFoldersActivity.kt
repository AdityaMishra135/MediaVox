package org.skywaves.mediavox.activities

import android.os.Bundle
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.viewBinding
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.core.interfaces.RefreshRecyclerViewListener
import org.skywaves.mediavox.R
import org.skywaves.mediavox.adapters.ManageHiddenFoldersAdapter
import org.skywaves.mediavox.databinding.ActivityManageFoldersBinding
import org.skywaves.mediavox.extensions.addNoMedia
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.getNoMediaFolders

class HiddenFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityManageFoldersBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title = getString(R.string.hidden_folders)

        updateMaterialActivityViews(binding.manageFoldersCoordinator, binding.manageFoldersList, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.manageFoldersList, binding.manageFoldersToolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        getNoMediaFolders {
            runOnUiThread {
                binding.manageFoldersPlaceholder.apply {
                    text = getString(R.string.hidden_folders_placeholder)
                    beVisibleIf(it.isEmpty())
                    setTextColor(getProperTextColor())
                }

                val adapter = ManageHiddenFoldersAdapter(this, it, this, binding.manageFoldersList) {}
                binding.manageFoldersList.adapter = adapter
            }
        }
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
        FilePickerDialog(this, config.lastFilepickerPath, false, config.shouldShowHidden, false, true) {
            config.lastFilepickerPath = it
            ensureBackgroundThread {
                addNoMedia(it) {
                    updateFolders()
                }
            }
        }
    }
}
