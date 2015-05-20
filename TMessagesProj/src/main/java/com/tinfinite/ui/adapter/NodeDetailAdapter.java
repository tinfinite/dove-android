package com.tinfinite.ui.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.RepliesEntity;
import com.tinfinite.ui.views.DetailFooterView;
import com.tinfinite.ui.views.StreamForwardFullView;
import com.tinfinite.ui.views.StreamHeaderView;
import com.tinfinite.ui.views.StreamPostView;
import com.tinfinite.ui.widget.LinkTextView;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.AvatarUpdateUtils;
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
 * Created by PanJiafang on 15/3/31.
 */
public class NodeDetailAdapter extends BaseRecyclerAdapter<RepliesEntity.ReplyEntity> {
    private NodeEntity nodeEntity;
    private int chat_id;
    private int score;
    private int reply;

    private DetailFooterView footerView;

    private LoadingDialog loadingDialog;

    public NodeDetailAdapter(BaseFragment context, ArrayList datas) {
        super(context, datas);
    }

    public NodeDetailAdapter(BaseFragment context, int chat_id, ArrayList datas, NodeEntity nodeEntity, int reply, int score){
        super(context, datas, true);
        this.nodeEntity = nodeEntity;
        this.chat_id = chat_id;
        this.score = score;
        this.reply = reply;
        loadingDialog = new LoadingDialog(context.getParentActivity());
    }

    @Override
    public int getHeaderItemCount() {
        return nodeEntity == null ? 0 : 1;
    }

    public void refreshData(int chat_id, NodeEntity nodeEntity, int reply, int score){
        this.nodeEntity = nodeEntity;
        this.chat_id = chat_id;
        this.score = score;
        this.reply = reply;

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_node_detail, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stream_reply, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindHeaderItemViewHolder(RecyclerView.ViewHolder headerViewHolder, int position) {
        super.onBindHeaderItemViewHolder(headerViewHolder, position);

        HeaderViewHolder viewHolder = (HeaderViewHolder) headerViewHolder;

        viewHolder.headerView.setBaseFragment(baseFragment);

        NodeEntity.ForwardNodeEntity forwardNode = nodeEntity.getForward();
        NodeEntity.AuthorEntity author = nodeEntity.getAuthor();
        if(forwardNode != null) {
            viewHolder.forwardView.setVisibility(View.VISIBLE);
            viewHolder.postView.setVisibility(View.GONE);
            viewHolder.forwardView.setContent(chat_id, forwardNode, author);
            viewHolder.forwardView.setBaseFragment(baseFragment);

            if(chat_id == 0)
                viewHolder.headerView.setContent(author.getTg_user_id(), author.getUsername(), author.getAvatar(), nodeEntity.getCreate_at().getTime(), forwardNode.getThird_group_id(), forwardNode.getThird_group_name(), forwardNode.getThird_group_image_key());
            else
                viewHolder.headerView.setContent(author.getTg_user_id(), author.getUsername(), author.getAvatar(), nodeEntity.getCreate_at().getTime());
        } else {
            viewHolder.forwardView.setVisibility(View.GONE);
        }

        NodeEntity.PostNodeEntity postNodeEntity = nodeEntity.getPost();
        if(postNodeEntity != null) {
            viewHolder.postView.setVisibility(View.VISIBLE);
            viewHolder.forwardView.setVisibility(View.GONE);
            viewHolder.postView.setContent(postNodeEntity);
            viewHolder.postView.setBaseFragment(baseFragment);
            if(chat_id == 0)
                viewHolder.headerView.setContent(author.getTg_user_id(), author.getUsername(), author.getAvatar(), nodeEntity.getCreate_at().getTime(), postNodeEntity.getThird_group_id(), postNodeEntity.getThird_group_name(), postNodeEntity.getThird_group_image_key());
            else
                viewHolder.headerView.setContent(author.getTg_user_id(), author.getUsername(), author.getAvatar(), nodeEntity.getCreate_at().getTime());
        } else {
            viewHolder.postView.setVisibility(View.GONE);
        }

        viewHolder.footerView.setContent(reply, score);
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
        super.onBindContentItemViewHolder(contentViewHolder, position);

        ContentViewHolder viewHolder = (ContentViewHolder) contentViewHolder;
        Object object = datas.get(position);
        if (object instanceof BaseRecyclerAdapter.EmptyEntity) {
            Log.d("empty", "this is empty ");
            viewHolder.detial_content.setVisibility(View.GONE);
            viewHolder.detial_empty.setVisibility(View.VISIBLE);
            return;
        }

        viewHolder.detial_content.setVisibility(View.VISIBLE);
        viewHolder.detial_empty.setVisibility(View.GONE);

        final RepliesEntity.ReplyEntity entity = datas.get(position);

        String avatar = entity.getAuthor().getAvatar();
        viewHolder.iv.setImage(Utils.getFileLocation(avatar), "50_50", context.getResources().getDrawable(R.drawable.default_profile_img_l));
        viewHolder.tv_name.setText(entity.getAuthor().getUsername());
        viewHolder.tv_time.setText(DateUtil.lifeTime(entity.getCreate_at()));
        viewHolder.tv_content.setText(entity.getContent());
        viewHolder.tv_content.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

        viewHolder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                T8Log.PAN_JIA_FANG.d("click the tv_name : "+entity.getAuthor().getUsername()+" and baseFragment is "+baseFragment);
                TLRPC.User user = MessagesController.getInstance().getUser(Integer.parseInt(entity.getAuthor().getTg_user_id()));
                if (user == null) {
                    if(loadingDialog != null)
                        loadingDialog.show();
                    StrangerUtils.SearchForStranger(entity.getAuthor().getUsername(), new StrangerUtils.SearchStrangerDelegate() {
                        @Override
                        public void getResult(TLRPC.User user) {
                            if(loadingDialog != null)
                                loadingDialog.dismiss();
                            if(user != null){
                                Bundle args = new Bundle();
                                args.putInt("user_id", user.id);
                                baseFragment.presentFragment(new ProfileActivity(args));
                            }
                        }
                    });
                } else {
                    Bundle args = new Bundle();
                    args.putInt("user_id", Integer.parseInt(entity.getAuthor().getTg_user_id()));
                    baseFragment.presentFragment(new ProfileActivity(args));
                }
            }
        });

        viewHolder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                T8Log.PAN_JIA_FANG.d("click the tv_name : "+entity.getAuthor().getUsername()+" and baseFragment is "+baseFragment);
                TLRPC.User user = MessagesController.getInstance().getUser(Integer.parseInt(entity.getAuthor().getTg_user_id()));
                if (user == null) {
                    if(loadingDialog != null)
                        loadingDialog.show();
                    StrangerUtils.SearchForStranger(entity.getAuthor().getUsername(), new StrangerUtils.SearchStrangerDelegate() {
                        @Override
                        public void getResult(TLRPC.User user) {
                            if(loadingDialog != null)
                                loadingDialog.dismiss();
                            if(user != null){
                                Bundle args = new Bundle();
                                args.putInt("user_id", user.id);
                                baseFragment.presentFragment(new ProfileActivity(args));
                            }
                        }
                    });
                } else {
                    Bundle args = new Bundle();
                    args.putInt("user_id", Integer.parseInt(entity.getAuthor().getTg_user_id()));
                    baseFragment.presentFragment(new ProfileActivity(args));
                }
            }
        });
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{

        @InjectView(R.id.header_node_detail_headerview)
        public StreamHeaderView headerView;
        @InjectView(R.id.header_node_detail_forwardview)
        public StreamForwardFullView forwardView;
        @InjectView(R.id.header_node_detail_postview)
        public StreamPostView postView;
        @InjectView(R.id.header_node_detail_footerview)
        public DetailFooterView footerView;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);

            NodeDetailAdapter.this.footerView = footerView;
        }
    }

    public class ContentViewHolder extends RecyclerView.ViewHolder{
        @InjectView(R.id.row_stream_reply_iv)
        public BackupImageView iv;
        @InjectView(R.id.row_stream_reply_name)
        public TextView tv_name;
        @InjectView(R.id.row_stream_reply_content)
        public TextView tv_content;
        @InjectView(R.id.row_stream_reply_time)
        public TextView tv_time;
        @InjectView(R.id.detail_content)
        public RelativeLayout detial_content;
        @InjectView(R.id.detail_empty)
        public RelativeLayout detial_empty;

        public ContentViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);

            iv.setRoundRadius(AndroidUtilities.dp(24));
        }
    }

    public void updateDetailFooter( int score){
        if(footerView != null)
            footerView.setContent(reply, score);
    }

    public void updateDetailFooterReply( int reply){
        this.reply = reply;
    }
}
