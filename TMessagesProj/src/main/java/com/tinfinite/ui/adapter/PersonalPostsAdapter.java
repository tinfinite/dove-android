package com.tinfinite.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.NodeDeleteEntity;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.ReportEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.provider.loader.PostLoader;
import com.tinfinite.ui.fragment.NodeDetailFragment;
import com.tinfinite.ui.views.StreamFooterView;
import com.tinfinite.ui.views.StreamForwardView;
import com.tinfinite.ui.views.StreamHeaderView;
import com.tinfinite.ui.views.StreamPostView;
import com.tinfinite.ui.widget.LoadingDialog;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/4/21.
 */
public class PersonalPostsAdapter extends BaseRecyclerAdapter<NodeEntity> {

    private Context mContext;
    private int chat_id;

    private LoadingDialog loadingDialog;

    public PersonalPostsAdapter(BaseFragment baseFragment, ArrayList datas, boolean empty) {
        super(baseFragment, datas, empty);

        this.mContext = baseFragment.getParentActivity();
        loadingDialog = new LoadingDialog(baseFragment.getParentActivity());
    }

    public PersonalPostsAdapter(BaseFragment baseFragment, ArrayList datas, int chat_id) {
        super(baseFragment, datas);

        this.mContext = baseFragment.getParentActivity();
        this.chat_id = chat_id;
        loadingDialog = new LoadingDialog(baseFragment.getParentActivity());
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stream, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ContentViewHolder contentViewHolder = (ContentViewHolder) viewHolder;

        final NodeEntity entity = datas.get(position);
        final int replyCount = entity.getTotal_reply();
        final int score = entity.getTotal_score();
        final boolean is_upvote = entity.isIs_upvote();
        final boolean is_downvote = entity.isIs_downvote();

        contentViewHolder.headerView.setBaseFragment(baseFragment);

        NodeEntity.ForwardNodeEntity forwardNode = entity.getForward();
        NodeEntity.AuthorEntity author = entity.getAuthor();
        if (forwardNode != null) {
            contentViewHolder.forwardView.setVisibility(View.VISIBLE);
            contentViewHolder.postView.setVisibility(View.GONE);
            contentViewHolder.forwardView.setBaseFragment(baseFragment);
            contentViewHolder.forwardView.setContent(chat_id, forwardNode, author);
            contentViewHolder.forwardView.setBaseFragment(baseFragment);

            contentViewHolder.headerView.setContent(author.getTg_user_id(), author.getUsername(), author.getAvatar(), entity.getCreate_at().getTime(), forwardNode.getThird_group_id(), forwardNode.getThird_group_name(), forwardNode.getThird_group_image_key());
        } else {
            contentViewHolder.forwardView.setVisibility(View.GONE);
        }

        NodeEntity.PostNodeEntity postNodeEntity = entity.getPost();
        if (postNodeEntity != null) {
            contentViewHolder.postView.setVisibility(View.VISIBLE);
            contentViewHolder.forwardView.setVisibility(View.GONE);
            contentViewHolder.postView.setBaseFragment(baseFragment);
            contentViewHolder.postView.setContent(postNodeEntity);
            contentViewHolder.headerView.setContent(author.getTg_user_id(), author.getUsername(), author.getAvatar(), entity.getCreate_at().getTime(), postNodeEntity.getThird_group_id(), postNodeEntity.getThird_group_name(), postNodeEntity.getThird_group_image_key());
        } else {
            contentViewHolder.postView.setVisibility(View.GONE);
        }

        contentViewHolder.footerView.setContent(entity, replyCount, score, is_upvote, is_downvote);
        contentViewHolder.footerView.setViewClickDelegate(new StreamFooterView.ViewClickDelegate() {
            @Override
            public void deleteClick(final NodeEntity nodeEntity) {
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .title(R.string.Delete)
                        .titleColorRes(android.R.color.black)
                        .content(R.string.AreYouSureDeletePost)
                        .contentColorRes(android.R.color.black)
                        .positiveText(R.string.Delete)
                        .positiveColorRes(android.R.color.holo_red_light)
                        .negativeText(R.string.Cancel)
                        .negativeColorRes(android.R.color.black)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);

                                Tracker t = ApplicationLoader.getInstance().getTracker(
                                        ApplicationLoader.TrackerName.APP_TRACKER);
                                // Build and send an Event.
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory("信息流")
                                        .setAction("删除")
                                        .build());

                                if (loadingDialog != null)
                                    loadingDialog.show();

                                ApiRequestHelper.postDelete(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
                                    @Override
                                    public void build(RequestParams params) {
                                        ApiUrlHelper.POST_DELETE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                            @Override
                                            public void onFailure() {
                                                if (loadingDialog != null)
                                                    loadingDialog.dismiss();
                                            }

                                            @Override
                                            public void onSuccess(String responseString) {
                                                if (loadingDialog != null)
                                                    loadingDialog.dismiss();

                                                datas.remove(position);
                                                notifyContentItemRemoved(position);

                                                NodeDeleteEntity deleteEntity = new NodeDeleteEntity();
                                                deleteEntity = deleteEntity.jsonParse(responseString);
                                                if (deleteEntity.getError() == null) {
                                                    PostsController.getInstance().delPost(nodeEntity.getId());
                                                }
                                            }
                                        }, nodeEntity.getId()).execute();
                                    }
                                });
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .build();
                dialog.show();
            }

            @Override
            public void commentClick() {
                Tracker t = ApplicationLoader.getInstance().getTracker(
                        ApplicationLoader.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("信息流")
                        .setAction("评论")
                        .build());
                baseFragment.presentFragment(new NodeDetailFragment(NodeDetailFragment.createBundle(chat_id, entity, replyCount, score, is_upvote, is_downvote)));
            }

            @Override
            public void blockClick(final NodeEntity nodeEntity) {
                Tracker t = ApplicationLoader.getInstance().getTracker(
                        ApplicationLoader.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("信息流")
                        .setAction("举报")
                        .build());
                MaterialDialog materialDialog = new MaterialDialog.Builder(mContext)
                        .title(R.string.Report)
                        .titleColor(Color.BLACK)
                        .items(R.array.report_reason)
                        .itemColor(Color.BLACK)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                if (loadingDialog != null)
                                    loadingDialog.show();
                                report2server(which, nodeEntity);
                            }
                        })
                        .negativeText(R.string.Cancel)
                        .negativeColor(Color.BLACK)
                        .build();
                materialDialog.show();
            }
        });
    }

    public class ContentViewHolder extends RecyclerView.ViewHolder{
        @InjectView(R.id.row_stream_header)
        public StreamHeaderView headerView;

        @InjectView(R.id.row_stream_forwardview)
        public StreamForwardView forwardView;

        @InjectView(R.id.row_stream_post)
        public StreamPostView postView;

        @InjectView(R.id.row_stream_footer)
        public StreamFooterView footerView;

        public ContentViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(itemView, getAdapterPosition());
                }
            });

            ButterKnife.inject(this, itemView);
        }
    }

    private void report2server(int which, NodeEntity node){
        String reason = "";
        switch (which){
            case 0 :
                reason = "垃圾营销";
                break;
            case 1 :
                reason = "淫秽色情";
                break;
            case 2 :
                reason = "虚假信息";
                break;
        }
        ApiRequestHelper.report(String.valueOf(UserConfig.getClientUserId()), node.getId(), reason, ApiRequestHelper.REPORT_TYPE_POST, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.REPORT.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        if(loadingDialog != null)
                            loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String responseString) {
                        if(loadingDialog != null)
                            loadingDialog.dismiss();
                        ReportEntity reportEntity = new ReportEntity();
                        reportEntity = reportEntity.jsonParse(responseString);
                        if (reportEntity.getError() == null) {
                            Toast.makeText(mContext, R.string.ReportSuccessful, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, LocaleController.getString("ReportFailure", R.string.ReportFailure) + ":" + reportEntity.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute();
            }
        });
    }
}
