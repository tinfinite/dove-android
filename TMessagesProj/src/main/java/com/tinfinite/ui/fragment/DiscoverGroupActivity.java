package com.tinfinite.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.ui.adapter.BaseRecyclerAdapter;
import com.tinfinite.ui.adapter.DiscoverGroupAdatper;

import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.GroupQRScanActivity;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/2/15.
 */
public class DiscoverGroupActivity extends BaseRecycleViewFragment<DiscoverEntity.Community> {

    private ArrayList<DiscoverEntity.Community> datas;
    private View emptyView;

    public DiscoverGroupActivity(Bundle args) {
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
        actionBar.setTitle(LocaleController.getString("DiscoverGroup", R.string.DiscoverGroup));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if(id == 1){
                    GroupQRScanActivity fragment = new GroupQRScanActivity();
                    presentFragment(fragment);
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(1, R.drawable.menu_scan_w);

        //代码监测
        Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
        t.setScreenName("发现群组");
        t.send(new HitBuilders.AppViewBuilder().build());

        //用于无数据时，给予用户提示
        emptyView = LayoutInflater.from(getParentActivity()).inflate(R.layout.view_empty_discover_group, null);
        mEmptyContainer.addView(emptyView);

        datas = new ArrayList<>();
        mListAdapter = new DiscoverGroupAdatper(this, datas);
        mListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DiscoverEntity.Community community = datas.get(position);
                if(community != null) {
                    presentFragment(new DiscoveryGroupStreamFragment(
                            DiscoveryGroupStreamFragment.createBundle(Integer.valueOf(community.getThird_group_id()), community)));
                }
            }
        });
    }

    private void getData(){
        ApiRequestHelper.communityListsParamsAsync(String.valueOf(UserConfig.getClientUserId()), ++pageNumber, pageCount, pageTimestamp, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.COMMUNITY_PULBIC_GET.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                    }

                    @Override
                    public void onSuccess(String s) {
                        DiscoverEntity entity = new DiscoverEntity();
                        entity = entity.jsonParse(s);

                        if(entity.getError() == null) {

                            pageNumber = entity.getPage();
                            pageTimestamp = entity.getTimestamp();

                            ArrayList<DiscoverEntity.Community>  recommend = entity.getData().getRecommend();
                            ArrayList<DiscoverEntity.Community> list = entity.getData().getList();

                            if (pageNumber > 1) {
                                appendItemsData(list);
                            } else {
                                if (recommend != null) {
                                    ((DiscoverGroupAdatper)mListAdapter).addHeaderList(recommend);
                                }
                                refreshNewItemsData(list);
                            }

                            if (list == null || list.size() < pageCount) {
                                hasNoMoreData();
                            }

                            //用于无数据时，给予用户提示
                            if (mListAdapter.getContentItemCount() == 0) {
                                mEmptyContainer.setVisibility(View.VISIBLE);
                            } else {
                                mEmptyContainer.setVisibility(View.GONE);
                            }
                        } else {
                            T8Toast.lt(entity.getError().getMessage());
                        }
                    }
                }).execute();
            }
        });
    }
}
