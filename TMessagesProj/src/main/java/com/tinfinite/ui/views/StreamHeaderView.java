package com.tinfinite.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.ui.fragment.DiscoveryGroupStreamFragment;
import com.tinfinite.utils.DateUtil;
import com.tinfinite.utils.StrangerUtils;
import com.tinfinite.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessagesController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.GroupJoinActivity;
import org.telegram.ui.ProfileActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/28.
 */
public class StreamHeaderView extends BaseView implements View.OnClickListener {

    public static final int TYPE_GROUP = 1;
    public static final int TYPE_USER = 2;

    @InjectView(R.id.view_stream_header_iv)
    public BackupImageView iv;
    @InjectView(R.id.view_stream_header_tv_name)
    public TextView tv_name;
    @InjectView(R.id.view_stream_header_tv_time)
    public TextView tv_time;
    @InjectView(R.id.view_stream_header_tv_community)
    public TextView tv_community;

    private String user_id;
    private String user_name;
    private String image_url;

    private String community_id;
    private String community_name;
    private String community_image;

    public StreamHeaderView(Context context) {
        super(context);
        init();
    }

    public StreamHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_header, null);
        ButterKnife.inject(this, view);
        iv.setRoundRadius(AndroidUtilities.dp(24));
        addView(view);
    }

    public void setContent(String user_id, String user_name, final String image_url, long time, String community_id, String community_name, String community_image){
        this.user_id = user_id;
        this.user_name = user_name;
        this.image_url = image_url;
        this.community_id = community_id;
        this.community_name = community_name;
        this.community_image = community_image;

        tv_name.setText(user_name);
        iv.setImage(Utils.getFileLocation(image_url), "50_50", getResources().getDrawable(R.drawable.default_profile_img_l));
        tv_time.setText(DateUtil.lifeTime(time));

        if(StringUtils.isEmpty(community_name))
            tv_community.setVisibility(GONE);
        else {
            tv_community.setVisibility(VISIBLE);
            tv_community.setText(community_name);
        }

        iv.setOnClickListener(this);
        tv_name.setOnClickListener(this);
        tv_community.setOnClickListener(this);
    }

    public void setContent(String user_id, String user_name, final String image_url, long time){
        this.user_id = user_id;
        this.user_name = user_name;
        this.image_url = image_url;

        tv_name.setText(user_name);
        iv.setImage(Utils.getFileLocation(image_url), "50_50", getResources().getDrawable(R.drawable.default_profile_img_l));
        tv_time.setText(DateUtil.lifeTime(time));

        tv_community.setVisibility(GONE);

        iv.setOnClickListener(this);
        tv_name.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v == iv || v == tv_name){
            T8Log.PAN_JIA_FANG.d("clikc the tv_name and baseFragment is "+baseFragment);

                Tracker t = ApplicationLoader.getInstance().getTracker(
                        ApplicationLoader.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("信息流")
                        .setAction("人详情")
                        .build());
                TLRPC.User user = MessagesController.getInstance().getUser(Integer.parseInt(user_id));
                if (user == null) {
                    if(loadingDialog != null)
                        loadingDialog.show();
                    StrangerUtils.SearchForStranger(user_name, new StrangerUtils.SearchStrangerDelegate() {
                        @Override
                        public void getResult(TLRPC.User user) {
                            if (loadingDialog != null)
                                loadingDialog.dismiss();
                            if (user != null) {
                                Bundle args = new Bundle();
                                args.putInt("user_id", user.id);
                                baseFragment.presentFragment(new ProfileActivity(args));
                            }
                        }
                    });
                } else {
                    Bundle args = new Bundle();
                    args.putInt("user_id", Integer.parseInt(user_id));
                    baseFragment.presentFragment(new ProfileActivity(args));
                }
        } else if(v == tv_community){
            Tracker t = ApplicationLoader.getInstance().getTracker(
                    ApplicationLoader.TrackerName.APP_TRACKER);
            // Build and send an Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory("信息流")
                    .setAction("群详情")
                    .build());
//            DiscoverEntity.Community community = new DiscoverEntity.Community();
//            community.setThird_group_id(community_id);
//            community.setName(community_name);
//            community.setThird_group_image_key(community_image);
            baseFragment.presentFragment(new DiscoveryGroupStreamFragment(DiscoveryGroupStreamFragment.createBundle(Integer.parseInt(community_id))));
        }
    }
}
