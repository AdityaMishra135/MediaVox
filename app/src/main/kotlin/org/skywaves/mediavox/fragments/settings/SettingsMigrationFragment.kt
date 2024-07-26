package org.skywaves.mediavox.fragments.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.extensions.baseConfig
import org.skywaves.mediavox.core.extensions.getCurrentFormattedDateTime
import org.skywaves.mediavox.core.extensions.getDoesFilePathExist
import org.skywaves.mediavox.core.extensions.getFileOutputStream
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.showErrorToast
import org.skywaves.mediavox.core.extensions.toBoolean
import org.skywaves.mediavox.core.extensions.toFileDirItem
import org.skywaves.mediavox.core.extensions.toInt
import org.skywaves.mediavox.core.extensions.toStringSet
import org.skywaves.mediavox.core.extensions.toast
import org.skywaves.mediavox.core.extensions.writeLn
import org.skywaves.mediavox.core.helpers.ACCENT_COLOR
import org.skywaves.mediavox.core.helpers.BACKGROUND_COLOR
import org.skywaves.mediavox.core.helpers.DATE_FORMAT
import org.skywaves.mediavox.core.helpers.KEEP_LAST_MODIFIED
import org.skywaves.mediavox.core.helpers.LAST_CONFLICT_APPLY_TO_ALL
import org.skywaves.mediavox.core.helpers.LAST_CONFLICT_RESOLUTION
import org.skywaves.mediavox.core.helpers.PERMISSION_READ_STORAGE
import org.skywaves.mediavox.core.helpers.PERMISSION_WRITE_STORAGE
import org.skywaves.mediavox.core.helpers.PRIMARY_COLOR
import org.skywaves.mediavox.core.helpers.SKIP_DELETE_CONFIRMATION
import org.skywaves.mediavox.core.helpers.SORT_ORDER
import org.skywaves.mediavox.core.helpers.TEXT_COLOR
import org.skywaves.mediavox.core.helpers.USE_24_HOUR_FORMAT
import org.skywaves.mediavox.core.helpers.USE_ENGLISH
import org.skywaves.mediavox.core.helpers.WAS_USE_ENGLISH_TOGGLED
import org.skywaves.mediavox.core.helpers.WIDGET_BG_COLOR
import org.skywaves.mediavox.core.helpers.WIDGET_TEXT_COLOR
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.core.helpers.isQPlus
import org.skywaves.mediavox.databinding.FragmentSettingsMigrationBinding
import org.skywaves.mediavox.databinding.FragmentSettingsThemeBinding
import org.skywaves.mediavox.dialogs.ExportFavoritesDialog
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.favoritesDB
import org.skywaves.mediavox.extensions.getFavoriteFromPath
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.helpers.ALBUM_COVERS
import org.skywaves.mediavox.helpers.ALLOW_DOWN_GESTURE
import org.skywaves.mediavox.helpers.ALLOW_INSTANT_CHANGE
import org.skywaves.mediavox.helpers.ALLOW_PHOTO_GESTURES
import org.skywaves.mediavox.helpers.ALLOW_VIDEO_GESTURES
import org.skywaves.mediavox.helpers.AUTOPLAY_VIDEOS
import org.skywaves.mediavox.helpers.BLACK_BACKGROUND
import org.skywaves.mediavox.helpers.BOTTOM_ACTIONS
import org.skywaves.mediavox.helpers.DELETE_EMPTY_FOLDERS
import org.skywaves.mediavox.helpers.DIRECTORY_SORT_ORDER
import org.skywaves.mediavox.helpers.DIR_COLUMN_CNT
import org.skywaves.mediavox.helpers.EDITOR_BRUSH_COLOR
import org.skywaves.mediavox.helpers.EDITOR_BRUSH_HARDNESS
import org.skywaves.mediavox.helpers.EDITOR_BRUSH_SIZE
import org.skywaves.mediavox.helpers.EXCLUDED_FOLDERS
import org.skywaves.mediavox.helpers.EXTENDED_DETAILS
import org.skywaves.mediavox.helpers.FILE_LOADING_PRIORITY
import org.skywaves.mediavox.helpers.FILTER_MEDIA
import org.skywaves.mediavox.helpers.FOLDER_MEDIA_COUNT
import org.skywaves.mediavox.helpers.GROUP_BY
import org.skywaves.mediavox.helpers.GROUP_DIRECT_SUBFOLDERS
import org.skywaves.mediavox.helpers.HIDE_EXTENDED_DETAILS
import org.skywaves.mediavox.helpers.HIDE_SYSTEM_UI
import org.skywaves.mediavox.helpers.INCLUDED_FOLDERS
import org.skywaves.mediavox.helpers.LAST_EDITOR_CROP_ASPECT_RATIO
import org.skywaves.mediavox.helpers.LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X
import org.skywaves.mediavox.helpers.LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y
import org.skywaves.mediavox.helpers.LIMIT_FOLDER_TITLE
import org.skywaves.mediavox.helpers.LOOP_VIDEOS
import org.skywaves.mediavox.helpers.MAX_BRIGHTNESS
import org.skywaves.mediavox.helpers.MEDIA_COLUMN_CNT
import org.skywaves.mediavox.helpers.OPEN_VIDEOS_ON_SEPARATE_SCREEN
import org.skywaves.mediavox.helpers.PINNED_FOLDERS
import org.skywaves.mediavox.helpers.REMEMBER_LAST_VIDEO_POSITION
import org.skywaves.mediavox.helpers.SCREEN_ROTATION
import org.skywaves.mediavox.helpers.SEARCH_ALL_FILES_BY_DEFAULT
import org.skywaves.mediavox.helpers.SHOW_ALL
import org.skywaves.mediavox.helpers.SHOW_DIR_SIZE
import org.skywaves.mediavox.helpers.SHOW_EXTENDED_DETAILS
import org.skywaves.mediavox.helpers.SHOW_HIDDEN_MEDIA
import org.skywaves.mediavox.helpers.SHOW_MEDIUM_FILE_SIZE
import org.skywaves.mediavox.helpers.SHOW_NOTCH
import org.skywaves.mediavox.helpers.SHOW_THUMBNAIL_FILE_DIR
import org.skywaves.mediavox.helpers.SHOW_THUMBNAIL_FILE_TYPES
import org.skywaves.mediavox.helpers.SHOW_WIDGET_FOLDER_NAME
import org.skywaves.mediavox.helpers.SLIDESHOW_INCLUDE_VIDEOS
import org.skywaves.mediavox.helpers.SLIDESHOW_INTERVAL
import org.skywaves.mediavox.helpers.SLIDESHOW_LOOP
import org.skywaves.mediavox.helpers.SLIDESHOW_MOVE_BACKWARDS
import org.skywaves.mediavox.helpers.SLIDESHOW_RANDOM_ORDER
import org.skywaves.mediavox.helpers.USE_RECYCLE_BIN
import org.skywaves.mediavox.helpers.VIEW_TYPE_FILES
import org.skywaves.mediavox.helpers.VIEW_TYPE_FOLDERS
import org.skywaves.mediavox.helpers.VISIBLE_BOTTOM_ACTIONS
import org.skywaves.mediavox.models.AlbumCover
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class SettingsMigrationFragment : SettingsBaseFragment() {
    companion object {
        val TAG: String = SettingsMigrationFragment::class.java.simpleName

        val instance: SettingsMigrationFragment
            get() = SettingsMigrationFragment()

        private const val PICK_IMPORT_SOURCE_INTENT = 1
        private const val SELECT_EXPORT_FAVORITES_FILE_INTENT = 2
        private const val SELECT_IMPORT_FAVORITES_FILE_INTENT = 3
    }

    private var _binding: FragmentSettingsMigrationBinding? = null
    private val binding get() = _binding!!

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.migrating
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsMigrationBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExportFavorites()
        setupImportFavorites()
        setupExportSettings()
        setupImportSettings()
        arrayOf(
            binding.settingsExport,
            binding.settingsExportFavorites,
            binding.settingsImport,
            binding.settingsImportFavorites,
        ).forEach {
            it.setTextColor(requireActivity().getProperTextColor())
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_IMPORT_SOURCE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val inputStream = requireContext().contentResolver.openInputStream(resultData.data!!)
            parseFile(inputStream)
        } else if (requestCode == SELECT_EXPORT_FAVORITES_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val outputStream = requireContext().contentResolver.openOutputStream(resultData.data!!)
            exportFavoritesTo(outputStream)
        } else if (requestCode == SELECT_IMPORT_FAVORITES_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val inputStream = requireContext().contentResolver.openInputStream(resultData.data!!)
            importFavorites(inputStream)
        }
    }

    private fun setupExportFavorites() {
        binding.settingsExportFavoritesHolder.setOnClickListener {
            if (isQPlus()) {
                ExportFavoritesDialog(requireActivity() as BaseSimpleActivity, getExportFavoritesFilename(), true) { path, filename ->
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, filename)
                        addCategory(Intent.CATEGORY_OPENABLE)

                        try {
                            startActivityForResult(this, SELECT_EXPORT_FAVORITES_FILE_INTENT)
                        } catch (e: ActivityNotFoundException) {
                            requireContext().toast(org.skywaves.mediavox.core.R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            requireContext().showErrorToast(e)
                        }
                    }
                }
            } else {
                (requireActivity() as BaseSimpleActivity).handlePermission(PERMISSION_WRITE_STORAGE) {
                    if (it) {
                        ExportFavoritesDialog(requireActivity() as BaseSimpleActivity, getExportFavoritesFilename(), false) { path, filename ->
                            val file = File(path)
                            (requireActivity() as BaseSimpleActivity).getFileOutputStream(file.toFileDirItem((requireActivity() as BaseSimpleActivity)), true) {
                                exportFavoritesTo(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun exportFavoritesTo(outputStream: OutputStream?) {
        if (outputStream == null) {
            requireContext().toast(org.skywaves.mediavox.core.R.string.unknown_error_occurred)
            return
        }

        ensureBackgroundThread {
            val favoritePaths = requireContext().favoritesDB.getValidFavoritePaths()
            if (favoritePaths.isNotEmpty()) {
                outputStream.bufferedWriter().use { out ->
                    favoritePaths.forEach { path ->
                        out.writeLn(path)
                    }
                }

                requireContext().toast(org.skywaves.mediavox.core.R.string.exporting_successful)
            } else {
                requireContext().toast(org.skywaves.mediavox.core.R.string.no_items_found)
            }
        }
    }

    private fun getExportFavoritesFilename(): String {
        val appName = requireContext().baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro").removePrefix("org.skywaves.")
        return "$appName-favorites_${requireContext().getCurrentFormattedDateTime()}"
    }

    private fun setupImportFavorites() {
        binding.settingsImportFavoritesHolder.setOnClickListener {
            if (isQPlus()) {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    startActivityForResult(this,
                        SELECT_IMPORT_FAVORITES_FILE_INTENT
                    )
                }
            } else {
                (requireActivity() as BaseSimpleActivity).handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        FilePickerDialog(requireActivity() as BaseSimpleActivity) {
                            ensureBackgroundThread {
                                importFavorites(File(it).inputStream())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun importFavorites(inputStream: InputStream?) {
        if (inputStream == null) {
            requireContext().toast(org.skywaves.mediavox.core.R.string.unknown_error_occurred)
            return
        }

        ensureBackgroundThread {
            var importedItems = 0
            inputStream.bufferedReader().use {
                while (true) {
                    try {
                        val line = it.readLine() ?: break
                        if (requireContext().getDoesFilePathExist(line)) {
                            val favorite = requireContext().getFavoriteFromPath(line)
                            requireContext().favoritesDB.insert(favorite)
                            importedItems++
                        }
                    } catch (e: Exception) {
                        requireContext().showErrorToast(e)
                    }
                }
            }

            requireContext().toast(if (importedItems > 0) org.skywaves.mediavox.core.R.string.importing_successful else org.skywaves.mediavox.core.R.string.no_entries_for_importing)
        }
    }

    private fun setupExportSettings() {
        binding.settingsExportHolder.setOnClickListener {
            val configItems = LinkedHashMap<String, Any>().apply {
                put(TEXT_COLOR, requireContext().config.textColor)
                put(BACKGROUND_COLOR, requireContext().config.backgroundColor)
                put(PRIMARY_COLOR, requireContext().config.primaryColor)
                put(ACCENT_COLOR, requireContext().config.accentColor)
                put(USE_ENGLISH, requireContext().config.useEnglish)
                put(WAS_USE_ENGLISH_TOGGLED, requireContext().config.wasUseEnglishToggled)
                put(WIDGET_BG_COLOR, requireContext().config.widgetBgColor)
                put(WIDGET_TEXT_COLOR, requireContext().config.widgetTextColor)
                put(DATE_FORMAT, requireContext().config.dateFormat)
                put(USE_24_HOUR_FORMAT, requireContext().config.use24HourFormat)
                put(INCLUDED_FOLDERS, TextUtils.join(",", requireContext().config.includedFolders))
                put(EXCLUDED_FOLDERS, TextUtils.join(",", requireContext().config.excludedFolders))
                put(SHOW_HIDDEN_MEDIA, requireContext().config.showHiddenMedia)
                put(FILE_LOADING_PRIORITY, requireContext().config.fileLoadingPriority)
                put(AUTOPLAY_VIDEOS, requireContext().config.autoplayVideos)
                put(REMEMBER_LAST_VIDEO_POSITION, requireContext().config.rememberLastVideoPosition)
                put(LOOP_VIDEOS, requireContext().config.loopVideos)
                put(OPEN_VIDEOS_ON_SEPARATE_SCREEN, requireContext().config.openVideosOnSeparateScreen)
                put(ALLOW_VIDEO_GESTURES, requireContext().config.allowVideoGestures)
                put(SHOW_THUMBNAIL_FILE_TYPES, requireContext().config.showThumbnailFileTypes)
                put(SHOW_THUMBNAIL_FILE_DIR, requireContext().config.showThumbnailFileDir)
                put(SHOW_MEDIUM_FILE_SIZE, requireContext().config.showMediumSize)
                put(MAX_BRIGHTNESS, requireContext().config.maxBrightness)
                put(BLACK_BACKGROUND, requireContext().config.blackBackground)
                put(HIDE_SYSTEM_UI, requireContext().config.hideSystemUI)
                put(ALLOW_INSTANT_CHANGE, requireContext().config.allowInstantChange)
                put(ALLOW_PHOTO_GESTURES, requireContext().config.allowPhotoGestures)
                put(ALLOW_DOWN_GESTURE, requireContext().config.allowDownGesture)
                put(SHOW_NOTCH, requireContext().config.showNotch)
                put(SCREEN_ROTATION, requireContext().config.screenRotation)
                put(SHOW_EXTENDED_DETAILS, requireContext().config.showExtendedDetails)
                put(HIDE_EXTENDED_DETAILS, requireContext().config.hideExtendedDetails)
                put(EXTENDED_DETAILS, requireContext().config.extendedDetails)
                put(DELETE_EMPTY_FOLDERS, requireContext().config.deleteEmptyFolders)
                put(KEEP_LAST_MODIFIED, requireContext().config.keepLastModified)
                put(SKIP_DELETE_CONFIRMATION, requireContext().config.skipDeleteConfirmation)
                put(BOTTOM_ACTIONS, requireContext().config.bottomActions)
                put(VISIBLE_BOTTOM_ACTIONS, requireContext().config.visibleBottomActions)
                put(USE_RECYCLE_BIN, requireContext().config.useRecycleBin)
                put(SORT_ORDER, requireContext().config.sorting)
                put(DIRECTORY_SORT_ORDER, requireContext().config.directorySorting)
                put(GROUP_BY, requireContext().config.groupBy)
                put(GROUP_DIRECT_SUBFOLDERS, requireContext().config.groupDirectSubfolders)
                put(PINNED_FOLDERS, TextUtils.join(",", requireContext().config.pinnedFolders))
                put(FILTER_MEDIA, requireContext().config.filterMedia)
                put(DIR_COLUMN_CNT, requireContext().config.dirColumnCnt)
                put(MEDIA_COLUMN_CNT, requireContext().config.mediaColumnCnt)
                put(SHOW_ALL, requireContext().config.showAll)
                put(SHOW_WIDGET_FOLDER_NAME, requireContext().config.showWidgetFolderName)
                put(VIEW_TYPE_FILES, requireContext().config.viewTypeFiles)
                put(VIEW_TYPE_FOLDERS, requireContext().config.viewTypeFolders)
                put(SLIDESHOW_INTERVAL, requireContext().config.slideshowInterval)
                put(SLIDESHOW_INCLUDE_VIDEOS, requireContext().config.slideshowIncludeVideos)
                put(SLIDESHOW_RANDOM_ORDER, requireContext().config.slideshowRandomOrder)
                put(SLIDESHOW_MOVE_BACKWARDS, requireContext().config.slideshowMoveBackwards)
                put(SLIDESHOW_LOOP, requireContext().config.loopSlideshow)
                put(LAST_EDITOR_CROP_ASPECT_RATIO, requireContext().config.lastEditorCropAspectRatio)
                put(LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X, requireContext().config.lastEditorCropOtherAspectRatioX)
                put(LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y, requireContext().config.lastEditorCropOtherAspectRatioY)
                put(LAST_CONFLICT_RESOLUTION, requireContext().config.lastConflictResolution)
                put(LAST_CONFLICT_APPLY_TO_ALL, requireContext().config.lastConflictApplyToAll)
                put(EDITOR_BRUSH_COLOR, requireContext().config.editorBrushColor)
                put(EDITOR_BRUSH_HARDNESS, requireContext().config.editorBrushHardness)
                put(EDITOR_BRUSH_SIZE, requireContext().config.editorBrushSize)
                put(ALBUM_COVERS, requireContext().config.albumCovers)
                put(FOLDER_MEDIA_COUNT, requireContext().config.showFolderMediaCount)
                put(LIMIT_FOLDER_TITLE, requireContext().config.limitFolderTitle)
                put(SHOW_DIR_SIZE, requireContext().config.showDirSize)
                put(SEARCH_ALL_FILES_BY_DEFAULT, requireContext().config.searchAllFilesByDefault)
            }

            (requireActivity() as BaseSimpleActivity).exportSettings(configItems)
        }
    }

    private fun setupImportSettings() {
        binding.settingsImportHolder.setOnClickListener {
            if (isQPlus()) {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    startActivityForResult(this, PICK_IMPORT_SOURCE_INTENT)
                }
            } else {
                (requireActivity() as BaseSimpleActivity).handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        FilePickerDialog(requireActivity() as BaseSimpleActivity) {
                            ensureBackgroundThread {
                                parseFile(File(it).inputStream())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun parseFile(inputStream: InputStream?) {
        if (inputStream == null) {
            requireContext().toast(org.skywaves.mediavox.core.R.string.unknown_error_occurred)
            return
        }

        var importedItems = 0
        val configValues = LinkedHashMap<String, Any>()
        inputStream.bufferedReader().use {
            while (true) {
                try {
                    val line = it.readLine() ?: break
                    val split = line.split("=".toRegex(), 2)
                    if (split.size == 2) {
                        configValues[split[0]] = split[1]
                    }
                    importedItems++
                } catch (e: Exception) {
                    requireContext().showErrorToast(e)
                }
            }
        }

        for ((key, value) in configValues) {
            when (key) {
                TEXT_COLOR -> requireContext().config.textColor = value.toInt()
                BACKGROUND_COLOR -> requireContext().config.backgroundColor = value.toInt()
                PRIMARY_COLOR -> requireContext().config.primaryColor = value.toInt()
                ACCENT_COLOR -> requireContext().config.accentColor = value.toInt()

                USE_ENGLISH -> requireContext().config.useEnglish = value.toBoolean()
                WAS_USE_ENGLISH_TOGGLED -> requireContext().config.wasUseEnglishToggled = value.toBoolean()
                WIDGET_BG_COLOR -> requireContext().config.widgetBgColor = value.toInt()
                WIDGET_TEXT_COLOR -> requireContext().config.widgetTextColor = value.toInt()
                DATE_FORMAT -> requireContext().config.dateFormat = value.toString()
                USE_24_HOUR_FORMAT -> requireContext().config.use24HourFormat = value.toBoolean()
                INCLUDED_FOLDERS -> requireContext().config.addIncludedFolders(value.toStringSet())
                EXCLUDED_FOLDERS -> requireContext().config.addExcludedFolders(value.toStringSet())
                SHOW_HIDDEN_MEDIA -> requireContext().config.showHiddenMedia = value.toBoolean()
                FILE_LOADING_PRIORITY -> requireContext().config.fileLoadingPriority = value.toInt()
                AUTOPLAY_VIDEOS -> requireContext().config.autoplayVideos = value.toBoolean()
                REMEMBER_LAST_VIDEO_POSITION -> requireContext().config.rememberLastVideoPosition = value.toBoolean()
                LOOP_VIDEOS -> requireContext().config.loopVideos = value.toBoolean()
                OPEN_VIDEOS_ON_SEPARATE_SCREEN -> requireContext().config.openVideosOnSeparateScreen = value.toBoolean()
                ALLOW_VIDEO_GESTURES -> requireContext().config.allowVideoGestures = value.toBoolean()
                SHOW_THUMBNAIL_FILE_TYPES -> requireContext().config.showThumbnailFileTypes = value.toBoolean()
                SHOW_THUMBNAIL_FILE_DIR -> requireContext().config.showThumbnailFileDir = value.toBoolean()
                SHOW_MEDIUM_FILE_SIZE -> requireContext().config.showMediumSize = value.toBoolean()
                MAX_BRIGHTNESS -> requireContext().config.maxBrightness = value.toBoolean()
                BLACK_BACKGROUND -> requireContext().config.blackBackground = value.toBoolean()
                HIDE_SYSTEM_UI -> requireContext().config.hideSystemUI = value.toBoolean()
                ALLOW_INSTANT_CHANGE -> requireContext().config.allowInstantChange = value.toBoolean()
                ALLOW_PHOTO_GESTURES -> requireContext().config.allowPhotoGestures = value.toBoolean()
                ALLOW_DOWN_GESTURE -> requireContext().config.allowDownGesture = value.toBoolean()
                SHOW_NOTCH -> requireContext().config.showNotch = value.toBoolean()
                SCREEN_ROTATION -> requireContext().config.screenRotation = value.toInt()
                SHOW_EXTENDED_DETAILS -> requireContext().config.showExtendedDetails = value.toBoolean()
                HIDE_EXTENDED_DETAILS -> requireContext().config.hideExtendedDetails = value.toBoolean()
                EXTENDED_DETAILS -> requireContext().config.extendedDetails = value.toInt()
                DELETE_EMPTY_FOLDERS -> requireContext().config.deleteEmptyFolders = value.toBoolean()
                KEEP_LAST_MODIFIED -> requireContext().config.keepLastModified = value.toBoolean()
                SKIP_DELETE_CONFIRMATION -> requireContext().config.skipDeleteConfirmation = value.toBoolean()
                BOTTOM_ACTIONS -> requireContext().config.bottomActions = value.toBoolean()
                VISIBLE_BOTTOM_ACTIONS -> requireContext().config.visibleBottomActions = value.toInt()
                USE_RECYCLE_BIN -> requireContext().config.useRecycleBin = value.toBoolean()
                SORT_ORDER -> requireContext().config.sorting = value.toInt()
                DIRECTORY_SORT_ORDER -> requireContext().config.directorySorting = value.toInt()
                GROUP_BY -> requireContext().config.groupBy = value.toInt()
                GROUP_DIRECT_SUBFOLDERS -> requireContext().config.groupDirectSubfolders = value.toBoolean()
                PINNED_FOLDERS -> requireContext().config.addPinnedFolders(value.toStringSet())
                FILTER_MEDIA -> requireContext().config.filterMedia = value.toInt()
                DIR_COLUMN_CNT -> requireContext().config.dirColumnCnt = value.toInt()
                MEDIA_COLUMN_CNT -> requireContext().config.mediaColumnCnt = value.toInt()
                SHOW_ALL -> requireContext().config.showAll = value.toBoolean()
                SHOW_WIDGET_FOLDER_NAME -> requireContext().config.showWidgetFolderName = value.toBoolean()
                VIEW_TYPE_FILES -> requireContext().config.viewTypeFiles = value.toInt()
                VIEW_TYPE_FOLDERS -> requireContext().config.viewTypeFolders = value.toInt()
                SLIDESHOW_INTERVAL -> requireContext().config.slideshowInterval = value.toInt()
                SLIDESHOW_INCLUDE_VIDEOS -> requireContext().config.slideshowIncludeVideos = value.toBoolean()
                SLIDESHOW_RANDOM_ORDER -> requireContext().config.slideshowRandomOrder = value.toBoolean()
                SLIDESHOW_MOVE_BACKWARDS -> requireContext().config.slideshowMoveBackwards = value.toBoolean()
                SLIDESHOW_LOOP -> requireContext().config.loopSlideshow = value.toBoolean()
                LAST_EDITOR_CROP_ASPECT_RATIO -> requireContext().config.lastEditorCropAspectRatio = value.toInt()
                LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X -> requireContext().config.lastEditorCropOtherAspectRatioX = value.toString().toFloat()
                LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y -> requireContext().config.lastEditorCropOtherAspectRatioY = value.toString().toFloat()
                LAST_CONFLICT_RESOLUTION -> requireContext().config.lastConflictResolution = value.toInt()
                LAST_CONFLICT_APPLY_TO_ALL -> requireContext().config.lastConflictApplyToAll = value.toBoolean()
                EDITOR_BRUSH_COLOR -> requireContext().config.editorBrushColor = value.toInt()
                EDITOR_BRUSH_HARDNESS -> requireContext().config.editorBrushHardness = value.toString().toFloat()
                EDITOR_BRUSH_SIZE -> requireContext().config.editorBrushSize = value.toString().toFloat()
                FOLDER_MEDIA_COUNT -> requireContext().config.showFolderMediaCount = value.toBoolean()
                LIMIT_FOLDER_TITLE -> requireContext().config.limitFolderTitle = value.toBoolean()
                SHOW_DIR_SIZE -> requireContext().config.showDirSize = value.toBoolean()
                SEARCH_ALL_FILES_BY_DEFAULT -> requireContext().config.searchAllFilesByDefault = value.toBoolean()
                ALBUM_COVERS -> {
                    val existingCovers = requireContext().config.parseAlbumCovers()
                    val existingCoverPaths = existingCovers.map { it.path }.toMutableList() as ArrayList<String>

                    val listType = object : TypeToken<List<AlbumCover>>() {}.type
                    val covers = Gson().fromJson<ArrayList<AlbumCover>>(value.toString(), listType) ?: ArrayList(1)
                    covers.filter { !existingCoverPaths.contains(it.path) && requireContext().getDoesFilePathExist(it.tmb) }.forEach {
                        existingCovers.add(it)
                    }

                    requireContext().config.albumCovers = Gson().toJson(existingCovers)
                }
            }
        }

        requireContext().toast(if (configValues.size > 0) org.skywaves.mediavox.core.R.string.settings_imported_successfully else org.skywaves.mediavox.core.R.string.no_entries_for_importing)
    }
}