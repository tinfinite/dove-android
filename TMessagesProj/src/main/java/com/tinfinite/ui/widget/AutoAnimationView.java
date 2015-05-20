package com.tinfinite.ui.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by caiying on 12/27/14.
 */
public class AutoAnimationView extends View {
    public AutoAnimationView(Context paramContext) {
        super(paramContext);
    }

    public AutoAnimationView(Context paramContext,
                             AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public AutoAnimationView(Context paramContext,
                             AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    protected void onAttachedToWindow() {
        ((AnimationDrawable) getBackground()).start();
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        ((AnimationDrawable) getBackground()).stop();
        super.onDetachedFromWindow();
    }
}
