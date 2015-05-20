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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Adapters.MyVotedMessagesAdapter;

public class MyVotedMessagesActivity extends BaseFragment {
    private ListView messagesListView;
    private MyVotedMessagesAdapter dialogsAdapter;
    private View searchEmptyView;
    private View progressView;

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

            dialogsAdapter = new MyVotedMessagesAdapter(getParentActivity());
            dialogsAdapter.setDelegate(new MyVotedMessagesAdapter.VoteMessageAdapterDelegate() {
                @Override
                public void searchStateChanged(boolean search) {
                    if ( messagesListView != null) {
                        progressView.setVisibility(search ? View.VISIBLE : View.INVISIBLE);
                        searchEmptyView.setVisibility(search ? View.INVISIBLE : View.VISIBLE);
                        messagesListView.setEmptyView(search ? progressView : searchEmptyView);
                    }
                }
            });

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
            messagesListView.setEmptyView(searchEmptyView);

            dialogsAdapter.getMyVoteMessageIds();

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
                    // dove add
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

                    presentFragment(new ChatActivity(args));
                }
            });

            messagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    if (i == SCROLL_STATE_TOUCH_SCROLL) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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
}
