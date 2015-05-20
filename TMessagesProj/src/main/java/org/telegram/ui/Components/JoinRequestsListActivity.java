/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui.Components;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.JoinRequestEntity;
import com.tinfinite.ui.fragment.BaseRecycleViewFragment;

import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Adapters.joinRequestsAdapter;

import java.util.ArrayList;

public class JoinRequestsListActivity extends BaseRecycleViewFragment<JoinRequestEntity.RequestEntity> implements NotificationCenter.NotificationCenterDelegate{
    private ArrayList<JoinRequestEntity.RequestEntity> datas = new ArrayList<>();

    private TextView emptyView;

    private String chat_id;

    public JoinRequestsListActivity(Bundle args) {
        super(args);
        arguments = args;
    }

    public static Bundle createBundle(String chat_id){
        Bundle bundle = new Bundle();
        bundle.putString("chat_id", chat_id);
        return bundle;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        ArrayList<JoinRequestEntity.RequestEntity> temp = arguments.getParcelableArrayList("join_request_list");
        if(temp != null)
            datas.addAll(temp);
        else
            chat_id = arguments.getString("chat_id");

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
    }

    @Override
    protected void onRefresh() {
        pageNumber = 0;

        getData();
    }

    @Override
    public void onLoadMore() {
        getData();
    }

    @Override
    public void init() {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("JoinRequests", R.string.JoinRequests));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        mListAdapter = new joinRequestsAdapter(this, datas);

        emptyView = new TextView(getParentActivity());
        emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyView.setTextColor(Color.BLACK);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void getData(){

        ApiRequestHelper.personalCommentsOrVotesOrPosts(String.valueOf(UserConfig.getClientUserId()), ++pageNumber, pageCount, pageTimestamp, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.GROUP_APPLY_GET.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String rootContent) {
                        JoinRequestEntity joinRequestEntity = new JoinRequestEntity();
                        joinRequestEntity = joinRequestEntity.jsonParse(rootContent);

                        if(joinRequestEntity.getError() == null){
                            pageNumber = Integer.parseInt(joinRequestEntity.getPage());
                            pageTimestamp = joinRequestEntity.getTimestamp();

                            ArrayList<JoinRequestEntity.RequestEntity> temp = joinRequestEntity.getData();
                            if (pageNumber > 1) {
                                appendItemsData(temp);
                            } else {
                                refreshNewItemsData(temp);
                            }

                            if (temp == null || temp.size() < pageCount) {
                                hasNoMoreData();
                            }

                            //用于无数据时，给予用户提示
//                            if (mListAdapter.getContentItemCount() == 0) {
//                                mEmptyContainer.setVisibility(View.VISIBLE);
//                                mEmptyContainer.addView(emptyView);
//                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) emptyView.getLayoutParams();
//                                layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
//                                layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
//                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                                emptyView.setLayoutParams(layoutParams);
//                            } else {
//                                mEmptyContainer.setVisibility(View.GONE);
//                            }
                        }
                    }

                }, chat_id).execute();
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if(id == NotificationCenter.closeChats)
            removeSelfFromStack();
    }
}
