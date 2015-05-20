package com.tinfinite.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.api.BotRequest;
import com.tinfinite.entity.UnReadMessageEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.ui.WebviewActivity;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.ContactsController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by PanJiafang on 15/3/23.
 */
public class Utils {
//    private static final String channel = "google play";
    private static final String channel = getMetaData("DOVE_CHANNEL");
//    private static final String channel = "xiaomi";
//    private static final String channel = "360";
//    private static final String channel = "fir";

    //绑定User
    public static void bindUser(final Context context) {
        TLRPC.User user = UserConfig.getCurrentUser();
        if (user != null) {
            String first_name = user.first_name;
            String last_name = user.last_name;
            String username = user.username;
            String userAvatar = AvatarUpdateUtils.getUserImageFileAndUpload(true);

            String userSavedAvatar = AvatarUpdateUtils.getUserSavedImage();
            if(userSavedAvatar == null || (userAvatar != null && !userSavedAvatar.equals(userAvatar))){
                if(!T8UserConfig.getUserId().equals(""))
                    AvatarUpdateUtils.getUserImageFileAndUpload(false);
            }

            ApiRequestHelper.bindUserParamsAsync(UserConfig.getPhoneNumberE164(), String.valueOf(UserConfig.getClientUserId()), first_name, last_name, username, channel, new ApiRequestHelper.BuildParamsCallBack() {
                @Override
                public void build(RequestParams params) {
                    ApiUrlHelper.USER_BIND.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                        @Override
                        public void onSuccess(String responseString) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseString);
                                if (jsonObject.has("tinfinite_user_id")) {
                                    String userId = jsonObject.getString("tinfinite_user_id");
                                    if (!userId.isEmpty()) {
                                        T8UserConfig.saveUserId(userId);

                                        if(context != null)
                                            JPushInterface.setAlias(context, T8UserConfig.getUserId(), null);

                                        updateUserInfo();

                                        updateUserGroupInfo();

                                        getUnreadMessage();
                                    }
                                }
                                if (jsonObject.has("newer")) {
                                    boolean result = jsonObject.getBoolean("newer");
                                    if (result) {
                                        BotRequest.buildDot(new ApiHttpClient.DoveHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(String s) {
                                                try {
                                                    JSONObject object = new JSONObject(s);
                                                    String first_name = object.getString("first_name");
                                                    String last_name = object.getString("last_name");
                                                    String phone = object.getString("phone");

                                                    ContactsController.getInstance().addContact(phone, first_name, last_name);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }

                                if (jsonObject.has("user_sync_info")) {
                                    JSONObject syncJson = jsonObject.getJSONObject("user_sync_info");
                                    if (syncJson.has("group_top_info")) {
                                        String top_dialogs = syncJson.getString("group_top_info");
                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("topdialog", Activity.MODE_PRIVATE);
                                        String originDialogs = preferences.getString("stick_to_top_dialogs", "");

                                        if (top_dialogs != null && !top_dialogs.equals(originDialogs)) {
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("stick_to_top_dialogs", top_dialogs);
                                            editor.commit();
                                            MessagesController.getInstance().stickDialogToTop(top_dialogs);
                                            MessagesController.getInstance().resortDialogsWithStickToTop();
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.topDialogsNeedReload);
                                        }
                                    }

                                    if(syncJson.has("is_notify_comment")) {
                                        int value = syncJson.getInt("is_notify_comment");
                                        PreferenceUtils.setNewCommentNotification(value == 1);
                                    }

                                    if(syncJson.has("is_notify_upvote")) {
                                        int value = syncJson.getInt("is_notify_upvote");
                                        PreferenceUtils.setNewVoteNotification(value == 1);
                                    }
                                    if(syncJson.has("is_notify_group_apply")) {
                                        int value = syncJson.getInt("is_notify_group_apply");
                                        PreferenceUtils.setNewVoteNotification(value == 1);
                                    }

                                    if (syncJson.has("block_users")) {
                                        JSONArray data = (JSONArray) syncJson.get("block_users");
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject object = (JSONObject) data.get(i);
                                            PostsController.getInstance().addBlockUser((String) object.get("tg_user_id"));
                                        }
                                    }

                                    if(syncJson.has("tg_group_anonymous_status")){
                                        int status = syncJson.getInt("tg_group_anonymous_status");
                                        if(status == 2){
                                            if(context != null) {
                                                MaterialDialog dialog = new MaterialDialog.Builder(context)
                                                        .title(R.string.PublicStream)
                                                        .titleColorRes(android.R.color.black)
                                                        .content(R.string.StreamFirstTip)
                                                        .contentColorRes(android.R.color.black)
                                                        .cancelable(false)
                                                        .positiveText(R.string.OK)
                                                        .positiveColorRes(android.R.color.black)
                                                        .callback(new MaterialDialog.ButtonCallback() {
                                                            @Override
                                                            public void onPositive(MaterialDialog dialog) {
                                                                super.onPositive(dialog);
                                                                ApiRequestHelper.anonymousGroupSync(String.valueOf(UserConfig.getClientUserId()), 1, new ApiRequestHelper.BuildParamsCallBack() {
                                                                    @Override
                                                                    public void build(RequestParams params) {
                                                                        ApiUrlHelper.ANONYMOUS_GROUP_SYNC.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                                                            @Override
                                                                            public void onSuccess(String responseString) {
                                                                                if(responseString.contains("result:\"true\"")){
                                                                                    PreferenceUtils.setGlobleStatus(true);
                                                                                }
                                                                            }
                                                                        }, T8UserConfig.getUserId()).execute();
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .build();
                                                dialog.setCanceledOnTouchOutside(false);
                                                dialog.show();
                                            }
                                        } else {
                                            PreferenceUtils.setGlobleStatus(status == 1);
                                        }
                                    }

                                }

                                if(jsonObject.has("build_info") && context != null){
                                    jsonObject = jsonObject.getJSONObject("build_info");
                                    if(jsonObject.has("android")){
                                        jsonObject = jsonObject.getJSONObject("android");
                                        final String version = String.valueOf(getAppVersion());
                                        if(jsonObject.has(version) && !StringUtils.isEmpty(version) && !version.equals(PreferenceUtils.getUpdateStatus())){
                                            jsonObject = jsonObject.getJSONObject(version);
                                            final String url = jsonObject.getString("link");
                                            final String content = jsonObject.getString("content");
                                            String title = jsonObject.getString("title");

                                            MaterialDialog dialog = new MaterialDialog.Builder(context)
                                                    .title(StringUtils.isEmpty(title) ? "" : title)
                                                    .titleColorRes(android.R.color.black)
                                                    .content(content)
                                                    .contentColorRes(android.R.color.black)
                                                    .cancelable(false)
                                                    .positiveText(R.string.Update)
                                                    .positiveColorRes(android.R.color.black)
                                                    .negativeText(R.string.Cancel)
                                                    .negativeColorRes(android.R.color.black)
                                                    .callback(new MaterialDialog.ButtonCallback() {
                                                        @Override
                                                        public void onPositive(MaterialDialog dialog) {
                                                            super.onPositive(dialog);
                                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                                            intent.setData(Uri.parse(url));
                                                            context.startActivity(intent);

                                                            PreferenceUtils.setUpdateStatus(version);
                                                        }
                                                    })
                                                    .build();
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.show();
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).execute();
                }
            });
        }
    }

    private static void updateUserInfo(){
        String model = Build.MODEL;
        String os_version = String.valueOf(Build.VERSION.SDK_INT);
        String app_version = "";
        PackageManager packageManager = ApplicationLoader.getInstance().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(ApplicationLoader.getInstance().getPackageName(), 0);
            app_version = packageInfo.versionName;

            ApiRequestHelper.userLastestInfoUpdate(String.valueOf(UserConfig.getClientUserId()), model, os_version, app_version, new ApiRequestHelper.BuildParamsCallBack() {
                @Override
                public void build(RequestParams params) {
                    ApiUrlHelper.USER_LASTEST_INFO_UPDATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                        @Override
                        public void onSuccess(String responseString) {
                        }
                    }, T8UserConfig.getUserId()).execute();
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void updateUserGroupInfo(){
        ConcurrentHashMap<Integer, TLRPC.Chat> chats = MessagesController.getInstance().getChats();
        if(chats != null){
            JSONArray array = new JSONArray();
                try {
                    JSONObject object = new JSONObject();
                    for (Map.Entry<Integer, TLRPC.Chat> entry : chats.entrySet()) {
                        object = new JSONObject();
                        object.put("id", entry.getKey());
                        object.put("name", entry.getValue().title);
                        object.put("image_key", AvatarUpdateUtils.getGroupImageFilePath(entry.getValue().id));
                        array.put(object);
                    }
                    if(chats.size() > 0)
                        ApiRequestHelper.communityUserSync(String.valueOf(UserConfig.getClientUserId()), array.toString(), new ApiRequestHelper.BuildParamsCallBack() {
                            @Override
                            public void build(RequestParams params) {
                                ApiUrlHelper.COMMUNITY_USER_SYNC.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(String responseString) {
                                    }
                                }).execute();
                            }
                        });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    private static void getUnreadMessage(){
        ApiRequestHelper.userUnReadMessage(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.USER_UNREADMESSAGE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        UnReadMessageEntity entity = new UnReadMessageEntity();
                        entity = entity.jsonParse(responseString);
                        if(entity.getError() == null){
                            int comments = entity.getData().getNew_comments_count();
                            int votes = entity.getData().getNew_upvotes_count();

                            PreferenceUtils.setUnReadComments(comments);
                            PreferenceUtils.setUnReadVotes(votes);
                        }
                    }
                }, T8UserConfig.getUserId()).execute();
            }
        });
    }

    public static String getImageFile(TLRPC.FileLocation imageLocation) {
        if(imageLocation == null)
            return null;
        String key = imageLocation.volume_id + "_" + imageLocation.local_id;
        String ext = "." + (imageLocation.ext != null ? imageLocation.ext : "jpg");
        String url = key + ext;

        File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), url);

        if (cacheFile.exists()) {
            return cacheFile.getAbsolutePath();
        }
        return null;
    }


    public static Drawable processDrawable(int drawableId, int colorId) {
        int color = ApplicationLoader.getInstance().getResources().getColor(colorId);

        Drawable result = ApplicationLoader.getInstance().getResources().getDrawable(drawableId);
        result.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return result;
    }

    public static Drawable processDrawable(Drawable drawable, int colorId) {
        int color = ApplicationLoader.getInstance().getResources().getColor(colorId);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return drawable;
    }

    public static int getImageWidthFromUrl(String image_url){
        if(image_url.contains(".gif"))
            image_url = image_url.replace(".gif", "");
        int start = image_url.lastIndexOf("w");
        int end = image_url.lastIndexOf("h");
        if(start == -1 || end == -1)
            return 0;
        String width = image_url.substring(start+1, end);
//        T8Log.PAN_JIA_FANG.d(width);
        if(width.matches("^[0-9]*$"))
            return Integer.parseInt(width);
        return 0;
    }

    public static int getImageHeightFromUrl(String image_url){
        if(image_url.contains(".gif"))
            image_url = image_url.replace(".gif", "");
        int start = image_url.lastIndexOf("h");
        if(start == -1)
            return 0;
        String width = image_url.substring(start+1, image_url.length());
//        T8Log.PAN_JIA_FANG.d(width);
        if(width.matches("^[0-9]*$"))
            return Integer.parseInt(width);
        return 0;
    }

    public static String getUrlFromText(String url){
        if(StringUtils.isEmpty(url))
            return null;
        if(!url.contains("http"))
            return null;
        String text = "";
        text = url.substring(url.indexOf("http"));
        int end = text.indexOf(" ");
        if(end != -1)
            text = url.subSequence(0, end).toString();
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$" ;
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(text);
        if(matcher.matches()){
            return text;
        }
        return null;
    }

    public static TLRPC.TL_fileLocation getFileLocation(String key){
        TLRPC.TL_fileLocation location = new TLRPC.TL_fileLocation();
        if(!StringUtils.isEmpty(key)){
            String[] keys = key.split("_");
            if(keys.length == 4){
                location.dc_id = Integer.parseInt(keys[0]);
                location.volume_id = Integer.parseInt(keys[1]);
                location.local_id = Integer.parseInt(keys[2]);
                location.secret = Long.parseLong(keys[3]);
            }
        }
        return location;
    }

    public static int getAppVersion(){
        int version = 0;
        version = ApplicationLoader.getAppVersion();
        return version;
    }

    public static String getMetaData(String key){
        String result = "";
        try {
            ApplicationInfo info = ApplicationLoader.getInstance().getPackageManager().getApplicationInfo(ApplicationLoader.getInstance().getPackageName(), PackageManager.GET_META_DATA);
            result = info.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 点击链接，在应用内打开
     * @param view
     * @param url
     */
    public static void clickUrl(View view, String url){
        T8Log.PAN_JIA_FANG.d("click url");
        Context context = view.getContext();
        Intent intent = null;
        if(url.contains("telegram.me") && url.contains("joinchat") || url.contains("tg:")) {
            intent = new Intent(context, LaunchActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        } else {
            intent = new Intent(context, WebviewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(WebviewActivity.createBundle(url));
        }
        context.startActivity(intent);
    }

    public static void copyFile(final File oldfile, final String newPath) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    int bytesum = 0;
                    int byteread = 0;
                    File newFile = new File(newPath);
                    if(newFile.exists() && newFile.getTotalSpace() == oldfile.getTotalSpace() && newFile.getUsableSpace() == oldfile.getUsableSpace())
                        return;
                    if (oldfile.exists()) {
                        InputStream inStream = new FileInputStream(oldfile);
                        FileOutputStream fs = new FileOutputStream(newPath);
                        byte[] buffer = new byte[1444];
                        while ((byteread = inStream.read(buffer)) != -1) {
                            bytesum += byteread;
                            System.out.println(bytesum);
                            fs.write(buffer, 0, byteread);
                        }
                        inStream.close();
                    }
                } catch (Exception e) {
                    System.out.println("error  ");
                    e.printStackTrace();
                }
            }
        }.start();
    }
}