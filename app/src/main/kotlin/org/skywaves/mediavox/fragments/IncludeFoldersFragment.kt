package org.skywaves.mediavox.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.activities.SimpleActivity
import org.skywaves.mediavox.adapters.ManageFoldersAdapter
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getColoredMaterialStatusBarColor
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.core.interfaces.RefreshRecyclerViewListener
import org.skywaves.mediavox.databinding.FragmentIncludeFoldersBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class IncludeFoldersFragment : SettingsBaseFragment(), RefreshRecyclerViewListener {
    private var _binding: FragmentIncludeFoldersBinding? = null
    private val binding get() = _binding!!
    private var adeHandler = Handler()

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return R.string.include_folders
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIncludeFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFolders()
        setupOptionsMenu()
        refreshMenuItems()

    }

    companion object {
        val TAG: String = IncludeFoldersFragment::class.java.simpleName

        val instance: IncludeFoldersFragment
            get() = IncludeFoldersFragment()
    }

    override fun onResume() {
        super.onResume()
        refreshMenuItems()
        (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Arrow, requireActivity().getColoredMaterialStatusBarColor())
    }


    private fun refreshMenuItems() {
        if (((requireActivity() as SettingsActivity).supportFragmentManager.findFragmentById(R.id.settings_content_container)) is IncludeFoldersFragment){
            (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(R.id.add_folder).isVisible = true
        }
    }

    private fun updateFolders() {
        val folders = ArrayList<String>()
        requireContext().config.includedFolders.mapTo(folders) { it }
        binding.manageFoldersPlaceholder.apply {
            text = getString(R.string.included_activity_placeholder)
            beVisibleIf(folders.isEmpty())
            setTextColor(requireContext().getProperTextColor())
        }

        val adapter = ManageFoldersAdapter(requireActivity() as BaseSimpleActivity, folders, false, this, binding.manageFoldersList) {}
        binding.manageFoldersList.adapter = adapter
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
        (requireActivity() as SimpleActivity).showAddIncludedFolderDialog {
            updateFolders()
        }
    }

    fun handleBackPressed4() {
        if (((requireActivity() as SettingsActivity).supportFragmentManager.findFragmentById(R.id.settings_content_container)) is IncludeFoldersFragment){
            adeHandler.postDelayed({
                (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(R.id.add_folder).isVisible = false
            }, 170)
            (requireActivity() as SettingsActivity).supportFragmentManager.popBackStack()
        }
    }
}