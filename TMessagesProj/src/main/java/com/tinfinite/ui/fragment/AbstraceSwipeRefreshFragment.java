package com.tinfinite.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tinfinite.ui.widget.MultiSwipeRefreshLayout;

import org.telegram.android.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;


/**
 * Created by caiying on 11/30/14.
 */
public abstract class AbstraceSwipeRefreshFragment extends BaseFragment implements MultiSwipeRefreshLayout.CanChildScrollUpCallback, NotificationCenter.NotificationCenterDelegate{

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mProgressBarTopWhenActionBarShown;
    private boolean mActionBarShown = true;

    protected AbstraceSwipeRefreshFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
            fragmentView = new LinearLayout(context);
            fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
            fragmentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            View view = inflater.inflate(R.layout.fragment_abstracerefresh, null);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setColorSchemeResources(
                        R.color.refresh_progress_1,
                        R.color.refresh_progress_2,
                        R.color.refresh_progress_3);
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        requestDataRefresh();
                    }
                });

                if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
                    MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
                    mswrl.setCanChildScrollUpCallback(this);
                }
                mSwipeRefreshLayout.setProgressViewOffset(false, - swipeRefreshOffset(), swipeRefreshOffset());
            }

            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ((LinearLayout) fragmentView).addView(view);
        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if(id == NotificationCenter.closeChats)
            removeSelfFromStack();
    }

    protected void setProgressBarTopWhenActionBarShown(int progressBarTopWhenActionBarShown) {
        mProgressBarTopWhenActionBarShown = progressBarTopWhenActionBarShown;
        updateSwipeRefreshProgressBarTop();
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        if(getParentActivity() == null)
            return;

        int progressBarStartMargin = getParentActivity().getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getParentActivity().getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = mActionBarShown ? mProgressBarTopWhenActionBarShown : 0;
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);
    }

    protected abstract void requestDataRefresh();

    protected int swipeRefreshOffset() {
        return (int) getParentActivity().getResources().getDimension(R.dimen.swipe_refresh_progress_bar_end_margin);
    }

    public void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    protected void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    public void closeSwipeBack(){
        swipeBackEnabled = false;
    }
}
