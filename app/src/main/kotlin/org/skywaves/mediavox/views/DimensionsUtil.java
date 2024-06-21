package org.skywaves.mediavox.views;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

public class DimensionsUtil {

    public static float getDimension(@NonNull Context context, float dimenDp) {
        float factor = (float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return factor * dimenDp;
    }

    public static int getDimensionPixelSize(@NonNull Context context, float dimenDp) {
        float factor = (float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return Math.round(factor * dimenDp);
    }
}