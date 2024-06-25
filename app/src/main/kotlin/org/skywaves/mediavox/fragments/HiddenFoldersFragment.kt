package org.skywaves.mediavox.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.adapters.ManageHiddenFoldersAdapter
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getColoredMaterialStatusBarColor
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.core.interfaces.RefreshRecyclerViewListener
import org.skywaves.mediavox.databinding.FragmentHiddenFoldersBinding
import org.skywaves.mediavox.extensions.addNoMedia
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.getNoMediaFolders
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class HiddenFoldersFragment : SettingsBaseFragment(), RefreshRecyclerViewListener {
    private var _binding: FragmentHiddenFoldersBinding? = null
    private val binding get() = _binding!!
    private var adeHandler = Handler()

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return R.string.hidden_folders
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHiddenFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFolders()
        setupOptionsMenu()
        refreshMenuItems()

    }

    companion object {
        val TAG: String = HiddenFoldersFragment::class.java.simpleName

        val instance: HiddenFoldersFragment
            get() = HiddenFoldersFragment()
    }



    override fun onResume() {
        super.onResume()
        refreshMenuItems()
        (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Arrow, requireActivity().getColoredMaterialStatusBarColor())
    }
    private fun refreshMenuItems() {
        if (((requireActivity() as SettingsActivity).supportFragmentManager.findFragmentById(R.id.settings_content_container)) is HiddenFoldersFragment){
            (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(R.id.add_folder).isVisible = true
        }
    }
    private fun updateFolders() {
        requireContext().getNoMediaFolders {
            requireActivity().runOnUiThread {
                binding.manageFoldersPlaceholder.apply {
                    text = getString(R.string.hidden_folders_placeholder)
                    beVisibleIf(it.isEmpty())
                    setTextColor(requireContext().getProperTextColor())
                }

                val adapter = ManageHiddenFoldersAdapter(requireActivity() as BaseSimpleActivity, it, this, binding.manageFoldersList) {}
                binding.manageFoldersList.adapter = adapter
            }
        }
    }

    private fun setupOptionsMenu() {
        (requireActivity() as SettingsActivity).binding.settingsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_folder -> addFolder()
                else -> return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }
    }

    override fun refreshItems() {
        updateFolders()
    }

    private fun addFolder() {
        FilePickerDialog(requireActivity() as BaseSimpleActivity, requireContext().config.lastFilepickerPath, false, requireContext().config.shouldShowHidden, false, true) {
            requireContext().config.lastFilepickerPath = it
            ensureBackgroundThread {
                (requireContext() as BaseSimpleActivity).addNoMedia(it) {
                    updateFolders()
                }
            }
        }
    }

    fun handleBackPressed3() {
        if (((requireActivity() as SettingsActivity).supportFragmentManager.findFragmentById(R.id.settings_content_container)) is HiddenFoldersFragment){
            adeHandler.postDelayed({
                (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(R.id.add_folder).isVisible = false
            }, 170)
            (requireActivity() as SettingsActivity).supportFragmentManager.popBackStack()
        }
    }
}