package com.tinfinite.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tinfinite.ui.widget.LoadingDialog;

import org.telegram.ui.ActionBar.BaseFragment;

/**
 * Created by PanJiafang on 15/4/2.
 */
public class BaseView extends RelativeLayout {

    protected BaseFragment baseFragment;
    protected LoadingDialog loadingDialog;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBaseFragment(BaseFragment baseFragment){
        if (baseFragment == null || baseFragment.getParentActivity() == null) {
            return;
        }
        this.baseFragment = baseFragment;
        loadingDialog = new LoadingDialog(baseFragment.getParentActivity());
    }
}
