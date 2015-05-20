package com.tinfinite.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import org.telegram.messenger.R;

/**
 * Created by caiying on 12/27/14.
 */
public class LoadingView extends AutoAnimationView {
    public LoadingView(Context paramContext) {
        super(paramContext);
        init();
    }

    public LoadingView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public LoadingView(Context paramContext,
                       AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {
        setBackgroundDrawable(getDrawable());
    }

    protected Drawable getDrawable() {
        return getResources().getDrawable(R.drawable.spinner_animation);
    }

    protected void onMeasure(int paramInt1, int paramInt2) {
        setMeasuredDimension(getBackground().getIntrinsicWidth(),
                getBackground().getIntrinsicHeight());
    }
}
