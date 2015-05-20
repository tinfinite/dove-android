package com.tinfinite.ui.fragment;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.melnykov.fab.FloatingActionButton;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8SharedPreferences;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.ApiResponse2;
import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.ReportEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.provider.loader.PostLoader;
import com.tinfinite.ui.adapter.CursorRecyclerAdapter;
import com.tinfinite.ui.adapter.DiscoveryGroupStreamAdapter;
import com.tinfinite.ui.widget.LoadingDialog;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.SetUsernameActivity;

/**
 * Created by PanJiafang on 15/3/27.
 */
public class DiscoveryGroupStreamFragment extends BaseStreamFragment implements NotificationCenter.NotificationCenterDelegate, LoaderManager.LoaderCallbacks<Cursor>{
    private static final int LOADER_GROUP_POST_ID = 1002;
    private FloatingActionButton floatingButton;
    private DiscoverEntity.Community community;

    private FrameLayout avatarContainer;
    private TextView nameTextView;
    private TextView descView;

    private static final int JOIN_MENU = 1;
    private static final int CHAT_MENU = 2;
    private static final int REPORT_MENU = 3;

    private LoadingDialog loadingDialog;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return PostLoader.constructLoaderForNodeQuery(getParentActivity(), chat_id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        T8Log.ZHAO_ZHEN.d("onLoadFinished count " + data.getCount());

        loadingDialog.dismiss();

        mListAdapter.swapCursor(data);
        onRefreshingStateChanged(false);
        // TO DO load more need optimize, because adapter has notifyDataSetChanged
        // end reached need optimize
        updateLoadMoreState();
        mListAdapter.isLoadCompleted = true;

        if(data.getCount() < PostLoader.PAGE_DISPLAY_COUNT){
            hasNoMoreData();
        }
//        if (data.getCount() == 0) {
//            mEmptyContainer.setVisibility(View.VISIBLE);
//            mEmptyText.setText(LocaleController.getString("GroupStreamNoResult", R.string.GroupStreamNoResult));
//            mEmptyContainer.setBackgroundResource(R.drawable.group_board_empty);
//        } else {
//            mEmptyContainer.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public static Bundle createBundle(int chat_id){
        Bundle bundle = new Bundle();
        bundle.putInt("chat_id", chat_id);
        return bundle;
    }

    public static Bundle createBundle(int chat_id, DiscoverEntity.Community community){
        Bundle bundle = new Bundle();
        bundle.putInt("chat_id", chat_id);
        bundle.putParcelable("community", community);
        return bundle;
    }

    public DiscoveryGroupStreamFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        if(arguments != null) {
            chat_id = arguments.getInt("chat_id", 0);
            community = arguments.getParcelable("community");
        }
        else {
            chat_id = 0;
        }

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.postsEndReached);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.postDidLoaded);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postsEndReached);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postDidLoaded);
        if (getParentActivity() != null) {
            getParentActivity().getLoaderManager().destroyLoader(LOADER_GROUP_POST_ID);
        }
    }

    @Override
    protected void onRefresh() {
        if(community == null)
            getCommunityInfo();
        else {
            loadingDialog.show();
            pageNumber = 0;
            mListAdapter.isLoadCompleted = true;
            PostsController.getInstance().loadPost(chat_id, 0);
        }
    }

    @Override
    public void onLoadMore() {
        pageNumber++;
        T8Log.ZHAO_ZHEN.d( "onLoadMore pageNumber" + pageNumber);
        mListAdapter.isLoadCompleted = true;
        PostsController.getInstance().loadPost(chat_id, pageNumber);
    }

    @Override
    public void init() {
        floatingButton = (FloatingActionButton) fragmentView.findViewById(R.id.floating_button);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker t = ApplicationLoader.getInstance().getTracker(
                        ApplicationLoader.TrackerName.APP_TRACKER);
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("群留言板")
                        .setAction("发布按钮")
                        .build());
                presentFragment(new PostNewStreamActivity(PostNewStreamActivity.createBundle(chat_id)));
            }
        });
        floatingButton.attachToRecyclerView(mNodeListView);

        TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_id);
        if (chat != null && !chat.left) {
//            actionBar.setTitle(MessagesController.getInstance().getChat(chat_id).title);
            floatingButton.setVisibility(View.VISIBLE);
            ActionBarMenu menu = actionBar.createMenu();
            menu.addItem(CHAT_MENU, R.drawable.ic_chat);
        } else {
//            actionBar.setTitle(community.getName());
            ActionBarMenu menu = actionBar.createMenu();
            menu.addItem(REPORT_MENU, R.drawable.ic_report);
            menu.addItem(JOIN_MENU, R.drawable.plus);
            floatingButton.setVisibility(View.GONE);
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1) {
                    finishFragment();
                } else if (id == JOIN_MENU) {
                    final EditText editText = new EditText(getParentActivity());
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = AndroidUtilities.dp(24);
                    params.rightMargin = AndroidUtilities.dp(24);
                    editText.setLayoutParams(params);
                    editText.setGravity(Gravity.LEFT);
                    editText.setHint(LocaleController.getString("", R.string.DialogJoinGroupHint));
                    MaterialDialog dialog = new MaterialDialog.Builder(getParentActivity())
                            .titleColorRes(android.R.color.black)
                            .title(R.string.JoinGroupDialogTitle)
                            .customView(editText, false)
                            .positiveText(R.string.OK)
                            .positiveColorRes(android.R.color.black)
                            .negativeText(R.string.Cancel)
                            .negativeColorRes(android.R.color.black)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    join(editText.getText().toString());
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .build();
                    dialog.show();
                } else if(id == REPORT_MENU){
                    if(community == null)
                        return;
                    MaterialDialog materialDialog = new MaterialDialog.Builder(getParentActivity())
                            .title(R.string.Report)
                            .titleColor(Color.BLACK)
                            .items(R.array.report_reason)
                            .itemColor(Color.BLACK)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                    report(which);
                                }
                            })
                            .negativeText(R.string.Cancel)
                            .negativeColor(Color.BLACK)
                            .build();
                    materialDialog.show();
                } else if(id == CHAT_MENU){
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);

                    Bundle args = new Bundle();
                    args.putInt("chat_id", chat_id);
                    presentFragment(new ChatActivity(args), true);
                }
            }
        });

        avatarContainer = new FrameLayoutFixed(getParentActivity());
        avatarContainer.setBackgroundResource(R.drawable.bar_selector);
        avatarContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
        actionBar.addView(avatarContainer);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) avatarContainer.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        if (chat == null || chat.left)
            layoutParams.rightMargin = AndroidUtilities.dp(96);
        else
            layoutParams.rightMargin = AndroidUtilities.dp(48);
        layoutParams.leftMargin = AndroidUtilities.dp(56);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        avatarContainer.setLayoutParams(layoutParams);

        nameTextView = new TextView(getParentActivity());
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
//        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        avatarContainer.addView(nameTextView);
        layoutParams = (FrameLayout.LayoutParams) nameTextView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.bottomMargin = AndroidUtilities.dp(22);
        layoutParams.gravity = Gravity.BOTTOM;
        nameTextView.setLayoutParams(layoutParams);

        descView = new TextView(getParentActivity());
        descView.setTextColor(0xffd7e8f7);
        descView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        descView.setLines(1);
        descView.setMaxLines(1);
        descView.setSingleLine(true);
        descView.setEllipsize(TextUtils.TruncateAt.END);
        descView.setGravity(Gravity.LEFT);
        avatarContainer.addView(descView);
        layoutParams = (FrameLayout.LayoutParams) descView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.bottomMargin = AndroidUtilities.dp(4);
        layoutParams.gravity = Gravity.BOTTOM;
        descView.setLayoutParams(layoutParams);


        mListAdapter = new DiscoveryGroupStreamAdapter(this, chat_id, community);
        mListAdapter.setOnItemClickListener(new CursorRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                NodeEntity entity = new NodeEntity();
                Cursor cursor = mListAdapter.getCursor();
                if ( cursor == null) {
                    return;
                }
                cursor.moveToPosition(position);
                String s = cursor.getString(PostLoader.PostQuery.POST_JSON_COLUMN);
                final int replyCount = cursor.getInt(PostLoader.PostQuery.POST_REPLY_COUNT_COLUMN);
                final int score = cursor.getInt(PostLoader.PostQuery.POST_SCORE_COLUMN);
                int upvote = cursor.getInt(PostLoader.PostQuery.POST_ISUPVOTE_COLUMN);
                int downvote = cursor.getInt(PostLoader.PostQuery.POST_ISDOWNVOTE_COLUMN);
                final boolean is_upvote = upvote ==  1 ? true : false;
                final boolean is_downvote = downvote == 1 ? true : false;

                entity = entity.jsonParse(s);
                presentFragment(new NodeDetailFragment(NodeDetailFragment.createBundle(chat_id, entity, replyCount, score, is_upvote, is_downvote)));
            }
        });

        boolean firstEnterGroupStream = T8SharedPreferences.getDefault("firstEnterGroupStream", true);
        if (firstEnterGroupStream) {
            MaterialDialog dialog = new MaterialDialog.Builder(getParentActivity())
                    .titleColorRes(android.R.color.black)
                    .title(R.string.GroupStream)
                    .content(R.string.FirstEnterGroupStream)
                    .contentColorRes(android.R.color.black)
                    .positiveText(R.string.OK)
                    .positiveColorRes(android.R.color.black)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                        }
                    })
                    .build();
            dialog.show();
            T8SharedPreferences.setDefault("firstEnterGroupStream", false);
        }

        if(community != null) {
            nameTextView.setText(community.getName());
            descView.setText(community.getDescription());
        }

        //代码监测
        Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
        t.setScreenName("发现-群详情");
        t.send(new HitBuilders.AppViewBuilder().build());

        loadingDialog = new LoadingDialog(getParentActivity());
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.postsEndReached) {
//            updateLoadMoreState();
            mNodeListView.post(new Runnable() {
                @Override
                public void run() {
                    hasNoMoreData();
                }
            });
        } else if (id == NotificationCenter.postDidLoaded) {
            if (getParentActivity() != null) {
                getParentActivity().getLoaderManager().restartLoader(LOADER_GROUP_POST_ID, null, this);
            }
        }
    }

    private void join(String text){
        TLRPC.User user = UserConfig.getCurrentUser();
        if(user.username == null || user.username.equals("")) {
            if(getParentActivity() == null)
                return;
            Toast.makeText(getParentActivity(), R.string.ChooseUserName, Toast.LENGTH_LONG).show();
            presentFragment(new SetUsernameActivity());
            return;
        }
        if(chat_id == 0)
            return;
        if(community == null)
            return;

        // Get tracker.
        Tracker t = ApplicationLoader.getInstance().getTracker(
                ApplicationLoader.TrackerName.APP_TRACKER);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("click")
                .setAction(StringUtils.isEmpty(text) ? "join a group" : "join a group with intro")
                .setLabel(community.getName())
                .build());

        loadingDialog.show();
        ApiRequestHelper.groupApplyToJoinParamsAsync(String.valueOf(user.id), String.valueOf(chat_id), user.username, null, text, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.GROUP_APPLY_TO_JOIN.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String s) {
                        loadingDialog.dismiss();
                        if(getParentActivity() == null)
                            return;
                        Toast.makeText(getParentActivity(), R.string.JoinGroupSuccess, Toast.LENGTH_SHORT).show();
                        finishFragment();
                    }
                }).execute();
            }
        });
    }

    private void report(final int which){
        loadingDialog.show();
        report2server(which);
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
        ApiRequestHelper.reportCommunityParamsAsync(String.valueOf(UserConfig.getClientUserId()), community.getId(), reason, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.REPORT.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        if(getParentActivity() == null)
                            return;
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String responseString) {
                        if(getParentActivity() == null)
                            return;
                        loadingDialog.dismiss();
                        ReportEntity reportEntity = new ReportEntity();
                        reportEntity = reportEntity.jsonParse(responseString);
                        if(reportEntity.getError() == null) {
                            Toast.makeText(getParentActivity(), R.string.ReportSuccessful, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getParentActivity(), LocaleController.getString("ReportFailure", R.string.ReportFailure)+":"+ reportEntity.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute();
            }
        });
    }

    private void getCommunityInfo(){
        loadingDialog.show();

        ApiRequestHelper.communityGetInfoParamsAsync(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.COMMUNITY_GET.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        if (getParentActivity() == null)
                            return;
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String responseString) {
                        loadingDialog.dismiss();
                        if (getParentActivity() == null)
                            return;
                        if (responseString.contains("\"result\":true")) {
                            try {
                                JSONObject object = new JSONObject(responseString);
                                object = object.getJSONObject("community");
                                community = (DiscoverEntity.Community) ApiResponse2.jsonParse(DiscoverEntity.Community.class, object.toString());
                                ((DiscoveryGroupStreamAdapter) mListAdapter).setCommunity(community);

                                nameTextView.setText(community.getName());
                                descView.setText(community.getDescription());

                                pageNumber = 0;
                                mListAdapter.isLoadCompleted = true;
                                PostsController.getInstance().loadPost(chat_id, 0);
                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        } else {
                            T8Toast.lt(LocaleController.getString("", R.string.NetworkResponseError));
                        }
                    }
                }, String.valueOf(chat_id)).execute();
            }
        });
    }
}
