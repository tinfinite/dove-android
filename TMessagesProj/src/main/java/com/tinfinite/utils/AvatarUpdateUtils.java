/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tinfinite.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.android.sdk.utils.EncryptUtil;
import com.tinfinite.cache.DiskCacheUtil;
import com.tinfinite.entity.ApiResponse2;
import com.tinfinite.entity.DiscoverEntity;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.MessagesController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;

import java.io.File;

public class AvatarUpdateUtils {

    public static final String DIRC_NAME = "tinfinite";
    private static final String QINIU_URL = "http://tinfinite.qiniudn.com/";

    //头像更新间隔
    private static long TIME = 3600 * 1000;


    public static String getFilePath(Context context, String avatar_url) {
        File file = DiskCacheUtil.getDiskCacheDir(context, DIRC_NAME);
        String filename = DiskCacheUtil.hashKeyForDisk(avatar_url);
        if (file.isDirectory()) {
            T8Log.PAN_JIA_FANG.i("file is directory");
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                file = files[i];
                if (file.getName().contains(filename))
                    return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * 获取本地保存的用户图像
     * @return
     */
    public static String getUserSavedImage(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        return preferences.getString("avatar", null);
    }

    /**
     * 保存用户图像
     * @param path
     */
    private static void saveUserImage(String path){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("avatar", path);
        editor.commit();
    }

    /**
     * 通过Location获取本地路径
     * @param imageLocation
     * @return
     */
    public static String getImageFile(TLRPC.FileLocation imageLocation) {
        if (imageLocation == null)
            return null;
//        旧机制
//        String key = imageLocation.volume_id + "_" + imageLocation.local_id;
//        String ext = "." + (imageLocation.ext != null ? imageLocation.ext : "jpg");
//        String url = key + ext;
//
//        File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), url);
//
//        if (cacheFile.exists()) {
//            T8Log.PAN_JIA_FANG.d("准备到图像：" + cacheFile.getAbsolutePath());
//            return cacheFile.getAbsolutePath();
//        }
//        return null;

        //新机制
        String path = imageLocation.dc_id+"_"+imageLocation.volume_id+"_"+imageLocation.local_id+"_"+imageLocation.secret;
        T8Log.PAN_JIA_FANG.d("图片地址："+path);
        return path;
    }

    /**
     * 通过ID获取群头像本地路径
     * @param id
     */
    public static String getGroupImageFilePath(int id){
        T8Log.PAN_JIA_FANG.d("准备获取图像");
        TLRPC.Chat chat = MessagesController.getInstance().getChat(id);
        if(chat.photo == null)
            return null;
        TLRPC.FileLocation fileLocation = chat.photo.photo_small;
        String path = getImageFile(fileLocation);
        if (path == null) {
            fileLocation = chat.photo.photo_big;
            path = getImageFile(fileLocation);
        }
        if (path != null) {
            return path;
        }
        return null;
    }

    /**
     * 通过ID获取群头像本地路径并上传
     * @param id
     */
    public static void getGroupImageFileAndUpload(int id, TLRPC.ChatParticipants info){
        T8Log.PAN_JIA_FANG.d("准备获取图像");
        String path = getGroupImageFilePath(id);
        if (path != null) {
            syncGroupWithServer(id, path, info);
        }
    }

    /**
     * 获取用户头像本地路径并上传
     * @param onlyGetPath 只获取本地路径
     */
    public static String getUserImageFileAndUpload(boolean onlyGetPath){
        T8Log.PAN_JIA_FANG.d("准备获取图像");
        TLRPC.User user = UserConfig.getCurrentUser();
        TLRPC.FileLocation fileLocation = user.photo.photo_small;
        String path = getImageFile(fileLocation);
        if (path == null) {
            fileLocation = user.photo.photo_big;
            path = getImageFile(fileLocation);
        }
        if (path != null) {
            if(onlyGetPath)
                return path;
//            uploadUserAvatar(path);
            syncUserWithServer(path);
        }
        return null;
    }

    /**
     * 用户更新群组头像
     * @param chat
     */
    public static void userUpdateGroupAvatar(final TLRPC.Chat chat){
        T8Log.PAN_JIA_FANG.d("用户更新群组头像");
        final int chat_id = chat.id;

        ApiRequestHelper.communityGetInfoParamsAsync(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(final RequestParams params) {
                ApiUrlHelper.COMMUNITY_GET.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        if (responseString.contains("\"result\":true")) {
                            try {
                                JSONObject object = new JSONObject(responseString);
                                object = object.getJSONObject("community");
                                String creator_id = "";
                                if (object.has("creator_id")) {
                                    creator_id = object.getString("creator_id");
                                }
                                if(creator_id.equals(""))
                                    return;
                                String path = getImageFile(chat.photo.photo_small);
                                if(path == null)
                                    path = getImageFile(chat.photo.photo_big);
                                if(path == null)
                                    return;
                                T8Log.PAN_JIA_FANG.d("准备更新群组头像");
                                ApiRequestHelper.communityUpdateAvatar(String.valueOf(UserConfig.getClientUserId()), String.valueOf(chat_id), creator_id, path, new ApiRequestHelper.BuildParamsCallBack() {
                                    @Override
                                    public void build(RequestParams params) {
                                        ApiUrlHelper.COMMUNITY_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
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
                }, String.valueOf(chat_id)).execute();
            }
        });
    }

//    /**
//     * 上传群组图像
//     * @param path
//     * @param chat_id
//     */
//    private static void uploadGroupAvatar(final String path, final String chat_id) {
//        ApiRequestHelper.getQNUpdateToken(String.valueOf(UserConfig.getClientUserId()), EncryptUtil.generateGroupAvatarPicName(chat_id), new ApiRequestHelper.BuildParamsCallBack() {
//            @Override
//            public void build(RequestParams params) {
//                ApiUrlHelper.GRAPH_QNUPDATE.build(params, new TextHttpResponseHandler() {
//                    @Override
//                    public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
//                        T8Log.PAN_JIA_FANG.d(s);
//                        RequestUtils.checkError(throwable);
//                    }
//
//                    @Override
//                    public void onSuccess(int i, Header[] headers, String s) {
//                        T8Log.PAN_JIA_FANG.d(s);
//                        try {
//                            JSONObject rootJson = new JSONObject(s);
//                            if (rootJson.has("qnUpdateToken")) {
//                                String qnToken = rootJson.getString("qnUpdateToken");
//
//                                UploadManager uploadManager = new UploadManager();
//
//                                T8Log.PAN_JIA_FANG.d("上传图像");
//                                String qiniuFile = EncryptUtil.generateGroupAvatarPicName(chat_id);
//                                uploadManager.put(path, qiniuFile, qnToken, new UpCompletionHandler() {
//                                    @Override
//                                    public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
//                                        if (jsonObject != null) {
//                                            if (responseInfo.error == null) {
//                                                syncGroupWithServer(chat_id);
//                                            }
//                                        }
//                                    }
//                                }, null);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).execute();
//            }
//        });
//    }

//    /**
//     * 上传用户图像
//     * @param path
//     */
//    private static void uploadUserAvatar(final String path) {
//        final String userid = String.valueOf(UserConfig.getClientUserId());
//        final String qiniuFile = EncryptUtil.generate9CellPicName(userid);
//        ApiRequestHelper.getQNUpdateToken(userid, qiniuFile, new ApiRequestHelper.BuildParamsCallBack() {
//            @Override
//            public void build(RequestParams params) {
//                ApiUrlHelper.GRAPH_QNUPDATE.build(params, new TextHttpResponseHandler() {
//                    @Override
//                    public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
//                        T8Log.PAN_JIA_FANG.d(s);
//                    }
//
//                    @Override
//                    public void onSuccess(int i, Header[] headers, String s) {
//                        T8Log.PAN_JIA_FANG.d(s);
//                        try {
//                            JSONObject rootJson = new JSONObject(s);
//                            if (rootJson.has("qnUpdateToken")) {
//                                String qnToken = rootJson.getString("qnUpdateToken");
//
//                                UploadManager uploadManager = new UploadManager();
//
//                                T8Log.PAN_JIA_FANG.d("上传图像");
//                                uploadManager.put(path, qiniuFile, qnToken, new UpCompletionHandler() {
//                                    @Override
//                                    public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
//                                        if (jsonObject != null) {
//                                            if (responseInfo.error == null) {
//                                                syncUserWithServer(QINIU_URL+s);
//                                            }
//                                        }
//                                    }
//                                }, null);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).execute();
//            }
//        });
//    }

    /**
     * 通知服务器群头像更新
     * @param chat_id
     */
    private static void syncGroupWithServer(int chat_id, String path, TLRPC.ChatParticipants info){
//        旧机制
//        TLRPC.Chat chat = MessagesController.getInstance().getChat(Integer.parseInt(chat_id));
//        if(chat != null){
//            String chat_name = chat.title;
//            ApiRequestHelper.communityImageSync(String.valueOf(UserConfig.getClientUserId()), chat_name, new ApiRequestHelper.BuildParamsCallBack() {
//                @Override
//                public void build(RequestParams params) {
//                    ApiUrlHelper.COMMUNITY_IMAGE_UPDATE.build(params, new TextHttpResponseHandler() {
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                            T8Log.PAN_JIA_FANG.d("COMMUNITY_IMAGE_UPDATE Error:" + responseString);
//                            RequestUtils.checkError(throwable);
//                        }
//
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                            T8Log.PAN_JIA_FANG.d("COMMUNITY_IMAGE_UPDATE :" + responseString);
//                        }
//                    }, chat_id).execute();
//                }
//            });
//        }
        if(info == null)
            return;
        ApiRequestHelper.communityUpdateAvatar(String.valueOf(UserConfig.getClientUserId()), String.valueOf(chat_id), String.valueOf(info.admin_id),  path, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.COMMUNITY_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                    }
                }).execute();
            }
        });

    }

    private static void syncUserWithServer(final String url){
        ApiRequestHelper.userUpdateAvatar(String.valueOf(UserConfig.getClientUserId()), url, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.USER_UPDATE_AVATAR.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        if(responseString.contains("\"result\":true")){
                            String path = getUserImageFileAndUpload(true);
                            if(path != null)
                                saveUserImage(path);
                        }
                    }
                }).execute();
            }
        });

    }

//    /**
//     * 获取群图像状态
//     * @param chat_id
//     */
//    private static void getGroupAvatarStat(final String chat_id) {
//        ApiRequestHelper.getQNRSToken(String.valueOf(UserConfig.getClientUserId()), chat_id, new ApiRequestHelper.BuildParamsCallBack() {
//            @Override
//            public void build(RequestParams params) {
//                ApiUrlHelper.GRAPH_QNRS.build(params, new TextHttpResponseHandler() {
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                        T8Log.PAN_JIA_FANG.d("GRAPH_QNRS :" + responseString);
//                        RequestUtils.checkError(throwable);
//                    }
//
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                        T8Log.PAN_JIA_FANG.d("GRAPH_QNRS :" + responseString);
//                        try {
//                            JSONObject object = new JSONObject(responseString);
//                            if (object.has("accessUrl") && object.has("Authorization")) {
//                                String url = object.getString("accessUrl");
//                                String auth = object.getString("Authorization");
//
//                                T8Log.PAN_JIA_FANG.d("GRAPH_QNRS : 请求七牛状态");
//
//                                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//                                asyncHttpClient.addHeader("Authorization", auth);
//
//                                asyncHttpClient.get(url, new TextHttpResponseHandler() {
//                                    @Override
//                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                        T8Log.PAN_JIA_FANG.d("GRAPH_QNRS Error :"+statusCode+" " + responseString);
//                                        if(statusCode == 612){//如果图像不存在
//                                            getGroupImageFilePath(Integer.parseInt(chat_id));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                                        T8Log.PAN_JIA_FANG.d("GRAPH_QNRS :"+statusCode+" " + responseString);
//                                        if(statusCode == 200){
//                                            try{
//                                                JSONObject object = new JSONObject(responseString);
//                                                long putTime = object.getLong("putTime");
//                                                if(System.currentTimeMillis() - putTime/100 > TIME){
//                                                    T8Log.PAN_JIA_FANG.d("GRAPH_QNRS : 距离上次更新时间较长，更新头像");
//                                                    getGroupImageFilePath(Integer.parseInt(chat_id));
//                                                }
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
//                                });
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).execute();
//            }
//        });
//    }
}
