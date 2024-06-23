package org.skywaves.mediavox.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.dialogs.RadioGroupDialog
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.models.RadioItem
import org.skywaves.mediavox.databinding.FragmentSecurityBinding
import org.skywaves.mediavox.databinding.FragmentSettingsMediaConfigurationBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.helpers.ROTATE_BY_ASPECT_RATIO
import org.skywaves.mediavox.helpers.ROTATE_BY_DEVICE_ROTATION
import org.skywaves.mediavox.helpers.ROTATE_BY_SYSTEM_SETTING

class SettingsMediaConfigurationFragment : SettingsBaseFragment() {
    private var _binding: FragmentSettingsMediaConfigurationBinding? = null
    private val binding get() = _binding!!

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun getToolbarTitleForFragment(): Int {
        return R.string.media_configuration
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsMediaConfigurationBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAutoplayVideos()
        setupRememberLastVideo()
        setupLoopVideos()
        setupOpenVideosOnSeparateScreen()
        setupMaxBrightness()
        setupDarkBackground()
        setupScreenRotation()
        setupHideSystemUI()
        setupAllowPhotoGestures()
        setupAllowVideoGestures()
        setupAllowDownGesture()
        setupAllowInstantChange()
        arrayOf(
            binding.settingsScreenRotation,
            binding.settingsScreenRotationLabel,
        ).forEach {
            it.setTextColor(requireActivity().getProperTextColor())
        }
        arrayOf(
            binding.settingsAllowDownGesture,
            binding.settingsAllowVideoGestures,
            binding.settingsBlackBackground,
            binding.settingsLoopVideos,
            binding.settingsAllowPhotoGestures,
            binding.settingsHideSystemUi,
            binding.settingsRememberLastVideoPosition,
            binding.settingsOpenVideosOnSeparateScreen,
            binding.settingsAllowInstantChange,
            binding.settingsMaxBrightness,
            binding.settingsAutoplayVideos,
        ).forEach {
            it.setColors(requireActivity().getProperTextColor(),requireActivity().getProperTextColor(),0)
        }

    }

    companion object {
        val TAG: String = SettingsMediaConfigurationFragment::class.java.simpleName

        val instance: SettingsMediaConfigurationFragment
            get() = SettingsMediaConfigurationFragment()
    }

    private fun setupAutoplayVideos() {
        binding.settingsAutoplayVideos.isChecked = requireContext().config.autoplayVideos
        binding.settingsAutoplayVideosHolder.setOnClickListener {
            binding.settingsAutoplayVideos.toggle()
            requireContext().config.autoplayVideos = binding.settingsAutoplayVideos.isChecked
        }
    }

    private fun setupRememberLastVideo() {
        binding.settingsRememberLastVideoPosition.isChecked =requireContext().config.rememberLastVideoPosition
        binding.settingsRememberLastVideoPositionHolder.setOnClickListener {
            binding.settingsRememberLastVideoPosition.toggle()
            requireContext().config.rememberLastVideoPosition = binding.settingsRememberLastVideoPosition.isChecked
        }
    }

    private fun setupLoopVideos() {
        binding.settingsLoopVideos.isChecked = requireContext().config.loopVideos
        binding.settingsLoopVideosHolder.setOnClickListener {
            binding.settingsLoopVideos.toggle()
            requireContext().config.loopVideos = binding.settingsLoopVideos.isChecked
        }
    }

    private fun setupOpenVideosOnSeparateScreen() {
        binding.settingsOpenVideosOnSeparateScreen.isChecked = requireContext().config.openVideosOnSeparateScreen
        binding.settingsOpenVideosOnSeparateScreenHolder.setOnClickListener {
            binding.settingsOpenVideosOnSeparateScreen.toggle()
            requireContext().config.openVideosOnSeparateScreen = binding.settingsOpenVideosOnSeparateScreen.isChecked
        }
    }

    private fun setupMaxBrightness() {
        binding.settingsMaxBrightness.isChecked = requireContext().config.maxBrightness
        binding.settingsMaxBrightnessHolder.setOnClickListener {
            binding.settingsMaxBrightness.toggle()
            requireContext().config.maxBrightness = binding.settingsMaxBrightness.isChecked
        }
    }


    private fun setupDarkBackground() {
        binding.settingsBlackBackground.isChecked = requireContext().config.blackBackground
        binding.settingsBlackBackgroundHolder.setOnClickListener {
            binding.settingsBlackBackground.toggle()
            requireContext().config.blackBackground = binding.settingsBlackBackground.isChecked
        }
    }

    private fun setupHideSystemUI() {
        binding.settingsHideSystemUi.isChecked = requireContext().config.hideSystemUI
        binding.settingsHideSystemUiHolder.setOnClickListener {
            binding.settingsHideSystemUi.toggle()
            requireContext().config.hideSystemUI = binding.settingsHideSystemUi.isChecked
        }
    }


    private fun setupAllowPhotoGestures() {
        binding.settingsAllowPhotoGestures.isChecked = requireContext().config.allowPhotoGestures
        binding.settingsAllowPhotoGesturesHolder.setOnClickListener {
            binding.settingsAllowPhotoGestures.toggle()
            requireContext().config.allowPhotoGestures = binding.settingsAllowPhotoGestures.isChecked
        }
    }

    private fun setupAllowVideoGestures() {
        binding.settingsAllowVideoGestures.isChecked = requireContext().config.allowVideoGestures
        binding.settingsAllowVideoGesturesHolder.setOnClickListener {
            binding.settingsAllowVideoGestures.toggle()
            requireContext().config.allowVideoGestures = binding.settingsAllowVideoGestures.isChecked
        }
    }

    private fun setupAllowDownGesture() {
        binding.settingsAllowDownGesture.isChecked = requireContext().config.allowDownGesture
        binding.settingsAllowDownGestureHolder.setOnClickListener {
            binding.settingsAllowDownGesture.toggle()
            requireContext().config.allowDownGesture = binding.settingsAllowDownGesture.isChecked
        }
    }


    private fun setupAllowInstantChange() {
        binding.settingsAllowInstantChange.isChecked = requireContext().config.allowInstantChange
        binding.settingsAllowInstantChangeHolder.setOnClickListener {
            binding.settingsAllowInstantChange.toggle()
            requireContext().config.allowInstantChange = binding.settingsAllowInstantChange.isChecked
        }
    }

    private fun setupScreenRotation() {
        binding.settingsScreenRotation.text = getScreenRotationText()
        binding.settingsScreenRotationHolder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(ROTATE_BY_SYSTEM_SETTING, getString(R.string.screen_rotation_system_setting)),
                RadioItem(ROTATE_BY_DEVICE_ROTATION, getString(R.string.screen_rotation_device_rotation)),
                RadioItem(ROTATE_BY_ASPECT_RATIO, getString(R.string.screen_rotation_aspect_ratio))
            )

            RadioGroupDialog(requireActivity(), items, requireContext().config.screenRotation) {
                requireContext().config.screenRotation = it as Int
                binding.settingsScreenRotation.text = getScreenRotationText()
            }
        }
    }

    private fun getScreenRotationText() = getString(
        when (requireContext().config.screenRotation) {
            ROTATE_BY_SYSTEM_SETTING -> R.string.screen_rotation_system_setting
            ROTATE_BY_DEVICE_ROTATION -> R.string.screen_rotation_device_rotation
            else -> R.string.screen_rotation_aspect_ratio
        }
    )

}