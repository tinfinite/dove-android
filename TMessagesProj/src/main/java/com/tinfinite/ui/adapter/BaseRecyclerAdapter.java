package com.tinfinite.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinfinite.ui.widget.LoadMoreButton;
import com.tinfinite.ui.widget.LoadMoreRecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by caiying on 12/26/14.
 */
public class BaseRecyclerAdapter<T> extends HeaderFooterRecyclerViewAdapter implements LoadMoreButton.LoadMoreInterface {
    protected Context context;
    protected BaseFragment baseFragment;
    protected ArrayList<T> datas = new ArrayList<>();
    private boolean showEmpty;
    private boolean isEmpty;
    private EmptyEntity emptyEntity = new EmptyEntity();

    private LoadMoreButton mLoadMoreButton;
    private LoadMoreRecyclerView.OnLoadMoreListener mOnLoadMoreListener;

    protected OnItemClickListener itemClickListener;

    public BaseRecyclerAdapter(BaseFragment baseFragment, ArrayList<T> datas) {
        this.context = baseFragment.getParentActivity();
        this.baseFragment = baseFragment;
        this.datas = datas;
    }

    public BaseRecyclerAdapter(BaseFragment baseFragment, ArrayList<T> datas, boolean empty) {
        this.context = baseFragment.getParentActivity();
        this.baseFragment = baseFragment;
        this.datas = datas;
        this.showEmpty = empty;
    }

    public void refreshNewItemsData(ArrayList<T> paramArrayList) {
        if (null == paramArrayList)
            return;
        ArrayList<T> localArrayList = (ArrayList<T>) paramArrayList.clone();
        this.datas.clear();
        if (showEmpty) {
            if (localArrayList != null && localArrayList.size() > 0) {
                isEmpty = false;
                this.datas.addAll(localArrayList);
            } else {
                isEmpty = true;
                this.datas.add((T)emptyEntity);
            }
        } else {
            this.datas.addAll(localArrayList);
        }

        notifyDataSetChanged();
    }

    public void insertItemDataHeading(T data){
        if(data == null)
            return;
        if (datas.contains(emptyEntity)) {
            datas.remove(emptyEntity);
        }
        this.datas.add(0, data);
        notifyDataSetChanged();
    }

    public void insertItemsData(ArrayList<T> paramArrayList) {
        if (null == paramArrayList)
            return;
        ArrayList<T> localArrayList = (ArrayList<T>) paramArrayList.clone();
        Iterator<T> localIterator = this.datas.iterator();
        while (localIterator.hasNext()) {
            Object localObject = localIterator.next();
            if (!localArrayList.contains(localObject))
                continue;
            localArrayList.remove(localObject);
        }
        this.datas.addAll(0, localArrayList);
        notifyDataSetChanged();
    }

    public void appendItemsData(ArrayList<T> paramArrayList) {
        if (null == paramArrayList)
            return;

        int start = this.datas.size();

        ArrayList<T> localArrayList = (ArrayList<T>) paramArrayList.clone();
        Iterator<T> localIterator = this.datas.iterator();
        while (localIterator.hasNext()) {
            Object localObject = localIterator.next();
            if (!localArrayList.contains(localObject))
                continue;
            localArrayList.remove(localObject);
        }
        this.datas.addAll(localArrayList);

        if (start > 1)
            notifyContentItemChanged(start - 1);

        int number = this.datas.size() - start;
        if (number > 0) {
            notifyContentItemRangeInserted(start, number);
        }
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
        return datas.size();
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

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        LoadMoreButton loadMoreButton;

        public FooterViewHolder(View view) {
            super(view);
            loadMoreButton = (LoadMoreButton) view.findViewById(R.id.load_more_button);
        }
    }


    public boolean hasMoreItems = true, isFailed, isLoading = true, isPrivate;

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
        if (showEmpty) {
            return !isEmpty ;
        } else {
            return true;
        }
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

    public class EmptyEntity{

    }
}
