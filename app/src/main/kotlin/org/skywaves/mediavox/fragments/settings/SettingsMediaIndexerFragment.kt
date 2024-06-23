package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.RadioGroupDialog
import org.skywaves.mediavox.core.extensions.beGoneIf
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.formatSize
import org.skywaves.mediavox.core.extensions.getProperSize
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.handleHiddenFolderPasswordProtection
import org.skywaves.mediavox.core.extensions.isExternalStorageManager
import org.skywaves.mediavox.core.extensions.recycleBinPath
import org.skywaves.mediavox.core.extensions.toast
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.core.helpers.isRPlus
import org.skywaves.mediavox.core.helpers.sumByLong
import org.skywaves.mediavox.core.models.RadioItem
import org.skywaves.mediavox.databinding.FragmentSettingsMediaIndexerBinding
import org.skywaves.mediavox.dialogs.GrantAllFilesDialog
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.emptyTheRecycleBin
import org.skywaves.mediavox.extensions.handleExcludedFolderPasswordProtection
import org.skywaves.mediavox.extensions.mediaDB
import org.skywaves.mediavox.extensions.showRecycleBinEmptyingDialog
import org.skywaves.mediavox.fragments.ExcludeFoldersFragment
import org.skywaves.mediavox.fragments.HiddenFoldersFragment
import org.skywaves.mediavox.fragments.IncludeFoldersFragment
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.helpers.PRIORITY_COMPROMISE
import org.skywaves.mediavox.helpers.PRIORITY_SPEED
import org.skywaves.mediavox.helpers.PRIORITY_VALIDITY
import org.skywaves.mediavox.helpers.RECYCLE_BIN
import java.io.File

class SettingsMediaIndexerFragment : SettingsBaseFragment() {


    private var _binding: FragmentSettingsMediaIndexerBinding? = null
    private val binding get() = _binding!!
    private var mRecycleBinContentSize = 0L

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return R.string.media_indexer
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsMediaIndexerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrayOf(
            binding.settingsClearCacheSize,
            binding.settingsClearCacheLabel,
            binding.settingsManageHiddenFolders,
            binding.settingsManageExcludedFolders,
            binding.settingsManageIncludedFolders,
            binding.settingsFileLoadingPriorityLabel,
            binding.settingsFileLoadingPriority,
            binding.settingsEmptyRecycleBinLabel,
            binding.settingsEmptyRecycleBinSize,
        ).forEach {
            it.setTextColor(requireActivity().getProperTextColor())
        }
        binding.settingsShowHiddenItems.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)
        setupFileLoadingPriority()
        setupManageIncludedFolders()
        setupManageExcludedFolders()
        setupManageHiddenFolders()
        setupShowHiddenItems()
        setupClearCache()
        setupEmptyRecycleBin()

    }

    companion object {
        val TAG: String = SettingsMediaIndexerFragment::class.java.simpleName

        val instance: SettingsMediaIndexerFragment
            get() = SettingsMediaIndexerFragment()
    }

    private fun setupFileLoadingPriority() {
        binding.settingsFileLoadingPriorityHolder.beGoneIf(isRPlus() && !isExternalStorageManager())
        binding.settingsFileLoadingPriority.text = getFileLoadingPriorityText()
        binding.settingsFileLoadingPriorityHolder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(PRIORITY_SPEED, getString(R.string.speed)),
                RadioItem(PRIORITY_COMPROMISE, getString(R.string.compromise)),
                RadioItem(PRIORITY_VALIDITY, getString(R.string.avoid_showing_invalid_files))
            )

            RadioGroupDialog(requireActivity(), items, requireContext().config.fileLoadingPriority) {
                requireContext().config.fileLoadingPriority = it as Int
                binding.settingsFileLoadingPriority.text = getFileLoadingPriorityText()
            }
        }
    }

    private fun getFileLoadingPriorityText() = getString(
        when (requireContext().config.fileLoadingPriority) {
            PRIORITY_SPEED -> R.string.speed
            PRIORITY_COMPROMISE -> R.string.compromise
            else -> R.string.avoid_showing_invalid_files
        }
    )


    private fun setupManageIncludedFolders() {
        if (isRPlus() && !isExternalStorageManager()) {
            binding.settingsManageIncludedFolders.text =
                "${getString(R.string.manage_included_folders)} (${getString(org.skywaves.mediavox.core.R.string.no_permission)})"
        } else {
            binding.settingsManageIncludedFolders.setText(R.string.manage_included_folders)
        }

        binding.settingsManageIncludedFoldersHolder.setOnClickListener {
            if (isRPlus() && !isExternalStorageManager()) {
                GrantAllFilesDialog(requireActivity() as BaseSimpleActivity)
            } else {
                (requireActivity() as SettingsActivity).changeFragment(IncludeFoldersFragment.instance)
            }
        }
    }

    private fun setupManageExcludedFolders() {
        binding.settingsManageExcludedFoldersHolder.setOnClickListener {
            requireActivity().handleExcludedFolderPasswordProtection {
                (requireActivity() as SettingsActivity).changeFragment(ExcludeFoldersFragment.instance)
            }
        }
    }

    private fun setupManageHiddenFolders() {
        binding.settingsManageHiddenFoldersHolder.setOnClickListener {
            requireActivity().handleHiddenFolderPasswordProtection {
                (requireActivity() as SettingsActivity).changeFragment(HiddenFoldersFragment.instance)
            }
        }
    }

    private fun setupShowHiddenItems() {
        if (isRPlus() && !isExternalStorageManager()) {
            binding.settingsShowHiddenItems.text =
                "${getString(org.skywaves.mediavox.core.R.string.show_hidden_items)} (${getString(org.skywaves.mediavox.core.R.string.no_permission)})"
        } else {
            binding.settingsShowHiddenItems.setText(org.skywaves.mediavox.core.R.string.show_hidden_items)
        }

        binding.settingsShowHiddenItems.isChecked = requireContext().config.showHiddenMedia
        binding.settingsShowHiddenItemsHolder.setOnClickListener {
            if (isRPlus() && !isExternalStorageManager()) {
                GrantAllFilesDialog(requireActivity() as BaseSimpleActivity)
            } else if (requireContext().config.showHiddenMedia) {
                toggleHiddenItems()
            } else {
                requireActivity().handleHiddenFolderPasswordProtection {
                    toggleHiddenItems()
                }
            }
        }
    }

    private fun toggleHiddenItems() {
        binding.settingsShowHiddenItems.toggle()
        requireContext().config.showHiddenMedia = binding.settingsShowHiddenItems.isChecked
    }


    private fun setupClearCache() {
        ensureBackgroundThread {
            val size = requireContext().cacheDir.getProperSize(true).formatSize()
            requireActivity().runOnUiThread {
                binding.settingsClearCacheSize.text = size
            }
        }

        binding.settingsClearCacheHolder.setOnClickListener {
            ensureBackgroundThread {
                requireContext().cacheDir.deleteRecursively()
                requireActivity().runOnUiThread {
                    binding.settingsClearCacheSize.text = requireContext().cacheDir.getProperSize(true).formatSize()
                }
            }
        }
    }

    private fun setupEmptyRecycleBin() {
        binding.settingsEmptyRecycleBinHolder.beVisibleIf(requireContext().config.useRecycleBin)
        ensureBackgroundThread {
            try {
                mRecycleBinContentSize = requireContext().mediaDB.getDeletedMedia().sumByLong { medium ->
                    val size = medium.size
                    if (size == 0L) {
                        val path = medium.path.removePrefix(RECYCLE_BIN).prependIndent(requireContext().recycleBinPath)
                        File(path).length()
                    } else {
                        size
                    }
                }
            } catch (ignored: Exception) {
            }

            requireActivity().runOnUiThread {
                binding.settingsEmptyRecycleBinSize.text = mRecycleBinContentSize.formatSize()
            }
        }

        binding.settingsEmptyRecycleBinHolder.setOnClickListener {
            if (mRecycleBinContentSize == 0L) {
                requireActivity().toast(org.skywaves.mediavox.core.R.string.recycle_bin_empty)
            } else {
                (requireActivity() as BaseSimpleActivity).showRecycleBinEmptyingDialog {
                    (requireActivity() as BaseSimpleActivity).emptyTheRecycleBin()
                    mRecycleBinContentSize = 0L
                    binding.settingsEmptyRecycleBinSize.text = 0L.formatSize()
                }
            }
        }
    }

}