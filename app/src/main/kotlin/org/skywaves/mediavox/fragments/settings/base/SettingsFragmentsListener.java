package org.skywaves.mediavox.fragments.settings.base;

import androidx.annotation.StringRes;


public interface SettingsFragmentsListener {

    void changeFragment(SettingsBaseFragment fragment);

    void setToolbarTitle(@StringRes int titleId);

    void requiresActivityRestart();

    void requiresApplicationRestart(boolean shouldStopPlayback);
}