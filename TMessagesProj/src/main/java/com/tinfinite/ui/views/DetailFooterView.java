package com.tinfinite.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/31.
 */
public class DetailFooterView extends LinearLayout {
    @InjectView(R.id.detail_footer_tv_content)
    public TextView tv_content;

    public DetailFooterView(Context context) {
        super(context);
        init();
    }

    public DetailFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetailFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detail_footer, this, false);
        ButterKnife.inject(this, view);
        addView(view);
    }

    public void setContent( int reply, int score){
        int comment = reply;
        int point = score;
        String str = getResources().getString(R.string.StreamDetailCommentPoint);
        tv_content.setText(String.format(str, comment, point));
    }
}
