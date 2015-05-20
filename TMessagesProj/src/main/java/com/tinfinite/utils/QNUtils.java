package com.tinfinite.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.utils.EncryptUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by PanJiafang on 15/3/25.
 */
public class QNUtils {

    public interface QNUploadDelegate{

        /**
         * @param result 为null时表示上传失败。
         * Key为本地地址，Value为对应的七牛地址
         */
        public void uploadResult(HashMap<String, String> result);
    }

    private final int MSG_WHAT_QINIU = 1;

    private ArrayList<String> photos = new ArrayList<String>();
    private static HashMap<String, String> uri_qiniuKey = new HashMap<String, String>();
    private QNUploadDelegate delegate;
    private String token;

    private int qiniu_upload_count = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_QINIU) {
                qiniu_upload_count++;
                if (qiniu_upload_count == photos.size()) {
                    update();
                }
            }
        }
    };

    /**
     * 构造函数
     * @param token 七牛的Token
     * @param photos 本地图片地址数组
     * @param delegate 结果回调
     */
    public QNUtils(String token, ArrayList<String> photos, QNUploadDelegate delegate) {
        this.photos.clear();
        this.photos.addAll(photos);

        this.delegate = delegate;
        this.token = token;
    }

    /**
     * 开始上传
     */
    public void start(){
        T8Log.PAN_JIA_FANG.d("准备上传图片到七牛");
        T8Log.PAN_JIA_FANG.d("上传图片路径"+photos.toString());

        new Thread(){
            @Override
            public void run() {
                super.run();

                UploadManager uploadManager = new UploadManager();
                qiniu_upload_count = 0;

                for (int i = 0; i < photos.size(); i++) {

                    /**
                     * 如果第一次发布，文件的key为null，为该文件生成一个key，
                     * 如果该文件上传成功，则将该文件uri与key做映射
                     * 重新发布时，如果该文件已经有key的映射，则跳过。
                     */

                    final String path = photos.get(i);

                    T8Log.PAN_JIA_FANG.d("上传图片:"+path);

                    if (path.startsWith("http://") || uri_qiniuKey.get(path) != null) {
                        qiniu_upload_count++;

                        if(qiniu_upload_count == photos.size()) {
                            update();
                            break;
                        }
                        continue;
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                    if(options.outWidth > 640){
                        options.inSampleSize = options.outWidth / 640;
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(path, options);
                    } else {
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(path, options);
                    }
                    String extra = "";
                    extra = "w"+bitmap.getWidth()+"h"+bitmap.getHeight();

                    String qiniuFile = EncryptUtil.generate9CellPicName(String.valueOf(i))+extra;

                    if(path.endsWith(".gif")){
                        File file = new File(path);
                        if(file.exists())
                            uploadManager.put(file, qiniuFile+".gif", token, new UpCompletionHandler() {
                                @Override
                                public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                                    if (jsonObject != null) {
                                        T8Log.PAN_JIA_FANG.d(s);
                                        if (responseInfo.error == null) {
                                            uri_qiniuKey.put(path, "http://tinfinite.qiniudn.com/" + s);
                                        }

                                        mHandler.sendEmptyMessage(MSG_WHAT_QINIU);
                                    }
                                }
                            }, null);
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        if(bitmap != null) {
                            if(bitmap.getWidth() > 640) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                            } else {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                extra = "w"+bitmap.getWidth()+"h"+bitmap.getHeight();
                            }
                            bitmap.recycle();
                            T8Log.PAN_JIA_FANG.d(extra);
                        }

                        uploadManager.put(baos.toByteArray(), qiniuFile, token, new UpCompletionHandler() {
                            @Override
                            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                                if (jsonObject != null) {
                                    T8Log.PAN_JIA_FANG.d(s);
                                    if (responseInfo.error == null) {
                                        uri_qiniuKey.put(path, "http://tinfinite.qiniudn.com/" + s);
                                    }

                                    mHandler.sendEmptyMessage(MSG_WHAT_QINIU);
                                }
                            }
                        }, null);

                        try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();

    }

    /**
     * 发布
     */
    private void update() {
        if (qiniu_upload_count != photos.size()) {
            if(delegate != null)
                delegate.uploadResult(null);
            return;
        }

        T8Log.PAN_JIA_FANG.d("头像上传成功，准备更新");

        HashMap<String, String> result = new HashMap<>();
        for(String local : photos){
            result.put(local, uri_qiniuKey.get(local));
        }

        if(delegate != null)
            delegate.uploadResult(result);
    }
}
