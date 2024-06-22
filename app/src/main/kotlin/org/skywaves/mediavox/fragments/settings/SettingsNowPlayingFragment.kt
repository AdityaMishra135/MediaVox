package org.skywaves.mediavox.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.skywaves.mediavox.R;
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment;

public class SettingsNowPlayingFragment extends SettingsBaseFragment {

    public static final String TAG = SettingsNowPlayingFragment.class.getSimpleName();

    @NonNull
    public static SettingsNowPlayingFragment getInstance() {
        return new SettingsNowPlayingFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getToolbarTitleForFragment() {
        return R.string.no_media_with_filters;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_now_playing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}