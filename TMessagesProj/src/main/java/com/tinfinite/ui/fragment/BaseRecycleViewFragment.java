package com.tinfinite.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tinfinite.ui.adapter.BaseRecyclerAdapter;
import com.tinfinite.ui.widget.LoadMoreRecyclerView;

import org.telegram.messenger.R;

import java.util.ArrayList;

/**
 * Created by caiying on 12/27/14.
 * 继承此类一定要指定泛型T
 */
public abstract class BaseRecycleViewFragment<T> extends AbstraceSwipeRefreshFragment implements LoadMoreRecyclerView.OnLoadMoreListener {
    protected LoadMoreRecyclerView mNodeListView;
    protected BaseRecyclerAdapter mListAdapter;
    protected RelativeLayout mEmptyContainer;
    protected FrameLayout footerContainer;
    private LinearLayoutManager layoutManager;

    protected int pageNumber = 1;
    protected long pageTimestamp = 0;
    protected int pageCount = 20;

    protected BaseRecycleViewFragment(Bundle args) {
        super(args);
    }

    public void refreshNewItemsData(ArrayList<T> paramArrayList) {
        mNodeListView.setAutoLoadMore(true);
        mListAdapter.hasMoreItems = true;
        mListAdapter.isLoading = true;
        mListAdapter.refreshNewItemsData(paramArrayList);
        onRefreshingStateChanged(false);
    }

    public void insertItemsData(ArrayList<T> paramArrayList) {
        mListAdapter.insertItemsData(paramArrayList);
        onRefreshingStateChanged(false);
    }

    public void appendItemsData(ArrayList<T> paramArrayList) {
        mListAdapter.appendItemsData(paramArrayList);
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

    public void hasNoMoreData() {
        onRefreshingStateChanged(false);
        mNodeListView.setAutoLoadMore(false);
        mListAdapter.hasMoreItems = false;
        mListAdapter.isLoading = false;
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if(fragmentView == null) {
            fragmentView = super.createView(context, inflater);
            layoutManager = new LinearLayoutManager(getParentActivity());
            mNodeListView = (LoadMoreRecyclerView) fragmentView.findViewById(R.id.recycleview);
            mNodeListView.setLayoutManager(layoutManager);

            mEmptyContainer = (RelativeLayout) fragmentView.findViewById(R.id.swipe_refresh_empty_container);
            footerContainer = (FrameLayout) fragmentView.findViewById(R.id.swipe_refresh_footer_container);

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
