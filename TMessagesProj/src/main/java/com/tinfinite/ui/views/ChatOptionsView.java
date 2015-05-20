package com.tinfinite.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/4/10.
 */
public class ChatOptionsView extends LinearLayout implements View.OnClickListener {

    @InjectView(R.id.chatoptions_iv_upvote)
    public ImageView iv_upvote;
    @InjectView(R.id.chatoptions_iv_downvote)
    public ImageView iv_downvote;
    @InjectView(R.id.chatoptions_iv_copy)
    public ImageView iv_copy;
    @InjectView(R.id.chatoptions_iv_download)
    public ImageView iv_download;
    @InjectView(R.id.chatoptions_iv_reply)
    public ImageView iv_reply;
    @InjectView(R.id.chatoptions_iv_forward)
    public ImageView iv_forward;
    @InjectView(R.id.chatoptions_iv_delete)
    public ImageView iv_delete;

    @InjectView(R.id.chatoptions_layout_vote)
    public RelativeLayout layout_vote;

    @InjectView(R.id.chatoptions_tv_post)
    public TextView tv_post;

    private OptionsSelectDelegate delegate;
    public ChatOptionsView(Context context) {
        super(context);
        init();
    }

    public ChatOptionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatOptionsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_poststream, this, false);
        ButterKnife.inject(this, view);
        addView(view);
    }

    public void setOptionsAndDelegate(int[] options, boolean up, boolean down, OptionsSelectDelegate delegate){

        if(options.length == 5) {
            layout_vote.setVisibility(GONE);
            if(options[2] == 4){
                iv_download.setVisibility(VISIBLE);
                iv_copy.setVisibility(GONE);
            } else {
                iv_download.setVisibility(GONE);
                iv_copy.setVisibility(VISIBLE);
            }
        }
        else {
            layout_vote.setVisibility(VISIBLE);
            if(options[4] == 4){
                iv_download.setVisibility(VISIBLE);
                iv_copy.setVisibility(GONE);
            } else {
                iv_download.setVisibility(GONE);
                iv_copy.setVisibility(VISIBLE);
            }

            if(up)
                iv_upvote.setImageResource(R.drawable.ic_dialog_upvote_selected);
            else
                iv_upvote.setImageResource(R.drawable.ic_dialog_upvote);
            if(down)
                iv_downvote.setImageResource(R.drawable.ic_dialog_downvote_selected);
            else
                iv_downvote.setImageResource(R.drawable.ic_dialog_downvote);
        }

        this.delegate = delegate;

        iv_upvote.setOnClickListener(this);
        iv_downvote.setOnClickListener(this);
        iv_download.setOnClickListener(this);
        iv_reply.setOnClickListener(this);
        iv_copy.setOnClickListener(this);
        iv_forward.setOnClickListener(this);
        iv_delete.setOnClickListener(this);

        tv_post.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == iv_upvote){
            if(delegate != null)
                delegate.onClick(9);
        } else if(v == iv_downvote){
            if(delegate != null)
                delegate.onClick(10);
        } else if(v == iv_download){
            if(delegate != null)
                delegate.onClick(4);
        } else if(v == iv_copy){
            if(delegate != null)
                delegate.onClick(3);
        } else if(v == iv_reply){
            if(delegate != null)
                delegate.onClick(8);
        } else if(v == iv_forward){
            if(delegate != null)
                delegate.onClick(2);
        } else if(v == iv_delete){
            if(delegate != null)
                delegate.onClick(1);
        } else if(v == tv_post){
            if(delegate != null)
                delegate.onClick(11);
        }
    }

    public interface OptionsSelectDelegate{
        public void onClick(int option);
    }
}
