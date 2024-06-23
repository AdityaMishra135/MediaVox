package org.skywaves.mediavox.adapters

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.allViews
import com.bumptech.glide.Glide
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.adapters.MyRecyclerViewAdapter
import org.skywaves.mediavox.core.dialogs.PropertiesDialog
import org.skywaves.mediavox.core.dialogs.RenameDialog
import org.skywaves.mediavox.core.dialogs.RenameItemDialog
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.core.helpers.*
import org.skywaves.mediavox.core.models.FileDirItem
import org.skywaves.mediavox.core.views.MyRecyclerView
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.ViewPagerActivity
import org.skywaves.mediavox.databinding.*
import org.skywaves.mediavox.dialogs.DeleteWithRememberDialog
import org.skywaves.mediavox.extensions.*
import org.skywaves.mediavox.helpers.*
import org.skywaves.mediavox.interfaces.MediaOperationsListener
import org.skywaves.mediavox.models.Medium
import org.skywaves.mediavox.models.ThumbnailItem
import org.skywaves.mediavox.models.ThumbnailSection

class MediaAdapter(
    activity: BaseSimpleActivity, var media: ArrayList<ThumbnailItem>, val listener: MediaOperationsListener?, val isAGetIntent: Boolean,
    val allowMultiplePicks: Boolean, val path: String, recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) :
    MyRecyclerViewAdapter(activity, recyclerView, itemClick), RecyclerViewFastScroller.OnPopupTextUpdate {

    private val INSTANT_LOAD_DURATION = 2000L
    private val IMAGE_LOAD_DELAY = 100L
    private val ITEM_SECTION = 0
    private val ITEM_MEDIUM_VIDEO_PORTRAIT = 1

    private val config = activity.config
    private val viewType = config.getFolderViewType(if (config.showAll) SHOW_ALL else path)
    private val isListViewType = viewType == VIEW_TYPE_LIST
    private var visibleItemPaths = ArrayList<String>()
    private var rotatedImagePaths = ArrayList<String>()
    private var loadImageInstantly = false
    private var delayHandler = Handler(Looper.getMainLooper())
    private var currentMediaHash = media.hashCode()
    private val hasOTGConnected = activity.hasOTGConnected()

    private var showFileTypes = config.showThumbnailFileTypes
    private var lastPlayed = config.lastPlayed
    private var showFileDir = config.showThumbnailFileDir
    private var showFileSize = config.showMediumSize

    var sorting = config.getFolderSorting(if (config.showAll) SHOW_ALL else path)
    var dateFormat = config.dateFormat
    var timeFormat = activity.getTimeFormat()

    init {
        setupDragListener(true)
        enableInstantLoad()
    }

    override fun getActionMenuId() = R.menu.cab_media

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = if (viewType == ITEM_SECTION) {
            ThumbnailSectionBinding.inflate(layoutInflater, parent, false)
        } else {
            if (isListViewType) {
                    VideoItemListBinding.inflate(layoutInflater, parent, false)
            } else {
                VideoItemGridBinding.inflate(layoutInflater, parent, false)
            }
        }
        return createViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val tmbItem = media.getOrNull(position) ?: return
        if (tmbItem is Medium) {
            visibleItemPaths.add(tmbItem.path)
        }

        val allowLongPress = (!isAGetIntent || allowMultiplePicks) && tmbItem is Medium
        holder.bindView(tmbItem, tmbItem is Medium, allowLongPress) { itemView, adapterPosition ->
            if (tmbItem is Medium) {
                setupThumbnail(itemView, tmbItem)
            } else {
                setupSection(itemView, tmbItem as ThumbnailSection)
            }
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = media.size

    override fun getItemViewType(position: Int): Int {
        val tmbItem = media[position]
        return when {
            tmbItem is ThumbnailSection -> ITEM_SECTION
            else -> ITEM_MEDIUM_VIDEO_PORTRAIT
        }
    }

    override fun prepareActionMode(menu: Menu) {
        val selectedItems = getSelectedItems()
        if (selectedItems.isEmpty()) {
            return
        }

        val isOneItemSelected = isOneItemSelected()
        val selectedPaths = selectedItems.map { it.path } as ArrayList<String>
        val isInRecycleBin = selectedItems.firstOrNull()?.getIsInRecycleBin() == true
        menu.apply {
            findItem(R.id.cab_rename).isVisible = !isInRecycleBin
            findItem(R.id.cab_add_to_favorites).isVisible = !isInRecycleBin
            findItem(R.id.cab_fix_date_taken).isVisible = !isInRecycleBin
            findItem(R.id.cab_move_to).isVisible = !isInRecycleBin
            findItem(R.id.cab_open_with).isVisible = isOneItemSelected
            findItem(R.id.cab_confirm_selection).isVisible = isAGetIntent && allowMultiplePicks && selectedKeys.isNotEmpty()
            findItem(R.id.cab_restore_recycle_bin_files).isVisible = selectedPaths.all { it.startsWith(activity.recycleBinPath) }
            findItem(R.id.cab_create_shortcut).isVisible = isOreoPlus() && isOneItemSelected

            checkHideBtnVisibility(this, selectedItems)
            checkFavoriteBtnVisibility(this, selectedItems)
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_confirm_selection -> confirmSelection()
            R.id.cab_properties -> showProperties()
            R.id.cab_rename -> checkMediaManagementAndRename()
            R.id.cab_hide -> toggleFileVisibility(true)
            R.id.cab_unhide -> toggleFileVisibility(false)
            R.id.cab_add_to_favorites -> toggleFavorites(true)
            R.id.cab_remove_from_favorites -> toggleFavorites(false)
            R.id.cab_restore_recycle_bin_files -> restoreFiles()
            R.id.cab_share -> shareMedia()
            R.id.cab_copy_to -> checkMediaManagementAndCopy(true)
            R.id.cab_move_to -> moveFilesTo()
            R.id.cab_create_shortcut -> createShortcut()
            R.id.cab_select_all -> selectAll()
            R.id.cab_open_with -> openPath()
            R.id.cab_fix_date_taken -> fixDateTaken()
            R.id.cab_delete -> checkDeleteConfirmation()
        }
    }

    override fun getSelectableItemCount() = media.filter { it is Medium }.size

    override fun getIsItemSelectable(position: Int) = !isASectionTitle(position)

    override fun getItemSelectionKey(position: Int) = (media.getOrNull(position) as? Medium)?.path?.hashCode()

    override fun getItemKeyPosition(key: Int) = media.indexOfFirst { (it as? Medium)?.path?.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed) {
            val itemView = holder.itemView
            visibleItemPaths.remove(itemView.allViews.firstOrNull { it.id == R.id.medium_name }?.tag)
            val tmb = itemView.allViews.firstOrNull { it.id == R.id.medium_thumbnail }
            if (tmb != null) {
                Glide.with(activity).clear(tmb)
            }
        }
    }

    fun isASectionTitle(position: Int) = media.getOrNull(position) is ThumbnailSection

    private fun checkHideBtnVisibility(menu: Menu, selectedItems: ArrayList<Medium>) {
        val isInRecycleBin = selectedItems.firstOrNull()?.getIsInRecycleBin() == true
        menu.findItem(R.id.cab_hide).isVisible = (!isRPlus() || isExternalStorageManager()) && !isInRecycleBin && selectedItems.any { !it.isHidden() }
        menu.findItem(R.id.cab_unhide).isVisible = (!isRPlus() || isExternalStorageManager()) && !isInRecycleBin && selectedItems.any { it.isHidden() }
    }

    private fun checkFavoriteBtnVisibility(menu: Menu, selectedItems: ArrayList<Medium>) {
        menu.findItem(R.id.cab_add_to_favorites).isVisible = selectedItems.none { it.getIsInRecycleBin() } && selectedItems.any { !it.isFavorite }
        menu.findItem(R.id.cab_remove_from_favorites).isVisible = selectedItems.none { it.getIsInRecycleBin() } && selectedItems.any { it.isFavorite }
    }

    private fun confirmSelection() {
        listener?.selectedPaths(getSelectedPaths())
    }

    private fun showProperties() {
        if (selectedKeys.size <= 1) {
            val path = getFirstSelectedItemPath() ?: return
            PropertiesDialog(activity, path, config.shouldShowHidden)
        } else {
            val paths = getSelectedPaths()
            PropertiesDialog(activity, paths, config.shouldShowHidden)
        }
    }

    private fun checkMediaManagementAndRename() {
        activity.handleMediaManagementPrompt {
            renameFile()
        }
    }

    private fun renameFile() {
        val firstPath = getFirstSelectedItemPath() ?: return

        val isSDOrOtgRootFolder = activity.isAStorageRootFolder(firstPath.getParentPath()) && !firstPath.startsWith(activity.internalStoragePath)
        if (isRPlus() && isSDOrOtgRootFolder && !isExternalStorageManager()) {
            activity.toast(org.skywaves.mediavox.core.R.string.rename_in_sd_card_system_restriction, Toast.LENGTH_LONG)
            finishActMode()
            return
        }

        if (selectedKeys.size == 1) {
            RenameItemDialog(activity, firstPath) {
                ensureBackgroundThread {
                    activity.updateDBMediaPath(firstPath, it)

                    activity.runOnUiThread {
                        enableInstantLoad()
                        listener?.refreshItems()
                        finishActMode()
                    }
                }
            }
        } else {
            RenameDialog(activity, getSelectedPaths(), true) {
                enableInstantLoad()
                listener?.refreshItems()
                finishActMode()
            }
        }
    }

    private fun openPath() {
        val path = getFirstSelectedItemPath() ?: return
        activity.openPath(path, true)
    }


    private fun toggleFileVisibility(hide: Boolean) {
        ensureBackgroundThread {
            getSelectedItems().forEach {
                activity.toggleFileVisibility(it.path, hide)
            }
            activity.runOnUiThread {
                listener?.refreshItems()
                finishActMode()
            }
        }
    }

    private fun toggleFavorites(add: Boolean) {
        ensureBackgroundThread {
            getSelectedItems().forEach {
                it.isFavorite = add
                activity.updateFavorite(it.path, add)
            }
            activity.runOnUiThread {
                listener?.refreshItems()
                finishActMode()
            }
        }
    }

    private fun restoreFiles() {
        activity.restoreRecycleBinPaths(getSelectedPaths()) {
            listener?.refreshItems()
            finishActMode()
        }
    }

    private fun shareMedia() {
        if (selectedKeys.size == 1 && selectedKeys.first() != -1) {
            activity.shareMediumPath(getSelectedItems().first().path)
        } else if (selectedKeys.size > 1) {
            activity.shareMediaPaths(getSelectedPaths())
        }
    }


    private fun moveFilesTo() {
        activity.handleDeletePasswordProtection {
            checkMediaManagementAndCopy(false)
        }
    }

    private fun checkMediaManagementAndCopy(isCopyOperation: Boolean) {
        activity.handleMediaManagementPrompt {
            copyMoveTo(isCopyOperation)
        }
    }

    private fun copyMoveTo(isCopyOperation: Boolean) {
        val paths = getSelectedPaths()

        val recycleBinPath = activity.recycleBinPath
        val fileDirItems = paths.asSequence().filter { isCopyOperation || !it.startsWith(recycleBinPath) }.map {
            FileDirItem(it, it.getFilenameFromPath())
        }.toMutableList() as ArrayList

        if (!isCopyOperation && paths.any { it.startsWith(recycleBinPath) }) {
            activity.toast(org.skywaves.mediavox.core.R.string.moving_recycle_bin_items_disabled, Toast.LENGTH_LONG)
        }

        if (fileDirItems.isEmpty()) {
            return
        }

        activity.tryCopyMoveFilesTo(fileDirItems, isCopyOperation) {
            val destinationPath = it
            config.tempFolderPath = ""
            activity.applicationContext.rescanFolderMedia(destinationPath)
            activity.applicationContext.rescanFolderMedia(fileDirItems.first().getParentPath())

            val newPaths = fileDirItems.map { "$destinationPath/${it.name}" }.toMutableList() as ArrayList<String>
            activity.rescanPaths(newPaths) {
                activity.fixDateTaken(newPaths, false)
            }

            if (!isCopyOperation) {
                listener?.refreshItems()
                activity.updateFavoritePaths(fileDirItems, destinationPath)
            }
        }
    }

    private fun createShortcut() {
        if (!isOreoPlus()) {
            return
        }

        val manager = activity.getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported) {
            val path = getSelectedPaths().first()
            val drawable = resources.getDrawable(R.drawable.shortcut_image).mutate()
            activity.getShortcutImage(path, drawable) {
                val intent = Intent(activity, ViewPagerActivity::class.java).apply {
                    putExtra(PATH, path)
                    putExtra(SHOW_ALL, config.showAll)
                    putExtra(SHOW_FAVORITES, path == FAVORITES)
                    putExtra(SHOW_RECYCLE_BIN, path == RECYCLE_BIN)
                    action = Intent.ACTION_VIEW
                    flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val shortcut = ShortcutInfo.Builder(activity, path)
                    .setShortLabel(path.getFilenameFromPath())
                    .setIcon(Icon.createWithBitmap(drawable.convertToBitmap()))
                    .setIntent(intent)
                    .build()

                manager.requestPinShortcut(shortcut, null)
            }
        }
    }

    private fun fixDateTaken() {
        ensureBackgroundThread {
            activity.fixDateTaken(getSelectedPaths(), true) {
                listener?.refreshItems()
                finishActMode()
            }
        }
    }

    private fun checkDeleteConfirmation() {
        activity.handleMediaManagementPrompt {
            if (config.isDeletePasswordProtectionOn) {
                activity.handleDeletePasswordProtection {
                    deleteFiles(config.tempSkipRecycleBin)
                }
            } else if (config.tempSkipDeleteConfirmation || config.skipDeleteConfirmation) {
                deleteFiles(config.tempSkipRecycleBin)
            } else {
                askConfirmDelete()
            }
        }
    }

    private fun askConfirmDelete() {
        val itemsCnt = selectedKeys.size
        val selectedMedia = getSelectedItems()
        val firstPath = selectedMedia.first().path
        val fileDirItem = selectedMedia.first().toFileDirItem()
        val size = fileDirItem.getProperSize(activity, countHidden = true).formatSize()
        val itemsAndSize = if (itemsCnt == 1) {
            fileDirItem.mediaStoreId = selectedMedia.first().mediaStoreId
            "\"${firstPath.getFilenameFromPath()}\" ($size)"
        } else {
            val fileDirItems = ArrayList<FileDirItem>(selectedMedia.size)
            selectedMedia.forEach { medium ->
                val curFileDirItem = medium.toFileDirItem()
                fileDirItems.add(curFileDirItem)
            }
            val fileSize = fileDirItems.sumByLong { it.getProperSize(activity, countHidden = true) }.formatSize()
            val deleteItemsString = resources.getQuantityString(org.skywaves.mediavox.core.R.plurals.delete_items, itemsCnt, itemsCnt)
            "$deleteItemsString ($fileSize)"
        }

        val isRecycleBin = firstPath.startsWith(activity.recycleBinPath)
        val baseString =
            if (config.useRecycleBin && !config.tempSkipRecycleBin && !isRecycleBin) org.skywaves.mediavox.core.R.string.move_to_recycle_bin_confirmation else org.skywaves.mediavox.core.R.string.deletion_confirmation
        val question = String.format(resources.getString(baseString), itemsAndSize)
        val showSkipRecycleBinOption = config.useRecycleBin && !isRecycleBin

        DeleteWithRememberDialog(activity, question, showSkipRecycleBinOption) { remember, skipRecycleBin ->
            config.tempSkipDeleteConfirmation = remember

            if (remember) {
                config.tempSkipRecycleBin = skipRecycleBin
            }

            deleteFiles(skipRecycleBin)
        }
    }

    private fun deleteFiles(skipRecycleBin: Boolean) {
        if (selectedKeys.isEmpty()) {
            return
        }

        val selectedItems = getSelectedItems()
        val selectedPaths = selectedItems.map { it.path } as ArrayList<String>
        val SAFPath = selectedPaths.firstOrNull { activity.needsStupidWritePermissions(it) } ?: getFirstSelectedItemPath() ?: return
        activity.handleSAFDialog(SAFPath) {
            if (!it) {
                return@handleSAFDialog
            }

            val sdk30SAFPath = selectedPaths.firstOrNull { activity.isAccessibleWithSAFSdk30(it) } ?: getFirstSelectedItemPath() ?: return@handleSAFDialog
            activity.checkManageMediaOrHandleSAFDialogSdk30(sdk30SAFPath) {
                if (!it) {
                    return@checkManageMediaOrHandleSAFDialogSdk30
                }

                val fileDirItems = ArrayList<FileDirItem>(selectedKeys.size)
                val removeMedia = ArrayList<Medium>(selectedKeys.size)
                val positions = getSelectedItemPositions()

                selectedItems.forEach { medium ->
                    fileDirItems.add(medium.toFileDirItem())
                    removeMedia.add(medium)
                }

                media.removeAll(removeMedia)
                listener?.tryDeleteFiles(fileDirItems, skipRecycleBin)
                listener?.updateMediaGridDecoration(media)
                removeSelectedItems(positions)
                currentMediaHash = media.hashCode()
            }
        }
    }

    private fun getSelectedItems() = selectedKeys.mapNotNull { getItemWithKey(it) } as ArrayList<Medium>

    private fun getSelectedPaths() = getSelectedItems().map { it.path } as ArrayList<String>

    private fun getFirstSelectedItemPath() = getItemWithKey(selectedKeys.first())?.path

    private fun getItemWithKey(key: Int): Medium? = media.firstOrNull { (it as? Medium)?.path?.hashCode() == key } as? Medium

    fun updateMedia(newMedia: ArrayList<ThumbnailItem>) {
        val thumbnailItems = newMedia.clone() as ArrayList<ThumbnailItem>
        if (thumbnailItems.hashCode() != currentMediaHash) {
            currentMediaHash = thumbnailItems.hashCode()
            media = thumbnailItems
            enableInstantLoad()
            notifyDataSetChanged()
            finishActMode()
        }
    }


    fun updateShowFileTypes(showFileTypes: Boolean) {
        this.showFileTypes = showFileTypes
        notifyDataSetChanged()
    }

    fun updateLastPlayed(lastPlayed: String) {
        this.lastPlayed = lastPlayed
        notifyDataSetChanged()
    }

    fun updateShowFileDir(showFileDir: Boolean) {
        this.showFileDir = showFileDir
        notifyDataSetChanged()
    }
    fun updateShowFileSize(showFileSize: Boolean) {
        this.showFileSize = showFileSize
        notifyDataSetChanged()
    }

    private fun enableInstantLoad() {
        loadImageInstantly = true
        delayHandler.postDelayed({
            loadImageInstantly = false
        }, INSTANT_LOAD_DURATION)
    }

    private fun setupThumbnail(view: View, medium: Medium) {
        val isSelected = selectedKeys.contains(medium.path.hashCode())
        bindItem(view, medium).apply {
            favorite.beVisibleIf(medium.isFavorite)

            playPortraitOutline?.beVisibleIf((medium.isVideo() || medium.isAudio()) && showFileTypes)
            if (medium.isAudio()) {
                config.lastPlayedType = "Audio"
                playPortraitOutline?.setImageResource(R.drawable.ic_music)
            } else {
                config.lastPlayedType = "Video"
                playPortraitOutline?.setImageResource(R.drawable.ic_video)
            }


            mediumDir.beVisibleIf(showFileDir)
            mediumName.text = medium.name
            mediumDir.text = medium.path
            mediumDir.maxLines = 2
            mediumDir.ellipsize = TextUtils.TruncateAt.END
            mediumSize.beVisibleIf(showFileSize)
            mediumSize.text = medium.size.formatSize()
            mediumResolution.text = getAudioBitrate(activity.contentResolver,medium.path,medium.type)

            val showVideoDuration = medium.isVideo() || medium.isAudio()
            if (showVideoDuration) {
                videoDuration?.text = medium.videoDuration.getFormattedDuration()
            }
            videoDuration?.beVisibleIf(showVideoDuration)

            mediumCheck.beVisibleIf(isSelected)
            if (isSelected) {
                mediumCheck.background?.applyColorFilter(properPrimaryColor)
                mediumCheck.applyColorFilter(contrastColor)
                mediaItemHolder.background =resources.getDrawable(R.drawable.ic_selected_bg)
            } else mediaItemHolder.setBackgroundResource(0)

            if (isListViewType) {
                mediaItemHolder.isSelected = isSelected
            }

            var path = medium.path
            if (hasOTGConnected && root.context.isPathOnOTG(path)) {
                path = path.getOTGPublicPath(root.context)
            }

            val roundedCorners = when {
                isListViewType -> ROUNDED_CORNERS_SMALL
                else -> ROUNDED_CORNERS_BIG
            }

            if (loadImageInstantly) {
                activity.loadImage(
                    medium.type, path, mediumThumbnail, roundedCorners, medium.getKey(), rotatedImagePaths
                )
            } else {
                mediumThumbnail.setImageDrawable(null)
                delayHandler.postDelayed({
                    val isVisible = visibleItemPaths.contains(medium.path)
                    if (isVisible) {
                        activity.loadImage(
                            medium.type, path, mediumThumbnail, roundedCorners,
                            medium.getKey(), rotatedImagePaths
                        )
                    }
                }, IMAGE_LOAD_DELAY)
            }

            mediumName.setTextColor(textColor)
            mediumDir.setTextColor(textColor)
            playPortraitOutline?.applyColorFilter(textColor)

            if (lastPlayed == medium.path){
                mediumName.setTextColor(properPrimaryColor)
                mediumDir.setTextColor(properPrimaryColor)
                playPortraitOutline?.applyColorFilter(properPrimaryColor.adjustAlpha(.6f))
                mediumSize.setBackgroundColor(properPrimaryColor.adjustAlpha(.6f))
            } else{
                mediumName.setTextColor(textColor)
                mediumDir.setTextColor(textColor)
                playPortraitOutline?.applyColorFilter(textColor.adjustAlpha(.6f))
                mediumSize.setBackgroundColor(textColor.adjustAlpha(.6f))
            }
        }
    }

    private fun setupSection(view: View, section: ThumbnailSection) {
        ThumbnailSectionBinding.bind(view).apply {
            thumbnailSection.text = section.title
            thumbnailSection.setTextColor(textColor)
        }
    }

    override fun onChange(position: Int): String {
        var realIndex = position
        if (isASectionTitle(position)) {
            realIndex++
        }

        return (media[realIndex] as? Medium)?.getBubbleText(sorting, activity, dateFormat, timeFormat) ?: ""
    }

    private fun bindItem(view: View, medium: Medium): MediaItemBinding {
        return if (isListViewType) {
            VideoItemListBinding.bind(view).toMediaItemBinding()
        } else {
            VideoItemGridBinding.bind(view).toMediaItemBinding()
        }
    }
}
