package com.tinfinite.utils;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;

import org.apache.http.Header;
import org.telegram.android.MessageObject;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.UserConfig;
import com.tinfinite.entity.VoteQueryEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PanJiafang on 15/3/20.
 */
public class VoteUtil {
    private ArrayList<String> synced_ids;
    private static ArrayList<String> wait_ids;
    private static HashMap<Integer, HashMap<Integer, MessageObject>> chat_wait;
    private static HashMap<Integer, HashMap<Integer, MessageObject>> chat_synced;
    private static HashMap<Integer, MessageObject> message_wait;
    private static HashMap<Integer, MessageObject> message_synced;

    public VoteUtil() {
        synced_ids = new ArrayList<>();
        wait_ids = new ArrayList<>();
    }

    public static void getVoteDataFromServer(int chat_id, List<MessageObject> messages){
        init(chat_id);

        //筛选未同步并且不在等待序列中的消息
        for(MessageObject message : messages){
            if(message_wait.get(message.getId()) != null || message_synced.get(message.getId()) != null)
                continue;
            else {
                message_wait.put(message.getId(), message);
            }
        }

        //临时变量，记载本次向服务器请求的所有消息，便于后续等待队列与已同步队列的数据移动
        final HashMap<Integer, MessageObject> temp = new HashMap<>();
        StringBuffer sb = new StringBuffer();

        for(Map.Entry<Integer, MessageObject> entry : message_wait.entrySet()){
            String key = entry.getValue().messageOwner.key;
            sb.append(key+",");
            temp.put(entry.getKey(), entry.getValue());
        }

        if(message_wait.size() > 0) {
            String ids = sb.subSequence(0, sb.length() - 1).toString();
            ApiRequestHelper.voteQurey(String.valueOf(UserConfig.getClientUserId()), chat_id, ids, new ApiRequestHelper.BuildParamsCallBack() {
                @Override
                public void build(RequestParams params) {
                    ApiUrlHelper.VOTEQUERY.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                        @Override
                        public void onFailure() {
                            //网络通讯失败，将消息从已同步序列移至等待序列
                            for(Map.Entry<Integer, MessageObject> entry : temp.entrySet()){
                                message_synced.put(entry.getKey(), entry.getValue());
                                message_wait.remove(entry.getKey());
                            }
                        }

                        @Override
                        public void onSuccess(String responseString) {
                            VoteQueryEntity queryEntity = new VoteQueryEntity();
                            queryEntity = queryEntity.jsonParse(responseString);
                            if(queryEntity.getError() == null){
                                List<VoteQueryEntity.VoteEntity> data = queryEntity.getData();
                                if(data != null && data.size() > 0){
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.voteUpdateMany, data);
                                }
                            } else {
                                T8Toast.lt(queryEntity.getError().getMessage());
                            }
                        }
                    }).execute();
                }
            });

            //将消息从等待序列中移至已同步序列，如果网络通讯失败，将消息从已同步序列移至等待序列
            for(Map.Entry<Integer, MessageObject> entry : temp.entrySet()){
                message_synced.put(entry.getKey(), entry.getValue());
                message_wait.remove(entry.getKey());
            }
        }
    }

    private static void init(int chat_id){
        if(chat_wait == null)
            chat_wait = new HashMap<>();
        if(chat_synced == null)
            chat_synced = new HashMap<>();
        message_wait = chat_wait.get(chat_id);
        if(message_wait == null) {
            message_wait = new HashMap<>();
            chat_wait.put(chat_id, message_wait);
        }
        message_synced = chat_synced.get(chat_id);
        if(message_synced == null) {
            message_synced = new HashMap<>();
            chat_synced.put(chat_id, message_synced);
        }
    }

    public static void clear(int chat_id){
        init(chat_id);
        chat_wait.remove(chat_id);
        chat_synced.remove(chat_id);
    }

    public static void resetMessageStatus(int chat_id, MessageObject message){
        init(chat_id);

        message_synced.remove(message.getId());
        message_wait.put(message.getId(), message);
    }
}
