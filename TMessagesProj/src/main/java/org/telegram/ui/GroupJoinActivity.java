package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.entity.ReportEntity;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.Utils;

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
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BackupImageView;

/**
 * Created by PanJiafang on 15/3/10.
 */
public class GroupJoinActivity extends BaseFragment implements View.OnClickListener {

    private static final String CHAT_IMAGE = "chat_image";
    private static final String CHAT_ID = "chat_id";
    private static final String CHAT_NAME = "chat_name";
    private static final String CHAT_DESC = "chat_desc";
    private static final String CHAT_COMMUNITY_ID = "community_id";

    private BackupImageView coreImageView;
    private TextView tv_name;
    private TextView tv_description;
    private EditText et_message;
    private Button btn_join;
    private Button btn_chat;

    private String community_id;
    private int chat_final_id;
    private String chat_name;
    private String chat_desc;
    private String chat_image;

    private LoadingDialog loadingDialog;

    public static Bundle createBundle(DiscoverEntity.Community community){
        Bundle bundle = new Bundle();
        bundle.putInt(CHAT_ID, Math.abs(Integer.parseInt(community.getThird_group_id())));
        bundle.putString(CHAT_NAME, community.getName());
        bundle.putString(CHAT_DESC, community.getDescription());
        bundle.putString(CHAT_IMAGE, community.getThird_group_image_key());
        bundle.putString(CHAT_COMMUNITY_ID, community.getId());
        return bundle;
    }

    public static Bundle createBundle(int chat_id, String name){
        Bundle bundle = new Bundle();
        bundle.putInt(CHAT_ID, Math.abs(chat_id));
        bundle.putString(CHAT_NAME, name);
        return bundle;
    }

    public GroupJoinActivity(Bundle args) {
        super(args);
    }

    @Override
    public View createView(final Context context, LayoutInflater inflater) {
        if(fragmentView == null){
            //代码监测
            Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
            t.setScreenName("加入群组");
            t.send(new HitBuilders.AppViewBuilder().build());

            fragmentView = new LinearLayout(getParentActivity());
            fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
            fragmentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });


            loadingDialog = new LoadingDialog(getParentActivity());


            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("JoinGroupDialogTitle", R.string.JoinGroupDialogTitle));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    } else if(id == 1){
                        //TODO
                        if(getParentActivity() == null)
                            return;
                        MaterialDialog materialDialog = new MaterialDialog.Builder(context)
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
                    }
                }
            });

            ActionBarMenu menu = actionBar.createMenu();
            menu.addItem(1, R.drawable.ic_report);

            View view = inflater.inflate(R.layout.group_join, null);
            tv_name = (TextView) view.findViewById(R.id.item_discover_tv_name);
            tv_description = (TextView) view.findViewById(R.id.item_discover_tv_description);
            et_message = (EditText) view.findViewById(R.id.group_join_et);
            coreImageView = (BackupImageView) view.findViewById(R.id.item_discover_iv);
            coreImageView.setRoundRadius(AndroidUtilities.dp(24));
            btn_chat = (Button) view.findViewById(R.id.group_join_btn_chat);
            btn_join = (Button) view.findViewById(R.id.group_join_btn_join);
            btn_chat.setOnClickListener(this);
            btn_join.setOnClickListener(this);

            if(getArguments() != null) {
                chat_final_id = getArguments().getInt(CHAT_ID);
                chat_name = getArguments().getString(CHAT_NAME);
                chat_desc = getArguments().getString(CHAT_DESC);
                chat_image = getArguments().getString(CHAT_IMAGE);
                community_id = getArguments().getString(CHAT_COMMUNITY_ID);
            }

            TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_final_id);
            if(chat == null || chat.left) {
                btn_join.setVisibility(View.VISIBLE);
                btn_chat.setVisibility(View.GONE);
                et_message.setVisibility(View.VISIBLE);
            } else {
                btn_join.setVisibility(View.GONE);
                btn_chat.setVisibility(View.VISIBLE);
                et_message.setVisibility(View.GONE);
            }

            tv_name.setText(chat_name);

            if(chat_desc != null)
                tv_description.setText(chat_desc);

            coreImageView.setImage(Utils.getFileLocation(chat_image), "50_50", context.getResources().getDrawable(R.drawable.default_profile_img_l));

            ((LinearLayout) fragmentView).addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            if(StringUtils.isEmpty(community_id)){
                loadingDialog.show();

                ApiRequestHelper.communityGetInfoParamsAsync(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
                    @Override
                    public void build(RequestParams params) {
                        ApiUrlHelper.COMMUNITY_GET.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                            @Override
                            public void onFailure() {
                                if(getParentActivity() == null)
                                    return;
                                loadingDialog.dismiss();
                            }

                            @Override
                            public void onSuccess(String responseString) {
                                loadingDialog.dismiss();
                                if(getParentActivity() == null)
                                    return;
                                if(responseString.contains("\"result\":true")) {
                                    try {
                                        JSONObject object = new JSONObject(responseString);
                                        object = object.getJSONObject("community");
                                        community_id = object.getString("id");
                                        if (object.has("description"))
                                            chat_desc = object.getString("description");
                                        tv_description.setText(chat_desc);
                                        if (StringUtils.isEmpty(chat_image) && object.has("third_group_image_key")) {
                                            chat_image = object.getString("third_group_image_key");
                                            coreImageView.setImage(Utils.getFileLocation(chat_image), "50_50", context.getResources().getDrawable(R.drawable.default_profile_img_l));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    }
                                } else {
                                    T8Toast.lt(LocaleController.getString("", R.string.NetworkResponseError));
                                }
                            }
                        }, String.valueOf(chat_final_id)).execute();
                    }
                });
            }

        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }

        return fragmentView;
    }

    private void join(){
        TLRPC.User user = UserConfig.getCurrentUser();
        if(user.username == null || user.username.equals("")) {
            if(getParentActivity() == null)
                return;
            Toast.makeText(getParentActivity(), R.string.ChooseUserName, Toast.LENGTH_LONG).show();
            presentFragment(new SetUsernameActivity());
            return;
        }
        if(chat_final_id == 0)
            return;


        String text = et_message.getText().toString();

        // Get tracker.
        Tracker t = ApplicationLoader.getInstance().getTracker(
                ApplicationLoader.TrackerName.APP_TRACKER);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("click")
                .setAction(StringUtils.isEmpty(text) ? "join a group" : "join a group with intro")
                .setLabel(chat_name)
                .build());

        loadingDialog.show();
        ApiRequestHelper.groupApplyToJoinParamsAsync(String.valueOf(user.id), String.valueOf(chat_final_id), user.username, null, text, new ApiRequestHelper.BuildParamsCallBack() {
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

    private void chat(){
        TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_final_id);

        if(chat == null)
            return;

        // Get tracker.
        Tracker t = ApplicationLoader.getInstance().getTracker(
                ApplicationLoader.TrackerName.APP_TRACKER);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("click")
                .setAction("enter a group")
                .setLabel(chat_name)
                .build());

        Bundle args = new Bundle();
        args.putInt("chat_id", chat.id);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.chatFromPublicPost);

        presentFragment(new ChatActivity(args), true);
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
        ApiRequestHelper.reportCommunityParamsAsync(String.valueOf(UserConfig.getClientUserId()), community_id, reason, new ApiRequestHelper.BuildParamsCallBack() {
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

    @Override
    public void onClick(View v) {
        if(v == btn_chat)
            chat();
        else if(v == btn_join)
            join();
    }
}
