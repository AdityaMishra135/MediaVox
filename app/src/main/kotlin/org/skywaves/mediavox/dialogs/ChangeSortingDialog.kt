package org.skywaves.mediavox.dialogs

import android.content.DialogInterface
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.core.helpers.*
import org.skywaves.mediavox.R
import org.skywaves.mediavox.databinding.DialogChangeSortingBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.helpers.SHOW_ALL
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ChangeSortingDialog(
    val activity: AppCompatActivity,
    val isDirectorySorting: Boolean,
    val showFolderCheckbox: Boolean,
    val path: String = "",
    val callback: () -> Unit
) : DialogInterface.OnClickListener {

    private var currSorting = 0
    private var config = activity.config
    private var pathToUse = if (!isDirectorySorting && path.isEmpty()) SHOW_ALL else path
    private lateinit var binding: DialogChangeSortingBinding

    init {
        currSorting = if (isDirectorySorting) config.directorySorting else config.getFolderSorting(pathToUse)
        binding = DialogChangeSortingBinding.inflate(activity.layoutInflater).apply {
            sortingDialogOrderDivider.beVisibleIf(showFolderCheckbox || (currSorting and SORT_BY_NAME != 0 || currSorting and SORT_BY_PATH != 0))
            checkBoxNumericSorting.beVisibleIf(showFolderCheckbox && (currSorting and SORT_BY_NAME != 0 || currSorting and SORT_BY_PATH != 0))
            checkBoxNumericSorting.isChecked = currSorting and SORT_USE_NUMERIC_VALUE != 0
            checkBoxUseForThisFolder.beVisibleIf(showFolderCheckbox)
            checkBoxUseForThisFolder.isChecked = config.hasCustomSorting(pathToUse)
            textViewBottomNote.beVisibleIf(!isDirectorySorting)
            chipCustom.beVisibleIf(isDirectorySorting)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok, this)
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, org.skywaves.mediavox.core.R.string.sort_by)
            }

        setupSortChips()
        setupOrderChips()
    }

        private fun setupSortChips() {
            val chipGroupSorting = binding.chipGroupSorting
            chipGroupSorting.setOnCheckedChangeListener { group, checkedId ->
                val chip = group.findViewById<Chip>(checkedId)
                val isSortingByNameOrPath = checkedId == R.id.chipName || checkedId == R.id.chipPath
                binding.checkBoxNumericSorting.beVisibleIf(isSortingByNameOrPath)
                binding.sortingDialogOrderDivider.beVisibleIf(binding.checkBoxNumericSorting.isVisible() || binding.checkBoxUseForThisFolder.isVisible())

                val hideSortOrder = checkedId == R.id.chipCustom || checkedId == R.id.chipRandom
                binding.buttonToggleGroupOrder.beGoneIf(hideSortOrder)
                binding.sortingDialogOrderDivider.beGoneIf(hideSortOrder)

                // Show check icon for the selected chip
                for (i in 0 until chipGroupSorting.childCount) {
                    val child = chipGroupSorting.getChildAt(i) as Chip
                    child.isCheckedIconVisible = child.id == checkedId
                }
            }

            // Set the checked state based on current sorting
            val sortChip = when {
                currSorting and SORT_BY_PATH != 0 -> binding.chipPath
                currSorting and SORT_BY_SIZE != 0 -> binding.chipSize
                currSorting and SORT_BY_DATE_MODIFIED != 0 -> binding.chipLastModified
                currSorting and SORT_BY_DATE_TAKEN != 0 -> binding.chipDateTaken
                currSorting and SORT_BY_RANDOM != 0 -> binding.chipRandom
                currSorting and SORT_BY_CUSTOM != 0 -> binding.chipCustom
                else -> binding.chipName
            }
            sortChip.isChecked = true
            sortChip.isCheckedIconVisible = true // Make sure to show the check icon for the initially checked chip
        }

    private fun setupOrderChips() {
        // Set the checked state based on current sorting order
        val orderChip = if (currSorting and SORT_DESCENDING != 0) {
            binding.buttonDescending
        } else {
            binding.buttonAscending
        }
        orderChip.isChecked = true
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val sortingChipGroup = binding.chipGroupSorting
        var sorting = when (sortingChipGroup.checkedChipId) {
            R.id.chipName -> SORT_BY_NAME
            R.id.chipPath -> SORT_BY_PATH
            R.id.chipSize -> SORT_BY_SIZE
            R.id.chipLastModified -> SORT_BY_DATE_MODIFIED
            R.id.chipDateTaken -> SORT_BY_DATE_TAKEN
            R.id.chipRandom -> SORT_BY_RANDOM
            R.id.chipCustom -> SORT_BY_CUSTOM
            else -> SORT_BY_DATE_TAKEN // Default fallback
        }


        val orderChipGroup = binding.buttonToggleGroupOrder
        if (orderChipGroup.checkedButtonId == R.id.buttonDescending) {
            sorting = sorting or SORT_DESCENDING
        }

        if (binding.checkBoxNumericSorting.isChecked) {
            sorting = sorting or SORT_USE_NUMERIC_VALUE
        }

        if (isDirectorySorting) {
            config.directorySorting = sorting
        } else {
            if (binding.checkBoxUseForThisFolder.isChecked) {
                config.saveCustomSorting(pathToUse, sorting)
            } else {
                config.removeCustomSorting(pathToUse)
                config.sorting = sorting
            }
        }

        if (currSorting != sorting) {
            callback()
        }
    }
}
