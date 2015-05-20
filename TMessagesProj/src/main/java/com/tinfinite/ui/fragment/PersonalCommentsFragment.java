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
import com.tinfinite.entity.PersonalCommentsEntity;
import com.tinfinite.ui.adapter.BaseRecyclerAdapter;
import com.tinfinite.ui.adapter.PersonalCommentsAdapter;
import com.tinfinite.utils.PreferenceUtils;

import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/4/20.
 */
public class PersonalCommentsFragment extends BaseRecycleViewFragment<PersonalCommentsEntity.PersonalCommentEntity> {

    private ArrayList<PersonalCommentsEntity.PersonalCommentEntity> datas;

    public PersonalCommentsFragment(Bundle args) {
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
        actionBar.setTitle(LocaleController.getString("", R.string.PersonalComments));
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
        t.setScreenName("个人评论消息");
        t.send(new HitBuilders.AppViewBuilder().build());

        datas = new ArrayList<>();

        mListAdapter = new PersonalCommentsAdapter(this, datas);
        mListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                T8Log.PAN_JIA_FANG.d(""+position);
                String id = datas.get(position).getPost().getId();
                Bundle bundle = NodeDetailFragment.createBundle(id);
                presentFragment(new NodeDetailFragment(bundle));
            }
        });
    }

    private void getData(){
        ApiRequestHelper.personalCommentsOrVotesOrPosts(String.valueOf(UserConfig.getClientUserId()), ++pageNumber, pageCount, pageTimestamp, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.PERSONAL_COMMENTS.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        PersonalCommentsEntity commentsEntity = new PersonalCommentsEntity();
                        commentsEntity = commentsEntity.jsonParse(responseString);

                        if (commentsEntity.getError() == null) {
                            pageNumber = commentsEntity.getPage();
                            pageTimestamp = commentsEntity.getTimestamp();

                            ArrayList<PersonalCommentsEntity.PersonalCommentEntity> temp = commentsEntity.getData();
                            if (pageNumber > 1) {
                                appendItemsData(temp);
                            } else {
                                refreshNewItemsData(temp);
                                PreferenceUtils.setUnReadComments(0);
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
