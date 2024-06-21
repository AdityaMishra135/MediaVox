package org.skywaves.mediavox.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.skywaves.mediavox.R;
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment;


public class SettingsGeneralFragment extends SettingsBaseFragment {

    public static final String TAG = SettingsGeneralFragment.class.getSimpleName();

    @NonNull
    public static SettingsGeneralFragment getInstance() {
        return new SettingsGeneralFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getToolbarTitleForFragment() {
        return org.skywaves.mediavox.core.R.string.general_settings;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_general, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onStop() {
        super.onStop();
    }
}