package com.tinfinite.ui.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;

import org.telegram.messenger.R;

/**
 * Created by PanJiafang on 15/3/26.
 */
public class LoadingDialog extends ProgressDialog{
    private Context context;

    public LoadingDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        init();
    }

    private void init(){
        setView(LayoutInflater.from(context).inflate(R.layout.load_dialog, null));
    }
}
