package com.tinfinite.ui.fragment;


import android.os.Bundle;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.PersonalPostsEntity;
import com.tinfinite.entity.PersonalVotesEntity;
import com.tinfinite.entity.StreamResultEntity;
import com.tinfinite.ui.adapter.BaseRecyclerAdapter;
import com.tinfinite.ui.adapter.PersonalPostsAdapter;
import com.tinfinite.ui.adapter.PersonalVotesAdapter;

import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/4/20.
 */
public class PersonalPostFragment extends BaseRecycleViewFragment {

    private ArrayList<NodeEntity> datas;

    protected PersonalPostFragment(Bundle args) {
        super(args);
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
        actionBar.setTitle(LocaleController.getString("", R.string.PersonalPost));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        //代码监测
        Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
        t.setScreenName("个人已发布");
        t.send(new HitBuilders.AppViewBuilder().build());

        datas = new ArrayList<>();

        mListAdapter = new PersonalPostsAdapter(this, datas, 0);
        mListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                T8Log.PAN_JIA_FANG.d(""+position);
                NodeEntity entity = datas.get(position);
                Bundle bundle = NodeDetailFragment.createBundle(0, entity, entity.getTotal_reply(), entity.getTotal_score(), entity.isIs_upvote(), entity.isIs_downvote());
                presentFragment(new NodeDetailFragment(bundle));
            }
        });
    }

    private void getData(){
        ApiRequestHelper.personalCommentsOrVotesOrPosts(String.valueOf(UserConfig.getClientUserId()), ++pageNumber, pageCount, pageTimestamp, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.PERSONAL_POSTS.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        PersonalPostsEntity resultEntity = new PersonalPostsEntity();
                        resultEntity = resultEntity.jsonParse(responseString);

                        if (resultEntity.getError() == null) {
                            pageNumber = resultEntity.getPage();
                            pageTimestamp = resultEntity.getTimestamp();

                            ArrayList<NodeEntity> temp = resultEntity.getData();
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
//                            } else {
//                                mEmptyContainer.setVisibility(View.GONE);
//                            }
                        }
                    }
                }, T8UserConfig.getUserId()).execute();
            }
        });
    }
}
