package com.tinfinite.ui.fragment;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.melnykov.fab.FloatingActionButton;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8SharedPreferences;
import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.provider.loader.PostLoader;
import com.tinfinite.ui.adapter.CursorRecyclerAdapter;
import com.tinfinite.ui.adapter.GroupStreamAdapter;

import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;

/**
 * Created by PanJiafang on 15/3/27.
 */
public class GroupStreamFragment extends BaseStreamFragment implements NotificationCenter.NotificationCenterDelegate, LoaderManager.LoaderCallbacks<Cursor>{
    private static final int LOADER_GROUP_POST_ID = 1002;
    private FloatingActionButton floatingButton;
    private DiscoverEntity.Community community;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return PostLoader.constructLoaderForNodeQuery(getParentActivity(), chat_id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        T8Log.ZHAO_ZHEN.d("onLoadFinished count " + data.getCount());
        mListAdapter.swapCursor(data);
        onRefreshingStateChanged(false);
        // TO DO load more need optimize, because adapter has notifyDataSetChanged
        // end reached need optimize
        updateLoadMoreState();
        mListAdapter.isLoadCompleted = true;
        if (data.getCount() == 0) {
            mEmptyContainer.setVisibility(View.VISIBLE);
            mEmptyText.setText(LocaleController.getString("GroupStreamNoResult", R.string.GroupStreamNoResult));
            mEmptyContainer.setBackgroundResource(R.drawable.group_board_empty);
        } else {
            mEmptyContainer.setVisibility(View.GONE);
        }
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

    public GroupStreamFragment(Bundle args) {
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
        pageNumber = 0;
        mListAdapter.isLoadCompleted = true;
        PostsController.getInstance().loadPost(chat_id, 0);
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
        if (chat != null) {
            actionBar.setTitle(MessagesController.getInstance().getChat(chat_id).title);
            floatingButton.setVisibility(View.VISIBLE);
        } else {
            actionBar.setTitle(community.getName());
            floatingButton.setVisibility(View.GONE);
        }

        actionBar.createMenu();
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1) {
                    finishFragment();
                }
            }
        });

        mListAdapter = new GroupStreamAdapter(this, chat_id);
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

        //代码监测
        Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
        t.setScreenName("群留言板");
        t.send(new HitBuilders.AppViewBuilder().build());
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
}
