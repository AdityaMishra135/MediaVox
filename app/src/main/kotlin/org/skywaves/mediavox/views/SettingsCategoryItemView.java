package org.skywaves.mediavox.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import org.skywaves.mediavox.R;

public class SettingsCategoryItemView extends RelativeLayout {

    private final MaterialTextView mTitle;
    private final MaterialTextView mText;
    private ImageView mIcon;

    public SettingsCategoryItemView(@NonNull Context context) {
        this(context, null, 0);
    }

    public SettingsCategoryItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsCategoryItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final int paddingDef = DimensionsUtil.getDimensionPixelSize(context, 12);
        setPadding(DimensionsUtil.getDimensionPixelSize(context, 8), paddingDef, paddingDef, paddingDef);

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        setBackgroundResource(outValue.resourceId);

        View view = View.inflate(context, R.layout.settings_category_list_item, this);
        mTitle = view.findViewById(R.id.settings_list_item_title);
        mText = view.findViewById(R.id.settings_list_item_text);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsCategoryItemView);

        if (mTitle.getTypeface() != null && typedArray.hasValue(R.styleable.SettingsCategoryItemView_android_textStyle)) {
            mTitle.setTypeface(mTitle.getTypeface(), typedArray.getInteger(R.styleable.SettingsCategoryItemView_android_textStyle, Typeface.NORMAL));
        }

        if (typedArray.hasValue(R.styleable.SettingsCategoryItemView_settingItemIcon)) {
            mIcon = view.findViewById(R.id.settings_list_item_icon);
            mIcon.setImageDrawable(typedArray.getDrawable(R.styleable.SettingsCategoryItemView_settingItemIcon));
        }

        mTitle.setText(typedArray.getText(R.styleable.SettingsCategoryItemView_settingItemTitle));
        mText.setText(typedArray.getText(R.styleable.SettingsCategoryItemView_settingItemText));

        typedArray.recycle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mTitle.setEnabled(enabled);
        mText.setEnabled(enabled);
        if (null != mIcon) mIcon.setEnabled(enabled);
        super.setEnabled(enabled);
    }
}