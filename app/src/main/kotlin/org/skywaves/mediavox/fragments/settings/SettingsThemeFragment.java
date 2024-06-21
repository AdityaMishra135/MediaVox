package org.skywaves.mediavox.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.skywaves.mediavox.R;
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment;

public class SettingsThemeFragment extends SettingsBaseFragment {

    public static final String TAG = SettingsThemeFragment.class.getSimpleName();

    @NonNull
    public static SettingsThemeFragment getInstance() {
        return new SettingsThemeFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getToolbarTitleForFragment() {
        return org.skywaves.mediavox.core.R.string.theme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}