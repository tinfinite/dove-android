package com.tinfinite.ui.fragment;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.melnykov.fab.FloatingActionButton;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.provider.loader.PostLoader;
import com.tinfinite.ui.adapter.CursorRecyclerAdapter;
import com.tinfinite.ui.adapter.GroupStreamAdapter;

import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;

/**
 * Created by PanJiafang on 15/3/27.
 */
public class PublicStreamFragment extends BaseStreamFragment implements NotificationCenter.NotificationCenterDelegate, LoaderManager.LoaderCallbacks<Cursor>{
    private static final int LOADER_NODE_VIEW_ID = 1001;
    private FloatingActionButton floatingButton;

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
            mEmptyText.setText(LocaleController.getString("PublicStreamNoResult", R.string.PublicStreamNoResult));
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

    public PublicStreamFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        if(arguments != null)
            chat_id = arguments.getInt("chat_id", 0);
        else
            chat_id = 0;

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.postsEndReached);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.postDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatFromPublicPost);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postsEndReached);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.postDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatFromPublicPost);
        if (getParentActivity() != null) {
            getParentActivity().getLoaderManager().destroyLoader(LOADER_NODE_VIEW_ID);
        }
    }

    @Override
    protected void onRefresh() {
        pageNumber = 0;
        PostsController.getInstance().loadPost(chat_id, 0);
    }

    @Override
    public void onLoadMore() {
        pageNumber++;
        T8Log.ZHAO_ZHEN.d( "onLoadMore pageNumber" + pageNumber);
        PostsController.getInstance().loadPost(chat_id, pageNumber);
    }

    @Override
    public void init() {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        if(chat_id <= 0)
            actionBar.setTitle(LocaleController.getString("PublicStream", R.string.PublicStream));
        else
            actionBar.setTitle(LocaleController.getString("GroupStream", R.string.GroupStream));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1)
                    finishFragment();
            }
        });

        floatingButton = (FloatingActionButton) fragmentView.findViewById(R.id.floating_button);
        floatingButton.setVisibility(View.VISIBLE);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentFragment(new PostNewStreamActivity(PostNewStreamActivity.createBundle(chat_id)));
            }
        });
        floatingButton.attachToRecyclerView(mNodeListView);

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
                entity = entity.jsonParse(s);
                final int replyCount = cursor.getInt(PostLoader.PostQuery.POST_REPLY_COUNT_COLUMN);
                final int score = cursor.getInt(PostLoader.PostQuery.POST_SCORE_COLUMN);
                int upvote = cursor.getInt(PostLoader.PostQuery.POST_ISUPVOTE_COLUMN);
                int downvote = cursor.getInt(PostLoader.PostQuery.POST_ISDOWNVOTE_COLUMN);
                final boolean is_upvote = upvote ==  1 ? true : false;
                final boolean is_downvote = downvote == 1 ? true : false;
                presentFragment(new NodeDetailFragment(NodeDetailFragment.createBundle(chat_id, entity, replyCount, score, is_upvote, is_downvote)));
            }
        });

        //代码监测
        Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
        t.setScreenName("公共信息流");
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
        } else if (id == NotificationCenter.chatFromPublicPost) {
            removeSelfFromStack();
        } else if (id == NotificationCenter.postDidLoaded) {
            if (getParentActivity() != null) {
                getParentActivity().getLoaderManager().restartLoader(LOADER_NODE_VIEW_ID, null, this);
            }
        }
    }
}
