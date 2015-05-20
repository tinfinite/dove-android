package com.tinfinite.ui.fragment;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

public class MyVotesTabActivity extends BaseFragment implements View.OnClickListener{
    private int currentTab;
    private BaseFragment currentFragment;

    private MyVotedMessagesSubTab messageFragment;
    private PersonalVotedSubTab postFragment;
    private View postView;
    private View messagesView;

    private TextView postTab;
    private TextView messagesTab;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        messageFragment = new MyVotedMessagesSubTab();
        messageFragment.setFragmentDelegate(this);
        postFragment = new PersonalVotedSubTab(null);
        postFragment.setFragmentDelegate(this);

        messageFragment.onFragmentCreate();
        postFragment.onFragmentCreate();
        currentTab = 0;
        currentFragment = messageFragment;

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        messageFragment.setFragmentDelegate(null);
        postFragment.setFragmentDelegate(null);
        messageFragment.onFragmentDestroy();
        postFragment.onFragmentDestroy();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("", R.string.PersonalVoted));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setExtraHeight(AndroidUtilities.dp(50), false);
        View tab = inflater.inflate(R.layout.my_vote_tab, null, false);
        actionBar.addView(tab);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) tab.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = AndroidUtilities.dp(50);
        layoutParams.gravity = Gravity.BOTTOM;
        tab.setLayoutParams(layoutParams);

        postTab = (TextView) tab.findViewById(R.id.post_tab);
        postTab.setOnClickListener(this);
        messagesTab = (TextView) tab.findViewById(R.id.messages_tab);
        messagesTab.setOnClickListener(this);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        postView = postFragment.createView(context, inflater);
        messagesView = messageFragment.createView(context, inflater);

        fragmentView = new FrameLayout(context);
        FrameLayout contentView = (FrameLayout) fragmentView;
        contentView.addView(postView);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) postView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        postView.setLayoutParams(params);

        contentView.addView(messagesView);
        params = (FrameLayout.LayoutParams) messagesView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        messagesView.setLayoutParams(params);

        selectTab(0);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentFragment != null) {
            currentFragment.onResume();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == postTab) {
            selectTab(0);
        } else if (v == messagesTab){
            selectTab(1);
        }
    }

    private void selectTab(int tab) {
        if (tab == 0) {
            currentTab = 0;
            postTab.setSelected(true);
            messagesTab.setSelected(false);
            postView.setVisibility(View.VISIBLE);
            messagesView.setVisibility(View.INVISIBLE);
            postFragment.onResume();
        } else if ( tab == 1) {
            currentTab = 1;
            postTab.setSelected(false);
            messagesTab.setSelected(true);
            postView.setVisibility(View.INVISIBLE);
            messagesView.setVisibility(View.VISIBLE);
            messageFragment.onResume();
        }
    }
}
