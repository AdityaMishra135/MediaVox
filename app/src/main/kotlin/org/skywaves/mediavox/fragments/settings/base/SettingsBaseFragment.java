package org.skywaves.mediavox.fragments.settings.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import org.skywaves.mediavox.activities.CustomSettingsActivity;
import org.skywaves.mediavox.activities.SettingsActivity;


public abstract class SettingsBaseFragment extends Fragment {

    protected SettingsFragmentsListener mListener;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListener = (SettingsFragmentsListener) getActivity();
        if (mListener instanceof CustomSettingsActivity)
            mListener.setToolbarTitle(getToolbarTitleForFragment());
    }

    protected void requestActivityRestart() {
        if (mListener instanceof CustomSettingsActivity)
            mListener.requiresActivityRestart();
    }

    protected void requiresApplicationRestart(boolean shouldStopPlayback) {
        if (mListener instanceof CustomSettingsActivity)
            mListener.requiresApplicationRestart(shouldStopPlayback);
    }

    public abstract String getFragmentTag();

    @StringRes
    public abstract int getToolbarTitleForFragment();
}