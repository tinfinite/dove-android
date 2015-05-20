package com.tinfinite.ui.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tinfinite.entity.PersonalCommentsEntity;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.DateUtil;
import com.tinfinite.utils.StrangerUtils;
import com.tinfinite.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/4/21.
 */
public class PersonalCommentsAdapter extends BaseRecyclerAdapter<PersonalCommentsEntity.PersonalCommentEntity> {

    private LoadingDialog loadingDialog;

    public PersonalCommentsAdapter(BaseFragment baseFragment, ArrayList datas, boolean empty) {
        super(baseFragment, datas, empty);

        loadingDialog = new LoadingDialog(baseFragment.getParentActivity());
    }

    public PersonalCommentsAdapter(BaseFragment baseFragment, ArrayList datas) {
        super(baseFragment, datas);

        loadingDialog = new LoadingDialog(baseFragment.getParentActivity());
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_personal_comments, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
        super.onBindContentItemViewHolder(contentViewHolder, position);

        ContentViewHolder viewHolder = (ContentViewHolder) contentViewHolder;
        final PersonalCommentsEntity.PersonalCommentEntity commentEntity = datas.get(position);

        viewHolder.avatar.setImage(Utils.getFileLocation(commentEntity.getUser().getAvatar()), "50_50", context.getResources().getDrawable(R.drawable.default_profile_img_l));
        viewHolder.tv_name.setText(commentEntity.getUser().getUsername());
        viewHolder.tv_time.setText(DateUtil.lifeTime(commentEntity.getComment().getCreate_at()));
        viewHolder.tv_content.setText(commentEntity.getComment().getContent());

        String origin_content = commentEntity.getPost().getContent().getText();
        viewHolder.tv_origin_content.setVisibility(StringUtils.isEmpty(origin_content) ? View.GONE : View.VISIBLE);
        viewHolder.tv_origin_content.setText(origin_content);

        String origin_image = commentEntity.getPost().getContent().getImage();
        if(StringUtils.isEmpty(origin_image)) {
            viewHolder.iv_origin.setVisibility(View.GONE);
        } else {
            viewHolder.iv_origin.setVisibility(View.VISIBLE);
            String url = "";
            if(origin_image.contains(",")) {
                String[] images = origin_image.split(",");
                url = images[0];
            } else
                url = origin_image;
            if(!url.equals(""))
                url = url + "?imageView2/3/w/200/h/200";
            viewHolder.iv_origin.setImageURI(Uri.parse(url));
        }

        viewHolder.avatar.setOnClickListener(new ClickUserListener(commentEntity.getUser()));
        viewHolder.tv_name.setOnClickListener(new ClickUserListener(commentEntity.getUser()));

    }

    public class ContentViewHolder extends RecyclerView.ViewHolder{

        @InjectView(R.id.view_comments_iv_avatar)
        public BackupImageView avatar;
        @InjectView(R.id.view_comments_tv_name)
        public TextView tv_name;
        @InjectView(R.id.view_comments_tv_time)
        public TextView tv_time;
        @InjectView(R.id.view_comments_tv_content)
        public TextView tv_content;
        @InjectView(R.id.view_comments_tv_origin_content)
        public TextView tv_origin_content;
        @InjectView(R.id.view_comments_iv_origin_image)
        public SimpleDraweeView iv_origin;


        public ContentViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null)
                        itemClickListener.onItemClick(itemView, getAdapterPosition());
                }
            });

            ButterKnife.inject(this, itemView);

            avatar.setRoundRadius(AndroidUtilities.dp(24));
        }
    }

    public class ClickUserListener implements View.OnClickListener {

        private PersonalCommentsEntity.AuthorEntity entity;

        public ClickUserListener(PersonalCommentsEntity.AuthorEntity entity) {
            this.entity = entity;
        }

        @Override
        public void onClick(View v) {
            if(entity == null)
                return;

            String user_id = entity.getTg_user_id();
            String user_name = entity.getUsername();

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
        }
    }
}
