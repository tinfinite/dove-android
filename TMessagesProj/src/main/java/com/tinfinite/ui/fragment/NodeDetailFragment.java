package com.tinfinite.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.NodeDeleteEntity;
import com.tinfinite.entity.NodeDetailEntity;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.RepliesEntity;
import com.tinfinite.entity.ReplyResultEntity;
import com.tinfinite.entity.ReportEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.ui.adapter.NodeDetailAdapter;
import com.tinfinite.ui.views.SimpleInputView;
import com.tinfinite.ui.views.StreamFooterView;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.AvatarUpdateUtils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by PanJiafang on 15/3/31.
 */
public class NodeDetailFragment extends BaseRecycleViewFragment<RepliesEntity.ReplyEntity> implements NotificationCenter.NotificationCenterDelegate{

    private ArrayList<RepliesEntity.ReplyEntity> datas;

    private NodeEntity nodeEntity;
    private int chat_id;
    private int reply;
    private int score;
    private boolean is_upVote;
    private boolean is_downVote;
    private String post_id;

    private SimpleInputView simpleInputView;
    private StreamFooterView footerView;

    private LoadingDialog loadingDialog;

    public static Bundle createBundle(int chat_id, NodeEntity entity, int reply, int score, boolean upvote, boolean downvote){
        Bundle bundle = new Bundle();
        bundle.putInt("chat_id", chat_id);
        bundle.putParcelable("node", entity);
        bundle.putInt("reply", reply);
        bundle.putInt("score", score);
        bundle.putBoolean("upvote", upvote);
        bundle.putBoolean("downvote", downvote);
        return bundle;
    }

    public static Bundle createBundle(String post_id){
        Bundle bundle = new Bundle();
        bundle.putString("post_id", post_id);
        return bundle;
    }

    public NodeDetailFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.postDidupdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatFromPublicPost);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postDidupdated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatFromPublicPost);
    }

    @Override
    protected void onRefresh() {
        pageNumber = 0;
        pageTimestamp = 0;

        if(!StringUtils.isEmpty(post_id)){
            getNodeDetail();
        } else {
            initView();
            getData();
        }
    }

    @Override
    public void onLoadMore() {
        getData();
    }

    @Override
    public void init() {
        datas = new ArrayList<>();

        loadingDialog = new LoadingDialog(getParentActivity());

        if(getArguments() != null) {
            nodeEntity = getArguments().getParcelable("node");
            reply = getArguments().getInt("reply");
            score = getArguments().getInt("score");
            is_upVote = getArguments().getBoolean("upvote");
            is_downVote = getArguments().getBoolean("downvote");
            chat_id = getArguments().getInt("chat_id");

            post_id = getArguments().getString("post_id");
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("StreamDetailTitle", R.string.StreamDetailTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        //代码监测
        Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
        t.setScreenName("流详情");
        t.send(new HitBuilders.AppViewBuilder().build());

        if (getParentActivity() != null) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        mListAdapter = new NodeDetailAdapter(this, chat_id, datas, nodeEntity, reply, score);

    }

    private void initView(){
        simpleInputView = new SimpleInputView(getParentActivity());
        simpleInputView.setPostID(nodeEntity.getId());

        footerView = new StreamFooterView(getParentActivity());
        footerView.setContent(nodeEntity, reply, score, is_upVote, is_downVote);
        footerView.dismissDivider();
        footerView.canSendNotify(true);

        simpleInputView.setVisibility(View.GONE);
        footerContainer.setVisibility(View.VISIBLE);
        footerContainer.addView(simpleInputView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footerContainer.addView(footerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        simpleInputView.setSendButtonDelegate(new SimpleInputView.SendButtonDelegate() {
            @Override
            public void sendContent(final String content) {
                loadingDialog.show();
                ApiRequestHelper.postReply(String.valueOf(UserConfig.getClientUserId()), nodeEntity.getId(), content, String.valueOf(chat_id), new ApiRequestHelper.BuildParamsCallBack() {
                    @Override
                    public void build(RequestParams params) {
                        ApiUrlHelper.POST_REPLY_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                            @Override
                            public void onFailure() {
                                loadingDialog.dismiss();
                            }

                            @Override
                            public void onSuccess(String responseString) {
                                loadingDialog.dismiss();
                                reply += 1;

                                ReplyResultEntity resultEntity = new ReplyResultEntity();
                                resultEntity = resultEntity.jsonParse(responseString);

                                if(resultEntity.getError() == null) {
                                    simpleInputView.clearText();

                                    RepliesEntity.ReplyAuthorEntity entity = new RepliesEntity.ReplyAuthorEntity();
                                    entity.setId(T8UserConfig.getUserId());

                                    TLRPC.User user = UserConfig.getCurrentUser();
                                    entity.setFirst_name(user.first_name);
                                    entity.setLast_name(user.last_name);
                                    entity.setUsername(user.username);
                                    entity.setAvatar(AvatarUpdateUtils.getUserImageFileAndUpload(true));

                                    RepliesEntity.ReplyEntity replyEntity = new RepliesEntity.ReplyEntity();
                                    replyEntity.setAuthor(entity);
                                    replyEntity.setContent(content);
                                    replyEntity.setCreate_at(new Date());
                                    replyEntity.setId(resultEntity.getId());

                                    nodeEntity.setTotal_reply(nodeEntity.getTotal_reply() + 1);
                                    footerView.setContent(nodeEntity, reply, score, is_upVote, is_downVote);

                                    ((NodeDetailAdapter)mListAdapter).updateDetailFooterReply(reply);
                                    PostsController.getInstance().updateReplyCount(nodeEntity.getId(), nodeEntity.getTotal_reply());
                                    mListAdapter.insertItemDataHeading(replyEntity);

                                    footerView.setVisibility(View.VISIBLE);
                                    simpleInputView.setVisibility(View.GONE);
                                    simpleInputView.clearTheFocus();
                                } else {
                                    T8Toast.lt(resultEntity.getError().getMessage());
                                }
                            }
                        }).execute();
                    }
                });
            }
        });

        footerView.setViewClickDelegate(new StreamFooterView.ViewClickDelegate() {
            @Override
            public void deleteClick(final NodeEntity nodeEntity) {
                MaterialDialog dialog = new MaterialDialog.Builder(getParentActivity())
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
                                loadingDialog.show();
                                ApiRequestHelper.postDelete(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
                                    @Override
                                    public void build(RequestParams params) {
                                        ApiUrlHelper.POST_DELETE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                            @Override
                                            public void onFailure() {
                                                loadingDialog.dismiss();
                                            }

                                            @Override
                                            public void onSuccess(String responseString) {
                                                loadingDialog.dismiss();
                                                NodeDeleteEntity deleteEntity = new NodeDeleteEntity();
                                                deleteEntity = deleteEntity.jsonParse(responseString);
                                                if (deleteEntity.getError() == null) {
                                                    PostsController.getInstance().delPost(nodeEntity.getId());
                                                    finishFragment();
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
                footerView.setVisibility(View.GONE);
                simpleInputView.setVisibility(View.VISIBLE);
                simpleInputView.getTheFocus();
            }

            @Override
            public void blockClick(NodeEntity nodeEntity) {
                MaterialDialog materialDialog = new MaterialDialog.Builder(getParentActivity())
                        .title(R.string.Report)
                        .titleColor(Color.BLACK)
                        .items(R.array.report_reason)
                        .itemColor(Color.BLACK)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                report2server(which);
                            }
                        })
                        .negativeText(R.string.Cancel)
                        .negativeColor(Color.BLACK)
                        .build();
                materialDialog.show();
            }
        });
    }

    private void getData(){
        ApiRequestHelper.postReplyQuery(String.valueOf(UserConfig.getClientUserId()), ++pageNumber, pageCount, pageTimestamp, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.POST_REPLY_QUERY.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        RepliesEntity entity = new RepliesEntity();
                        entity = entity.jsonParse(responseString);
                        if(entity.getError() == null) {
                            pageNumber = entity.getPage();
                            pageTimestamp = entity.getTimestamp();

                            ArrayList<RepliesEntity.ReplyEntity> temp = entity.getData();

                            if (pageNumber > 1) {
                                appendItemsData(temp);
                            } else {
                                refreshNewItemsData(temp);
                            }

                            if (temp == null || temp.size() < pageCount) {
                                hasNoMoreData();
                            }
                        }
                    }
                }, nodeEntity.getId()).execute();
            }
        });
    }

    private void getNodeDetail(){
        ApiRequestHelper.postDetail(String.valueOf(UserConfig.getClientUserId()), 1, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                T8Log.PAN_JIA_FANG.d("adf");
                ApiUrlHelper.POST_DETAIL.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        NodeDetailEntity detailEntity = new NodeDetailEntity();
                        detailEntity = detailEntity.jsonParse(responseString);
                        if(detailEntity.getError() == null){
                            nodeEntity = detailEntity.getData();

                            if(nodeEntity == null){
                                T8Toast.lt(LocaleController.getString("", R.string.NodeNotExsits));
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finishFragment();
                                    }
                                }, 600);
                            } else {
                                chat_id = 0;
                                reply = nodeEntity.getTotal_reply();
                                score = nodeEntity.getTotal_score();

                                ((NodeDetailAdapter) mListAdapter).refreshData(chat_id, nodeEntity, reply, score);

                                initView();
                                getData();
                            }
                        }
                    }

                    @Override
                    public void onFailure() {
                        super.onFailure();

                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                finishFragment();
                            }
                        }, 600);
                    }
                }, post_id).execute();
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if(simpleInputView != null && simpleInputView.getVisibility() == View.VISIBLE){
            simpleInputView.setVisibility(View.GONE);
            footerView.setVisibility(View.VISIBLE);
            return false;
        }
        return super.onBackPressed();
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if(id == NotificationCenter.postDidupdated){
            if (args != null  ) {
                int score = (int) args[0];
                ((NodeDetailAdapter)mListAdapter).updateDetailFooter(score);
            }
        } else if (id == NotificationCenter.chatFromPublicPost) {
            removeSelfFromStack();
        }
    }

    private void report2server(int which){
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
        loadingDialog.show();
        ApiRequestHelper.report(String.valueOf(UserConfig.getClientUserId()), nodeEntity.getId(), reason, ApiRequestHelper.REPORT_TYPE_POST, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.REPORT.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String responseString) {
                        loadingDialog.dismiss();
                        ReportEntity reportEntity = new ReportEntity();
                        reportEntity = reportEntity.jsonParse(responseString);
                        if (reportEntity.getError() == null) {
                            Toast.makeText(getParentActivity(), R.string.ReportSuccessful, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getParentActivity(), LocaleController.getString("ReportFailure", R.string.ReportFailure) + ":" + reportEntity.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute();
            }
        });
    }
}
