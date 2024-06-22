package org.skywaves.mediavox.fragments.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import org.skywaves.mediavox.R;
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment;

public class SettingsAboutFragment extends SettingsBaseFragment {

    public static final String TAG = SettingsAboutFragment.class.getSimpleName();

    @NonNull
    public static SettingsAboutFragment getInstance() {
        return new SettingsAboutFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getToolbarTitleForFragment() {
        return org.skywaves.mediavox.core.R.string.about;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

      //**do here

    }

}