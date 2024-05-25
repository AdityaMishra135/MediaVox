package org.skywaves.mediavox.core.views.customTriStateSwitch;

/**
 * Created by Riccardo Moro on 19/08/2016.
 */
public interface TristateCheckable {
    void toggle();

    void setState(@RMTristateSwitch.State int state);

    @RMTristateSwitch.State
    int getState();
}
