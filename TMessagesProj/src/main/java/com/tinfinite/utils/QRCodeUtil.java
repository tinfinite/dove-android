package com.tinfinite.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.ui.fragment.DiscoveryGroupStreamFragment;

import org.apache.http.Header;
import org.telegram.android.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.GroupJoinActivity;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PanJiafang on 15/2/12.
 */
public class QRCodeUtil {

    public static void joinGrpByQRCode(BaseFragment baseFragment, String content){
        joinGrpByQRCode(baseFragment, content, false);
    }

    public static void joinGrpByQRCode(BaseFragment baseFragment, String content, boolean closeLast) {
        HashMap<String, String> map = parseContent(content);
        String chat_name = null;
        int chat_id = 0;
        if (map != null) {
            chat_id = Integer.valueOf(map.get("chat_id"));
            chat_name = map.get("chat_name");
        } else {
            Toast.makeText(baseFragment.getParentActivity(), R.string.ScanFailure, Toast.LENGTH_SHORT).show();
            return;
        }
//        showDialog(context, chat_name, chat_final_id, delegate);
        if(baseFragment != null && chat_id != 0 && chat_name != null) {
            baseFragment.presentFragment(new DiscoveryGroupStreamFragment(DiscoveryGroupStreamFragment.createBundle(chat_id)), closeLast);
        }
    }

    public static void showDialog(Context context, String chat_name, final int chat_final_id){
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_group_join, false)
                .positiveText(R.string.OK)
                .positiveColor(Color.BLACK)
                .negativeText(R.string.Cancel)
                .negativeColor(Color.BLACK)
                .title(LocaleController.getString("JoinGroupDialogTitle", R.string.JoinGroupDialogTitle))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        TLRPC.User user = UserConfig.getCurrentUser();
                        if(user.username == null || user.username.equals("")) {
                            return;
                        }
                        EditText et = (EditText) dialog.getCustomView().findViewById(R.id.dialog_group_join_et);
                        String text = et.getText().toString();
                        ApiRequestHelper.groupApplyToJoinParamsAsync(String.valueOf(user.id), String.valueOf(chat_final_id), user.username, null, text, new ApiRequestHelper.BuildParamsCallBack() {
                            @Override
                            public void build(RequestParams params) {
                                ApiUrlHelper.GROUP_APPLY_TO_JOIN.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(String s) {
                                    }
                                }).execute();
                            }
                        });
                    }
                })
                .build();
        TextView tv = (TextView) dialog.getCustomView().findViewById(R.id.dialog_group_join_tv);
        tv.setText(LocaleController.getString("JoinTheGroup", R.string.JoinTheGroup)+" : "+chat_name+ " ?");
        dialog.show();
    }

    private static HashMap <String, String> parseContent (String src) {
        HashMap <String, String> map = new HashMap<String, String>();
        String patternStr = "http://(.*)\\?chat_id=[-]?(\\d+)&chat_name=(.+)";

        Pattern p = Pattern.compile(patternStr);
        Matcher m = p.matcher(src);
        if (!m.find()) {
            return null;
        } else if (m.groupCount() == 3){
            map.put("chat_id", m.group(2));
            map.put("chat_name", m.group(3));
            return map;
        }

        return null;
    }
}
