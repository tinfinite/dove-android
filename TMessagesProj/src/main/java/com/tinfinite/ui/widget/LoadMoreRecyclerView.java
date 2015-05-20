package com.tinfinite.ui.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by caiying on 12/27/14.
 */
public class LoadMoreRecyclerView extends RecyclerView {

    private static final String TAG = "LoadMoreListView";

    /**
     * Listener that will receive notifications every time the list scrolls.
     */
    private OnScrollListener mOnScrollListener;

    // Listener to process load more items when user reaches the end of the list
    private OnLoadMoreListener mOnLoadMoreListener;
    // To know if the list is loading more items
    private boolean mIsLoadingMore = false;
    private int mCurrentScrollState;
    private boolean isAutoLoadMore = true;

    public LoadMoreRecyclerView(Context context) {
        super(context);
        init();
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setOnScrollListener(new LoadMoreOnScrollListener());
    }

    /**
     * Set the listener that will receive notifications every time the list
     * scrolls.
     *
     * @param l The scroll listener.
     */
    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     * Register a callback to be invoked when this list reaches the end (last
     * item be visible)
     *
     * @param onLoadMoreListener The callback to run.
     */

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    private class LoadMoreOnScrollListener extends OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            mCurrentScrollState = newState;

            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(LoadMoreRecyclerView.this, newState);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrolled(LoadMoreRecyclerView.this, dx, dy);
            }

            if (isAutoLoadMore && mOnLoadMoreListener != null) {

                int visibleItemCount = getChildCount();
                int totalItemCount = getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
                if (visibleItemCount == totalItemCount) {
                    return;
                }

                boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

                if (!mIsLoadingMore && loadMore
                        && mCurrentScrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    mIsLoadingMore = true;
                    onLoadMore();
                }

            }
        }
    }

    public boolean isAutoLoadMore() {
        return isAutoLoadMore;
    }

    public void setAutoLoadMore(boolean isAutoLoadMore) {
        this.isAutoLoadMore = isAutoLoadMore;
    }

    public void onLoadMore() {
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }

    /**
     * Notify the loading more operation has finished
     */
    public void onLoadMoreComplete() {
        mIsLoadingMore = false;
    }

    /**
     * Interface definition for a callback to be invoked when list reaches the
     * last item (the user load more items in the list)
     */
    public interface OnLoadMoreListener {
        /**
         * Called when the list reaches the last item (the last item is visible
         * to the user)
         */
        public void onLoadMore();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(mOnLayoutCallBack != null)
            mOnLayoutCallBack.onLayout(changed, l, t, r, b);
    }

    private OnLayoutCallBack mOnLayoutCallBack;

    public void setOnLayoutCallBack(OnLayoutCallBack onLayoutCallBack) {
        this.mOnLayoutCallBack = onLayoutCallBack;
    }

    public static interface OnLayoutCallBack {
        void onLayout(boolean changed, int l, int t, int r, int b);
    }
}
