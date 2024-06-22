package org.skywaves.mediavox.fragments.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.skywaves.mediavox.R;
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment;

public class SettingsContributorsFragment extends SettingsBaseFragment {

    public static final String TAG = SettingsContributorsFragment.class.getSimpleName();

    @NonNull
    public static SettingsContributorsFragment getInstance() {
        return new SettingsContributorsFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getToolbarTitleForFragment() {
        return org.skywaves.mediavox.core.R.string.contributors;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_contributors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


}