package com.tinfinite.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Components.BackupImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/4/20.
 */
public class PersonalVotesView extends BaseView {

    @InjectView(R.id.view_votes_tv_name)
    public TextView tv_name;
    @InjectView(R.id.view_votes_tv_time)
    public TextView tv_time;
    @InjectView(R.id.view_votes_tv_origin_content)
    public TextView tv_origin_content;
    @InjectView(R.id.view_votes_iv_avatar)
    public BackupImageView iv_avatar;
    @InjectView(R.id.view_votes_iv_origin_image)
    public SimpleDraweeView iv_origin;

    public PersonalVotesView(Context context) {
        super(context);

        init();
    }

    public PersonalVotesView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PersonalVotesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_personal_votes, this, false);

        ButterKnife.inject(this, view);
        iv_avatar.setRoundRadius(AndroidUtilities.dp(24));

        addView(view);
    }

    public void setContent(){

    }
}
