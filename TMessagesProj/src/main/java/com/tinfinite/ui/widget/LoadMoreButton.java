package com.tinfinite.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;

/**
 * Created by caiying on 12/27/14.
 */
public class LoadMoreButton extends ViewAnimator {
    private static final int INDEX_ALL_ITEMS_LOADED = 0;
    private static final int INDEX_LOADING = 1;
    private static final int INDEX_LOAD_MORE = 2;
    private static final int INDEX_PRIVATE = 3;
    private static final int INDEX_RETRY = 4;
    LoadMoreInterface mLoadMoreInterface;

    public LoadMoreButton(Context paramContext) {
        super(paramContext);
        init();
    }

    public LoadMoreButton(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    private void bindInternal(LoadMoreInterface paramLoadMoreInterface) {
        if (!wouldShow(paramLoadMoreInterface)) {
//            setVisibility(View.GONE);
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.height = 0;
        } else {
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.height = AndroidUtilities.dp(48);
            setVisibility(View.VISIBLE);
            if (paramLoadMoreInterface.isPrivate())
                setDisplayedChild(INDEX_PRIVATE);
            else if (!paramLoadMoreInterface.hasMoreItems())
                setDisplayedChild(INDEX_ALL_ITEMS_LOADED);
            else if (paramLoadMoreInterface.isLoading())
                setDisplayedChild(INDEX_LOADING);
            else if (paramLoadMoreInterface.isFailed())
                setDisplayedChild(INDEX_RETRY);
            else if (paramLoadMoreInterface.hasMoreItems())
                setDisplayedChild(INDEX_LOAD_MORE);
        }
    }

    private View createAllItemsLoadedButton() {
        TextView loadMoreTextView = new TextView(getContext());
        loadMoreTextView.setText(getResources().getString(R.string.load_more_no_more_items));
        loadMoreTextView.setTextSize(14);
        loadMoreTextView.setTextColor(getResources().getColor(R.color.theme_t8_font_2));
        loadMoreTextView.setGravity(Gravity.CENTER);

        return loadMoreTextView;
    }

    private View createLoadMoreButton() {
        TextView loadMoreTextView = new TextView(getContext());
        loadMoreTextView.setText(getResources().getString(R.string.load_more));
        loadMoreTextView.setTextSize(14);
        loadMoreTextView.setTextColor(getResources().getColor(R.color.theme_t8_font_2));
        loadMoreTextView.setGravity(Gravity.CENTER);

        loadMoreTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreInterface.loadMore();
                setDisplayedChild(INDEX_LOADING);
            }
        });
        return loadMoreTextView;
      }

    private View createLoadingButton() {
        return new LoadingView(getContext());
    }

    private View createPrivateUserState() {
        TextView loadMoreTextView = new TextView(getContext());
        loadMoreTextView.setText(getResources().getString(R.string.load_more_no_authorize));
        loadMoreTextView.setTextSize(14);
        loadMoreTextView.setTextColor(getResources().getColor(R.color.theme_t8_font_2));
        loadMoreTextView.setGravity(Gravity.CENTER);

        return loadMoreTextView;
    }

    private View createRetryState() {
        TextView loadMoreTextView = new TextView(getContext());
        loadMoreTextView.setText(getResources().getString(R.string.load_more_retry));
        loadMoreTextView.setTextSize(14);
        loadMoreTextView.setTextColor(getResources().getColor(R.color.theme_t8_font_2));
        loadMoreTextView.setGravity(Gravity.CENTER);

        loadMoreTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreInterface.loadMore();
                setDisplayedChild(INDEX_LOADING);
            }
        });
        return loadMoreTextView;
      }

    private void init() {
        LayoutParams localLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(createAllItemsLoadedButton(), localLayoutParams);
        addView(createLoadingButton(), localLayoutParams);
        addView(createLoadMoreButton(), localLayoutParams);
        addView(createPrivateUserState(), localLayoutParams);
        addView(createRetryState(), localLayoutParams);
    }

    private static boolean wouldShow(LoadMoreInterface paramLoadMoreInterface) {
        if ((paramLoadMoreInterface.isPrivate()) ||
                ((paramLoadMoreInterface.isLoadMoreVisible())))
            return true;
        return false;
    }

    public void bind(LoadMoreInterface paramLoadMoreInterface) {
        this.mLoadMoreInterface = paramLoadMoreInterface;
        bindInternal(paramLoadMoreInterface);
    }

    public abstract interface LoadMoreInterface {
        public abstract boolean hasMoreItems();

        public abstract boolean isFailed();

        public abstract boolean isLoadMoreVisible();

        public abstract boolean isLoading();

        public abstract boolean isPrivate();

        public abstract void loadMore();
    }
}
