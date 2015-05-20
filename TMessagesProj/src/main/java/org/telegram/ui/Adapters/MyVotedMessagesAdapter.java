/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.android.MessagesStorage;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.DialogCell;
import com.tinfinite.entity.MyVoteMessageEntity;

import java.util.ArrayList;
import java.util.List;

public class MyVotedMessagesAdapter extends BaseFragmentAdapter {

    private Context mContext;
    private ArrayList<TLObject> searchResult = new ArrayList<>();
    private ArrayList<CharSequence> searchResultNames = new ArrayList<>();
    private ArrayList<MessageObject> searchResultMessages = new ArrayList<>();
    private long reqId = 0;
    private int lastReqId;
    private VoteMessageAdapterDelegate delegate;
    MyVoteMessageEntity entity = new MyVoteMessageEntity();

    public static interface VoteMessageAdapterDelegate {
        public abstract void searchStateChanged(boolean searching);
    }

    public MyVotedMessagesAdapter(Context context) {
        mContext = context;
    }

    public void setDelegate(VoteMessageAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    private void    getMessagesInternal(final ArrayList<Integer> mids) {
        if (reqId != 0) {
            ConnectionsManager.getInstance().cancelRpc(reqId, true);
            reqId = 0;
        }
        if (mids == null || mids.isEmpty()) {
            searchResultMessages.clear();
            lastReqId = 0;
            notifyDataSetChanged();
            if (delegate != null) {
                delegate.searchStateChanged(false);
            }
            return;
        }

        final TLRPC.TL_messages_getMessages req = new TLRPC.TL_messages_getMessages();
        for (Integer mid : mids) {
            req.id.add(mid);
        }
        if (req.id.isEmpty()) {
            return;
        }

        final int currentReqId = ++lastReqId;

        reqId = ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentReqId == lastReqId) {
                            if (error == null) {
                                TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                                MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, true, true);
                                MessagesController.getInstance().putUsers(res.users, false);
                                MessagesController.getInstance().putChats(res.chats, false);
                                for (TLRPC.Message message : res.messages) {
                                    if (message.message != null) {
                                        MessageObject obj = new MessageObject(message, null, false);
                                        for (MyVoteMessageEntity.VoteData data : entity.getData()) {
                                            if (Integer.valueOf(data.getTg_message_id()) == message.id) {
                                                obj.messageOwner.voteValue = data.getPoints();
                                            }
                                        }
                                        searchResultMessages.add(obj);
                                    }
                                }
                                notifyDataSetChanged();
                            }
                        }
                        if (delegate != null) {
                            delegate.searchStateChanged(false);
                        }
                        reqId = 0;
                    }
                });
            }
        });
    }

    public void getMyVoteMessageIds() {
        if (delegate != null) {
            delegate.searchStateChanged(true);
        }
        ApiRequestHelper.getMyVotedMsgIds(String.valueOf(UserConfig.getClientUserId()), T8UserConfig.getUserId(), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.GET_MY_VOTE_MSG.build(params,
                        new ApiHttpClient.DoveHttpResponseHandler() {
                            @Override
                            public void onSuccess(String responseString) {
                                entity = entity.jsonParse(responseString);
                                if(entity != null && entity.getError() == null){
                                    List<MyVoteMessageEntity.VoteData> datas = entity.getData();
                                    ArrayList<Integer> Ids = new ArrayList<Integer>();

                                    for (MyVoteMessageEntity.VoteData data : datas) {
                                        Ids.add(Integer.valueOf(data.getTg_message_id()));
                                    }
                                    getMessages(Ids);
                                } else {
                                    if (delegate != null) {
                                        delegate.searchStateChanged(false);
                                    }
                                }
                            }
                        }).execute();
            }
        });
    }

    public void getMessages(final ArrayList<Integer> searchIds) {
        if (searchIds == null || searchIds.isEmpty()) {
            searchResult.clear();
            searchResultNames.clear();
            getMessagesInternal(null);
            notifyDataSetChanged();
        } else {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    getMessagesInternal(searchIds);
                }
            });
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        return searchResultMessages.size();
    }

    @Override
    public Object getItem(int i) {
        return searchResultMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new DialogCell(mContext);
        }
        ((DialogCell) view).useSeparator = (i != getCount() - 1);
        MessageObject messageObject = (MessageObject)getItem(i);
        ((DialogCell) view).setDialog(messageObject.getDialogId(), messageObject, messageObject.messageOwner.date);
        return view;
    }

    @Override
    public boolean isEmpty() {
        return searchResultMessages.isEmpty();
    }
}
