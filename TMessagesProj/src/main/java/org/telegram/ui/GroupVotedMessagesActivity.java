/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesStorage;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.VoteMsgDialogCell;
import com.tinfinite.entity.GroupVoteMessageEntity;

import java.util.ArrayList;
import java.util.List;

public class GroupVotedMessagesActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
    private ListView messagesListView;
    private GroupVotedMessagesAdapter dialogsAdapter;
    private View searchEmptyView;
    private View progressView;
    private long dialog_id;
    private GroupVoteMessageEntity entity = new GroupVoteMessageEntity();
    private ArrayList<MessageObject> messages = new ArrayList<>();
    private ArrayList<MessageObject> searchResultMessages = new ArrayList<>();

    public GroupVotedMessagesActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        dialog_id = arguments.getLong("dialog_id", 0);
        getMyVoteMessageIds();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDidLoaded);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messagesDidLoaded);
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater){
        if (fragmentView == null) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setAllowOverlayTitle(true);
            actionBar.setTitle(LocaleController.getString("VotedMessage", R.string.VotedMessage));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            fragmentView = inflater.inflate(R.layout.voted_messages_list, null, false);
            dialogsAdapter = new GroupVotedMessagesAdapter(getParentActivity());
            messagesListView = (ListView)fragmentView.findViewById(R.id.messages_list_view);
            messagesListView.setAdapter(dialogsAdapter);
            if (Build.VERSION.SDK_INT >= 11) {
                messagesListView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
            }

            progressView = fragmentView.findViewById(R.id.progressLayout);
            dialogsAdapter.notifyDataSetChanged();
            searchEmptyView = fragmentView.findViewById(R.id.search_empty_view);
            searchEmptyView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            TextView textView = (TextView)fragmentView.findViewById(R.id.search_empty_text);
            textView.setText(LocaleController.getString("NoResult", R.string.NoResult));
            searchStateChanged(true);
            messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (messagesListView == null || messagesListView.getAdapter() == null) {
                        return;
                    }
                    long dialog_id = 0;
                    int message_id = 0;
                    BaseFragmentAdapter adapter = (BaseFragmentAdapter)messagesListView.getAdapter();
                    Object obj = adapter.getItem(i);
                    if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject)obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.messageOwner.id;
                    }

                    if (dialog_id == 0) {
                        return;
                    }

                    Bundle args = new Bundle();
                    args.putLong("dialog_id", dialog_id);
                    int lower_part = (int)dialog_id;
                    int high_id = (int)(dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }

                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.viewVoteMessage);
                    presentFragment(new ChatActivity(args), true);
                }
            });

        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }

    private void searchStateChanged(boolean search) {
        if ( messagesListView != null) {
            progressView.setVisibility(search ? View.VISIBLE : View.INVISIBLE);
            searchEmptyView.setVisibility(search ? View.INVISIBLE : View.VISIBLE);
            messagesListView.setEmptyView(search ? progressView : searchEmptyView);
        }
    }

    public void getMyVoteMessageIds() {
        ApiRequestHelper.getGroupVotedMsgIds(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.GET_GRP_VOTE_MSG.build(params,
                        new ApiHttpClient.DoveHttpResponseHandler() {
                            @Override
                            public void onSuccess(String responseString) {
                                entity = entity.jsonParse(responseString);
                                if (entity != null && entity.getError() == null && entity.getData() != null && entity.getData().size() > 0) {
                                    MessagesStorage.getInstance().getMessages(-dialog_id, Integer.MAX_VALUE, 0, 0, classGuid, 0);
                                } else {
                                    searchStateChanged(false);
                                }
                            }
                        }, String.valueOf(dialog_id)).execute();
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, final Object... args) {
        if (id == NotificationCenter.messagesDidLoaded) {
            long did = (Long)args[0];
            if (did == -dialog_id) {
                ArrayList<MessageObject> messArr = (ArrayList<MessageObject>)args[2];

                for (int a = 0; a < messArr.size(); a++) {
                    MessageObject obj = messArr.get(a);
                    if (obj.type < 0) {
                        continue;
                    }
                    messages.add(obj);
                }

                MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        List<GroupVoteMessageEntity.VoteData> datas = entity.getData();

                        for (GroupVoteMessageEntity.VoteData data : datas) {
                            for (MessageObject obj : messages)
                            if (data.getTg_message_key().equals(getMessageKey(obj))) {
                                obj.messageOwner.voteValue = data.getPoints();
                                searchResultMessages.add(obj);
                            }
                        }

                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogsAdapter.notifyDataSetChanged();
                                searchStateChanged(false);
                            }
                        });
                    }
                });
            }
        }
    }

    private String getMessageKey(MessageObject messageObject){
        String key =  messageObject.messageOwner.from_id + "_" +messageObject.messageOwner.to_id.chat_id + "_" + messageObject.messageOwner.date+"_"+getMessageNumber(messageObject);
        return key;
    }

    private int getMessageNumber(MessageObject messageObject){
        if(messages == null || messages.size() == 0)
            return 0;
        int index = messages.lastIndexOf(messageObject);
        if(index < messages.size() - 1) {
            MessageObject temp = messages.get(index + 1);
            if(messageObject.messageOwner.from_id == temp.messageOwner.from_id && messageObject.messageOwner.date == temp.messageOwner.date)
                return getMessageNumber(temp)+1;
            return 0;
        }
        return 0;
    }

    private class GroupVotedMessagesAdapter extends BaseFragmentAdapter {

        private Context mContext;

        public GroupVotedMessagesAdapter(Context context) {
            mContext = context;
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
                view = new VoteMsgDialogCell(mContext);
            }
            ((VoteMsgDialogCell) view).useSeparator = (i != getCount() - 1);
            MessageObject messageObject = (MessageObject)getItem(i);
            ((VoteMsgDialogCell) view).setDialog(messageObject.getDialogId(), messageObject, messageObject.messageOwner.date);
            return view;
        }

        @Override
        public boolean isEmpty() {
            return searchResultMessages.isEmpty();
        }
    }
}
