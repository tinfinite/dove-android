package com.tinfinite.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinfinite.ui.widget.LoadMoreButton;
import com.tinfinite.ui.widget.LoadMoreRecyclerView;

import org.telegram.messenger.R;

public abstract class CursorRecyclerAdapter extends HeaderFooterRecyclerViewAdapter implements LoadMoreButton.LoadMoreInterface {
    protected Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
    private LoadMoreButton mLoadMoreButton;
    private LoadMoreRecyclerView.OnLoadMoreListener mOnLoadMoreListener;

    protected OnItemClickListener itemClickListener;

    public CursorRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }

        @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public int getFooterItemCount() {
        return 1;
    }

    @Override
    public int getContentItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterItemViewHolder(ViewGroup parent, int footerViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_button, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        return null;
    }

    @Override
    public void onBindHeaderItemViewHolder(RecyclerView.ViewHolder headerViewHolder, int position) {
    }

    @Override
    public void onBindFooterItemViewHolder(RecyclerView.ViewHolder footerViewHolder, int position) {
        mLoadMoreButton = ((FooterViewHolder) footerViewHolder).loadMoreButton;
        mLoadMoreButton.bind(this);
    }

    public abstract void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor);

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(contentViewHolder, mCursor);
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        LoadMoreButton loadMoreButton;

        public FooterViewHolder(View view) {
            super(view);
            loadMoreButton = (LoadMoreButton) view.findViewById(R.id.load_more_button);
        }
    }

    public boolean hasMoreItems = true, isFailed, isLoading = true, isPrivate, isLoadCompleted;

    public void setOnLoadMoreListener(LoadMoreRecyclerView.OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    @Override
    public boolean hasMoreItems() {
        return hasMoreItems;
    }

    @Override
    public boolean isFailed() {
        return isFailed;
    }

    @Override
    public boolean isLoadMoreVisible() {
        return !isLoadCompleted || !hasMoreItems;
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public boolean isPrivate() {
        return isPrivate;
    }

    @Override
    public void loadMore() {
        if(mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }

    public interface OnItemClickListener{
        public void onItemClick(View view, int position);
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}
