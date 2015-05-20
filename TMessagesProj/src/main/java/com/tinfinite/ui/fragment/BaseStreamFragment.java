package com.tinfinite.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tinfinite.ui.adapter.GroupStreamAdapter;
import com.tinfinite.ui.widget.LoadMoreRecyclerView;

import org.telegram.messenger.R;

/**
 * Created by caiying on 12/27/14.
 * 继承此类一定要指定泛型T
 */
public abstract class BaseStreamFragment<T> extends AbstraceSwipeRefreshFragment implements LoadMoreRecyclerView.OnLoadMoreListener{
    protected LoadMoreRecyclerView mNodeListView;
    protected GroupStreamAdapter mListAdapter;
    protected RelativeLayout mEmptyContainer;
    protected TextView mEmptyText;
//    protected FrameLayout footerContainer;
    private LinearLayoutManager layoutManager;
    protected int chat_id;

    protected int pageNumber = 1;

    protected BaseStreamFragment(Bundle args) {
        super(args);
    }

    public void hasNoMoreData() {
        onRefreshingStateChanged(false);
        mNodeListView.setAutoLoadMore(false);
        mListAdapter.hasMoreItems = false;
        mListAdapter.isLoading = false;
        mListAdapter.notifyDataSetChanged();
    }

    public void updateLoadMoreState() {
        onRefreshingStateChanged(false);

        // 通过页面Number控制ListView是否自动加载更多
        if(pageNumber <= 3) {
            mNodeListView.setAutoLoadMore(true);
            mListAdapter.isLoading = true;
        } else {
            mNodeListView.setAutoLoadMore(false);
            mListAdapter.isLoading = false;
        }
        mNodeListView.onLoadMoreComplete();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if(fragmentView == null) {
            fragmentView = super.createView(context, inflater);
            layoutManager = new LinearLayoutManager(getParentActivity());
            mNodeListView = (LoadMoreRecyclerView) fragmentView.findViewById(R.id.recycleview);
            mNodeListView.setLayoutManager(layoutManager);

            mEmptyContainer = (RelativeLayout) fragmentView.findViewById(R.id.swipe_refresh_empty_container);
            mEmptyText = (TextView) fragmentView.findViewById(R.id.empty_text_view);
//            footerContainer = (FrameLayout) fragmentView.findViewById(R.id.swipe_refresh_footer_container);

            init();

            mNodeListView.setAdapter(mListAdapter);

            // 控制手动加载更多
            mListAdapter.setOnLoadMoreListener(this);
            // 控制自动加载更多
            mNodeListView.setOnLoadMoreListener(this);

            onRefresh();
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }

        return fragmentView;
    }

    @Override
    protected void requestDataRefresh() {
        onRefresh();
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return ViewCompat.canScrollVertically(mNodeListView, -1);
    }

    protected abstract void onRefresh();

    public abstract void onLoadMore();

    /**
     * 初始化数据，尤其是mListAdapter
     */
    public abstract void init();

}
