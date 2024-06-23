package org.skywaves.mediavox.fragments.settings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.ColorPickerDialog
import org.skywaves.mediavox.core.dialogs.ConfirmationAdvancedDialog
import org.skywaves.mediavox.core.dialogs.LineColorPickerDialog
import org.skywaves.mediavox.core.dialogs.RadioGroupDialog
import org.skywaves.mediavox.core.extensions.baseConfig
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getColoredMaterialStatusBarColor
import org.skywaves.mediavox.core.extensions.getProperPrimaryColor
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.getThemeId
import org.skywaves.mediavox.core.extensions.isUsingSystemDarkTheme
import org.skywaves.mediavox.core.extensions.setFillWithStroke
import org.skywaves.mediavox.core.extensions.toast
import org.skywaves.mediavox.core.extensions.value
import org.skywaves.mediavox.core.helpers.DARK_GREY
import org.skywaves.mediavox.core.helpers.NavigationIcon
import org.skywaves.mediavox.core.helpers.SAVE_DISCARD_PROMPT_INTERVAL
import org.skywaves.mediavox.core.helpers.isSPlus
import org.skywaves.mediavox.core.models.MyTheme
import org.skywaves.mediavox.core.models.RadioItem
import org.skywaves.mediavox.databinding.FragmentSettingsThemeBinding
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment

class SettingsThemeFragment : SettingsBaseFragment() {

    private val THEME_LIGHT = 0
    private val THEME_DARK = 1
    private val THEME_SOLARIZED = 2
    private val THEME_DARK_RED = 3
    private val THEME_BLACK_WHITE = 4
    private val THEME_CUSTOM = 5
    private val THEME_WHITE = 7
    private val THEME_AUTO = 8
    private val THEME_SYSTEM = 9    // Material You

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAccentColor = 0
    private var curSelectedThemeId = 0
    private var lastSavePromptTS = 0L
    private var hasUnsavedChanges = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null


    private var _binding: FragmentSettingsThemeBinding? = null
    private val binding get() = _binding!!
    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return org.skywaves.mediavox.core.R.string.theme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsThemeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupOptionsMenu()
        refreshMenuItems()


        initColorVariables()

        setupThemes()

        val textColor = if (requireContext().baseConfig.isUsingSystemTheme) {
            requireContext().getProperTextColor()
        } else {
            requireContext().baseConfig.textColor
        }

        updateLabelColors(textColor)
    }



    companion object {
        val TAG: String = SettingsThemeFragment::class.java.simpleName

        val instance: SettingsThemeFragment
            get() = SettingsThemeFragment()
    }


    override fun onResume() {
        super.onResume()
        context!!.setTheme(requireActivity().getThemeId(getCurrentPrimaryColor()))

        if (!context!!.baseConfig.isUsingSystemTheme) {
            (requireActivity() as SettingsActivity).updateBackgroundColor(getCurrentBackgroundColor())
            (requireActivity() as SettingsActivity).updateActionbarColor(getCurrentStatusBarColor())
        }

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            (requireActivity() as SettingsActivity).updateActionbarColor(this)
            context!!.setTheme(requireActivity().getThemeId(this))
        }

        (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Arrow, requireActivity().getColoredMaterialStatusBarColor())
    }

    private fun refreshMenuItems() {
        (requireActivity() as SettingsActivity).binding.settingsToolbar.menu.findItem(org.skywaves.mediavox.core.R.id.save).isVisible = hasUnsavedChanges
    }

    private fun setupOptionsMenu() {
        (requireActivity() as SettingsActivity).binding.settingsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                org.skywaves.mediavox.core.R.id.save -> {
                    saveChanges(true)
                    true
                }

                else -> false
            }
        }
    }

    fun handleBackPressed() {
        if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
        } else {
            (requireActivity() as SettingsActivity).supportFragmentManager.popBackStack()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            if (isSPlus()) {
                put(THEME_SYSTEM, getSystemThemeColors())
            }

            put(THEME_AUTO, getAutoThemeColors())
            put(
                THEME_LIGHT,
                MyTheme(
                    label = getString(org.skywaves.mediavox.core.R.string.light_theme),
                    textColorId = org.skywaves.mediavox.core.R.color.theme_light_text_color,
                    backgroundColorId = org.skywaves.mediavox.core.R.color.theme_light_background_color,
                    primaryColorId = org.skywaves.mediavox.core.R.color.color_primary,
                )
            )
            put(
                THEME_DARK,
                MyTheme(
                    label = getString(org.skywaves.mediavox.core.R.string.dark_theme),
                    textColorId = org.skywaves.mediavox.core.R.color.theme_dark_text_color,
                    backgroundColorId = org.skywaves.mediavox.core.R.color.theme_dark_background_color,
                    primaryColorId = org.skywaves.mediavox.core.R.color.color_primary,
                )
            )
            put(
                THEME_DARK_RED,
                MyTheme(
                    label = getString(org.skywaves.mediavox.core.R.string.dark_red),
                    textColorId = org.skywaves.mediavox.core.R.color.theme_dark_text_color,
                    backgroundColorId = org.skywaves.mediavox.core.R.color.theme_dark_background_color,
                    primaryColorId = org.skywaves.mediavox.core.R.color.theme_dark_red_primary_color,
                )
            )
            put(
                THEME_WHITE,
                MyTheme(
                    label = getString(org.skywaves.mediavox.core.R.string.white),
                    textColorId = org.skywaves.mediavox.core.R.color.dark_grey,
                    backgroundColorId = android.R.color.white,
                    primaryColorId = android.R.color.white,
                )
            )
            put(
                THEME_BLACK_WHITE,
                MyTheme(
                    label = getString(org.skywaves.mediavox.core.R.string.black_white),
                    textColorId = android.R.color.white,
                    backgroundColorId = android.R.color.black,
                    primaryColorId = android.R.color.black,
                )
            )
            put(THEME_CUSTOM, MyTheme(getString(org.skywaves.mediavox.core.R.string.custom), 0, 0, 0))
        }
        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        binding.customizationTheme.text = getThemeText()
        updateAutoThemeFields()
        handleAccentColorLayout()
        binding.customizationThemeHolder.setOnClickListener {
            themePickerClicked()
        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, value.label))
        }

        RadioGroupDialog(requireActivity(), items, curSelectedThemeId) {
            updateColorTheme(it as Int, true)
            if (it != THEME_CUSTOM && it != THEME_AUTO && it != THEME_SYSTEM && !context!!.baseConfig.wasCustomThemeSwitchDescriptionShown) {
                context!!.baseConfig.wasCustomThemeSwitchDescriptionShown = true
                requireActivity().toast(org.skywaves.mediavox.core.R.string.changing_color_description)
            }

            (requireActivity() as SettingsActivity).updateMenuItemColors((requireActivity() as SettingsActivity).binding.settingsToolbar.menu, getCurrentStatusBarColor())
            (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Cross, getCurrentStatusBarColor())
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        binding.customizationTheme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = context!!.baseConfig.customTextColor
                    curBackgroundColor = context!!.baseConfig.customBackgroundColor
                    curPrimaryColor = context!!.baseConfig.customPrimaryColor
                    curAccentColor = context!!.baseConfig.customAccentColor
                    context!!.setTheme(requireActivity().getThemeId(curPrimaryColor))
                    (requireActivity() as SettingsActivity).updateMenuItemColors((requireActivity() as SettingsActivity).binding.settingsToolbar.menu, curPrimaryColor)
                    (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Cross, curPrimaryColor)
                    setupColorsPickers()
                } else {
                    context!!.baseConfig.customPrimaryColor = curPrimaryColor
                    context!!.baseConfig.customAccentColor = curAccentColor
                    context!!.baseConfig.customBackgroundColor = curBackgroundColor
                    context!!.baseConfig.customTextColor = curTextColor
                }
            } else {
                val theme = predefinedThemes[curSelectedThemeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)

                if (curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM) {
                    curPrimaryColor = getColor(theme.primaryColorId)
                    curAccentColor = getColor(org.skywaves.mediavox.core.R.color.color_primary)
                }

                context!!.setTheme(requireActivity().getThemeId(getCurrentPrimaryColor()))
                colorChanged()
                (requireActivity() as SettingsActivity).updateMenuItemColors((requireActivity() as SettingsActivity).binding.settingsToolbar.menu, getCurrentStatusBarColor())
                (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Cross, getCurrentStatusBarColor())
            }
        }

        hasUnsavedChanges = true
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
        (requireActivity() as SettingsActivity).updateBackgroundColor(getCurrentBackgroundColor())
        (requireActivity() as SettingsActivity).updateActionbarColor(getCurrentStatusBarColor())
        updateAutoThemeFields()
        handleAccentColorLayout()
    }

    private fun getAutoThemeColors(): MyTheme {
        val isUsingSystemDarkTheme = context!!.isUsingSystemDarkTheme()
        val textColor = if (isUsingSystemDarkTheme) org.skywaves.mediavox.core.R.color.theme_dark_text_color else org.skywaves.mediavox.core.R.color.theme_light_text_color
        val backgroundColor = if (isUsingSystemDarkTheme) org.skywaves.mediavox.core.R.color.theme_dark_background_color else org.skywaves.mediavox.core.R.color.theme_light_background_color
        return MyTheme(getString(org.skywaves.mediavox.core.R.string.auto_light_dark_theme), textColor, backgroundColor, org.skywaves.mediavox.core.R.color.color_primary)
    }

    // doesn't really matter what colors we use here, everything will be taken from the system. Use the default dark theme values here.
    private fun getSystemThemeColors(): MyTheme {
        return MyTheme(
            getMaterialYouString(),
            org.skywaves.mediavox.core.R.color.theme_dark_text_color,
            org.skywaves.mediavox.core.R.color.theme_dark_background_color,
            org.skywaves.mediavox.core.R.color.color_primary,
        )
    }

    private fun getCurrentThemeId(): Int {
        if ((context!!.baseConfig.isUsingSystemTheme && !hasUnsavedChanges) || curSelectedThemeId == THEME_SYSTEM) {
            return THEME_SYSTEM
        } else if (context!!.baseConfig.isUsingAutoTheme || curSelectedThemeId == THEME_AUTO) {
            return THEME_AUTO
        }

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM  && it.key != THEME_AUTO && it.key != THEME_SYSTEM }) {
                if (curTextColor == getColor(value.textColorId) &&
                    curBackgroundColor == getColor(value.backgroundColorId) &&
                    curPrimaryColor == getColor(value.primaryColorId)
                ) {
                    themeId = key
                }
            }
        }

        return themeId
    }

    private fun getThemeText(): String {
        var label = getString(org.skywaves.mediavox.core.R.string.custom)
        for ((key, value) in predefinedThemes) {
            if (key == curSelectedThemeId) {
                label = value.label
            }
        }
        return label
    }

    private fun updateAutoThemeFields() {
        arrayOf(binding.customizationTextColorHolder, binding.customizationBackgroundColorHolder).forEach {
            it.beVisibleIf(curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM)
        }

        binding.customizationPrimaryColorHolder.beVisibleIf(curSelectedThemeId != THEME_SYSTEM)
    }

    private fun promptSaveDiscard() {
        lastSavePromptTS = System.currentTimeMillis()
        ConfirmationAdvancedDialog(requireActivity(), "", org.skywaves.mediavox.core.R.string.save_before_closing, org.skywaves.mediavox.core.R.string.save, org.skywaves.mediavox.core.R.string.discard) {
            if (it) {
                saveChanges(true)
            } else {
                resetColors()
                (requireActivity() as SettingsActivity).supportFragmentManager.popBackStack()
            }
        }
    }

    private fun saveChanges(finishAfterSave: Boolean) {
        context!!.baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
            accentColor = curAccentColor
        }


        context!!.baseConfig.isUsingAutoTheme = curSelectedThemeId == THEME_AUTO
        context!!.baseConfig.isUsingSystemTheme = curSelectedThemeId == THEME_SYSTEM

        hasUnsavedChanges = false
        if (finishAfterSave) {
            (requireActivity() as SettingsActivity).supportFragmentManager.popBackStack()
        } else {
            refreshMenuItems()
        }
    }

    private fun resetColors() {
        hasUnsavedChanges = false
        initColorVariables()
        setupColorsPickers()
        (requireActivity() as SettingsActivity).updateBackgroundColor()
        val view = (requireActivity() as SettingsActivity).binding
        (requireActivity() as SettingsActivity).setupToolbar2(view.settingsToolbar,NavigationIcon.Arrow,
            context!!.getProperPrimaryColor())
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
    }

    private fun initColorVariables() {
        curTextColor = context!!.baseConfig.textColor
        curBackgroundColor = context!!.baseConfig.backgroundColor
        curPrimaryColor = context!!.baseConfig.primaryColor
        curAccentColor = context!!.baseConfig.accentColor
    }

    private fun setupColorsPickers() {
        val textColor = getCurrentTextColor()
        val backgroundColor = getCurrentBackgroundColor()
        val primaryColor = getCurrentPrimaryColor()
        binding.customizationTextColor.setFillWithStroke(textColor, backgroundColor)
        binding.customizationPrimaryColor.setFillWithStroke(primaryColor, backgroundColor)
        binding.customizationAccentColor.setFillWithStroke(curAccentColor, backgroundColor)
        binding.customizationBackgroundColor.setFillWithStroke(backgroundColor, backgroundColor)

        binding.customizationTextColorHolder.setOnClickListener { pickTextColor() }
        binding.customizationBackgroundColorHolder.setOnClickListener { pickBackgroundColor() }
        binding.customizationPrimaryColorHolder.setOnClickListener { pickPrimaryColor() }
        binding.customizationAccentColorHolder.setOnClickListener { pickAccentColor() }

        handleAccentColorLayout()
    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateLabelColors(color)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        (requireActivity() as SettingsActivity).updateBackgroundColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        (requireActivity() as SettingsActivity).updateActionbarColor(color)
    }


    private fun handleAccentColorLayout() {
        binding.customizationAccentColorHolder.beVisibleIf(curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme() || curSelectedThemeId == THEME_BLACK_WHITE || isCurrentBlackAndWhiteTheme())
        binding.customizationAccentColorLabel.text = getString(
            if (curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme()) {
                org.skywaves.mediavox.core.R.string.accent_color_white
            } else {
                org.skywaves.mediavox.core.R.string.accent_color_black_and_white
            }
        )
    }

    private fun isCurrentWhiteTheme() = curTextColor == DARK_GREY && curPrimaryColor == Color.WHITE && curBackgroundColor == Color.WHITE

    private fun isCurrentBlackAndWhiteTheme() = curTextColor == Color.WHITE && curPrimaryColor == Color.BLACK && curBackgroundColor == Color.BLACK

    private fun pickTextColor() {
        ColorPickerDialog(requireActivity(), curTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(requireActivity(), curBackgroundColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        if (!requireActivity().packageName.startsWith("org.skywaves.", true) && context!!.baseConfig.appRunCount > 50) {
            requireActivity().finish()
            return
        }
        val view =(requireActivity() as SettingsActivity).binding.settingsToolbar
        curPrimaryLineColorPicker = LineColorPickerDialog(requireActivity() as BaseSimpleActivity, curPrimaryColor, true, toolbar = view) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    context!!.setTheme(requireActivity().getThemeId(color))
                }
                (requireActivity() as SettingsActivity).updateMenuItemColors((requireActivity() as SettingsActivity).binding.settingsToolbar.menu, color)
                (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Cross, color)
            } else {
                (requireActivity() as SettingsActivity).updateActionbarColor(curPrimaryColor)
                context!!.setTheme(requireActivity().getThemeId(curPrimaryColor))
                (requireActivity() as SettingsActivity).updateMenuItemColors((requireActivity() as SettingsActivity).binding.settingsToolbar.menu, curPrimaryColor)
                (requireActivity() as SettingsActivity).setupToolbar2((requireActivity() as SettingsActivity).binding.settingsToolbar, NavigationIcon.Cross, curPrimaryColor)
                (requireActivity() as SettingsActivity).updateTopBarColors((requireActivity() as SettingsActivity).binding.settingsToolbar, curPrimaryColor)
            }
        }
    }

    private fun pickAccentColor() {
        ColorPickerDialog(requireActivity(), curAccentColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAccentColor, color)) {
                    curAccentColor = color
                    colorChanged()

                    if (isCurrentWhiteTheme() || isCurrentBlackAndWhiteTheme()) {
                        (requireActivity() as SettingsActivity).updateActionbarColor(getCurrentStatusBarColor())
                    }
                }
            }
        }
    }

    private fun getUpdatedTheme() = getCurrentThemeId()


    private fun updateLabelColors(textColor: Int) {
        arrayListOf(
            binding.customizationThemeLabel,
            binding.customizationTheme,
            binding.customizationTextColorLabel,
            binding.customizationBackgroundColorLabel,
            binding.customizationPrimaryColorLabel,
            binding.customizationAccentColorLabel,
        ).forEach {
            it.setTextColor(textColor)
        }
    }

    private fun getCurrentTextColor() = if (binding.customizationTheme.value == getMaterialYouString()) {
        resources.getColor(org.skywaves.mediavox.core.R.color.you_neutral_text_color)
    } else {
        curTextColor
    }

    private fun getCurrentBackgroundColor() = if (binding.customizationTheme.value == getMaterialYouString()) {
        resources.getColor(org.skywaves.mediavox.core.R.color.you_background_color)
    } else {
        curBackgroundColor
    }

    private fun getCurrentPrimaryColor() = if (binding.customizationTheme.value == getMaterialYouString()) {
        resources.getColor(org.skywaves.mediavox.core.R.color.you_primary_color)
    } else {
        curPrimaryColor
    }

    private fun getCurrentStatusBarColor() = if (binding.customizationTheme.value == getMaterialYouString()) {
        resources.getColor(org.skywaves.mediavox.core.R.color.you_status_bar_color)
    } else {
        curPrimaryColor
    }

    private fun getMaterialYouString() = "${getString(org.skywaves.mediavox.core.R.string.system_default)} (${getString(
        org.skywaves.mediavox.core.R.string.material_you)})"
}
