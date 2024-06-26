package org.skywaves.mediavox.core.views.customTriStateSwitch;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.ColorInt;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.skywaves.mediavox.core.R;

/**
 * Created by Riccardo Moro on 30/07/2016.
 */
public class Utils {

    public static float convertDpToPixel(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(Context context, float px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @ColorInt
    public static int getAccentColor(Context context) {
        TypedValue value = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(value.data, new int[]{com.google.android.material.R.attr.colorAccent});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @ColorInt
    public static int getPrimaryColor(Context context) {
        TypedValue value = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(
                value.data,
                new int[]{com.google.android.material.R.attr.colorAccent});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @ColorInt
    public static int getPrimaryColorDark(Context context) {
        TypedValue value = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(
                value.data,
                new int[]{com.google.android.material.R.attr.colorPrimaryDark});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public static int getDefaultBackgroundColor(Context context) {
        TypedValue value = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(
                value.data,
                new int[]{androidx.appcompat.R.attr.colorControlHighlight});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
