/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Components.PagerSlidingTabStrip;

import java.util.ArrayList;

public class MainTabActivity extends BaseFragment  implements NotificationCenter.NotificationCenterDelegate{
    private ActionBarMenuItem passcodeItem;
    private boolean searching = false;
    private boolean searchWas = false;
    private String searchString;
    private static boolean dialogsLoaded = false;
    private static final int passcode_menu_item = 1;
    private ActionBarMenuItem headerItem;
    private final static int new_secret_chat = 3;
    private final static int new_broadcast = 4;
    private final static int join_group = 5;
    private final static int invite = 6;

    private ViewPager pager;
    private PagerSlidingTabStrip pagerTab;
    private ArrayList<View> views = new ArrayList<>();

    private int currentTab;
    private BaseFragment currentFragment;
    private BaseFragment searchFragment;

    private String [] Titles = {
            LocaleController.getString("Groups", R.string.Groups),
            LocaleController.getString("Chats", R.string.Chats)
    };

    SeparateMessagesActivity chatMessagesFragment;
    SeparateMessagesActivity userMessagesFragment;

    public MainTabActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        Bundle chatArgs = new Bundle();
        chatArgs.putInt("dialogShowType", DialogsAdapter.CHAT_DIALOG);
        chatMessagesFragment = new SeparateMessagesActivity(chatArgs);
        chatMessagesFragment.setFragmentDelegate(this);
        Bundle userArgs = new Bundle();
        userArgs.putInt("dialogShowType", DialogsAdapter.USER_DIALOG);
        userMessagesFragment = new SeparateMessagesActivity(userArgs);
        userMessagesFragment.setFragmentDelegate(this);

        chatMessagesFragment.onFragmentCreate();
        userMessagesFragment.onFragmentCreate();

        currentTab = 0;
        currentFragment = chatMessagesFragment;

        if (getArguments() != null) {
//            onlySelect = arguments.getBoolean("onlySelect", false);
//            serverOnly = arguments.getBoolean("serverOnly", false);
//            selectAlertString = arguments.getString("selectAlertString");
//            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
        }

        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogUnreadPostNumDidLoaded);
        }

        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 0, 100, true);
            ContactsController.getInstance().checkInviteText();
            dialogsLoaded = true;
        }

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        views.clear();
        chatMessagesFragment.onFragmentDestroy();
        userMessagesFragment.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogUnreadPostNumDidLoaded);
        }
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        searching = false;
        searchWas = false;

        ActionBarMenu menu = actionBar.createMenu();
        if (searchString == null) {
            passcodeItem = menu.addItem(passcode_menu_item, R.drawable.lock_close);
            updatePasscodeButton();
        }
        ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searching = true;
//                if (messagesListView != null) {
//                    if (searchString != null) {
//                        messagesListView.setEmptyView(progressView);
//                        searchEmptyView.setVisibility(View.INVISIBLE);
//                    } else {
//                        messagesListView.setEmptyView(searchEmptyView);
//                        progressView.setVisibility(View.INVISIBLE);
//                    }
//                    emptyView.setVisibility(View.INVISIBLE);
//                    if (!onlySelect) {
//                        floatingButton.setVisibility(View.GONE);
//                    }
//                }
                searchFragment = currentFragment;
                ((SeparateMessagesActivity)searchFragment).onSearchExpand();
                updatePasscodeButton();
            }

            @Override
            public boolean onSearchCollapse() {
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                searching = false;
                searchWas = false;
//                if (messagesListView != null) {
//                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
//                        searchEmptyView.setVisibility(View.INVISIBLE);
//                        emptyView.setVisibility(View.INVISIBLE);
//                        progressView.setVisibility(View.VISIBLE);
//                        messagesListView.setEmptyView(progressView);
//                    } else {
//                        messagesListView.setEmptyView(emptyView);
//                        searchEmptyView.setVisibility(View.INVISIBLE);
//                        progressView.setVisibility(View.INVISIBLE);
//                    }
//                    if (!onlySelect) {
//                        floatingButton.setVisibility(View.VISIBLE);
//                        floatingHidden = true;
//                        ViewProxy.setTranslationY(floatingButton, AndroidUtilities.dp(100));
//                        hideFloatingButton(false);
//                    }
//                    if (messagesListView.getAdapter() != dialogsAdapter) {
//                        messagesListView.setAdapter(dialogsAdapter);
//                        dialogsAdapter.notifyDataSetChanged();
//                    }
//                }
//                if (dialogsSearchAdapter != null) {
//                    dialogsSearchAdapter.searchDialogs(null, false);
//                }
                ((SeparateMessagesActivity)searchFragment).onSearchCollapse();
                searchFragment = null;
                updatePasscodeButton();
                return true;
            }

            @Override
            public void onTextChanged(EditText editText) {
//                String text = editText.getText().toString();
//                if (text.length() != 0) {
//                    searchWas = true;
//                    if (dialogsSearchAdapter != null) {
//                        messagesListView.setAdapter(dialogsSearchAdapter);
//                        dialogsSearchAdapter.notifyDataSetChanged();
//                    }
//                    if (searchEmptyView != null && messagesListView.getEmptyView() == emptyView) {
//                        messagesListView.setEmptyView(searchEmptyView);
//                        emptyView.setVisibility(View.INVISIBLE);
//                        progressView.setVisibility(View.INVISIBLE);
//                    }
//                }
//                if (dialogsSearchAdapter != null) {
//                    dialogsSearchAdapter.searchDialogs(text, serverOnly);
//                }
                if (searchFragment == null) {
                    ((SeparateMessagesActivity)currentFragment).onTextChanged(editText);
                } else {
                    ((SeparateMessagesActivity)searchFragment).onTextChanged(editText);
                }
            }
        });

            headerItem = menu.addItem(2, R.drawable.ic_ab_other);
            headerItem.addSubItem(new_secret_chat, LocaleController.getString("NewSecretChat", R.string.NewSecretChat), 0);
            headerItem.addSubItem(new_broadcast, LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), 0);
            headerItem.addSubItem(join_group, LocaleController.getString("AddGroup", R.string.AddGroup), 0);
            headerItem.addSubItem(invite, LocaleController.getString("InviteFriends", R.string.InviteFriends), 0);

        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (searchString != null) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        } else {
            actionBar.setBackButtonDrawable(new MenuDrawable());
        }
        actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == passcode_menu_item) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                    } else if (id == new_secret_chat) {
                        Bundle args = new Bundle();
                        args.putBoolean("onlyUsers", true);
                        args.putBoolean("destroyAfterSelect", true);
                        args.putBoolean("createSecretChat", true);
                        presentFragment(new ContactsActivity(args));
                    } else if (id == new_broadcast) {
                        if (!MessagesController.isFeatureEnabled("broadcast_create", MainTabActivity.this)) {
                            return;
                        }
                        Bundle args = new Bundle();
                        args.putBoolean("broadcast", true);
                        presentFragment(new GroupCreateActivity(args));
                    } else if (id == join_group) {
                        GroupQRScanActivity fragment = new GroupQRScanActivity();
                        presentFragment(fragment);
                    } else if (id == invite) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, ContactsController.getInstance().getInviteText());
                            startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
            });

        views.clear();
        views.add(chatMessagesFragment.createView(context, inflater));
        views.add(userMessagesFragment.createView(context, inflater));

        fragmentView = inflater.inflate(R.layout.messages_pager, null, false);
        pager = (ViewPager) fragmentView.findViewById(R.id.pager);
        pager.setAdapter(new MessagePagesAdapter());

        // actionbar tab
        actionBar.setExtraHeight(AndroidUtilities.dp(40), false);
        pagerTab = new PagerSlidingTabStrip(context);
        actionBar.addView(pagerTab);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) pagerTab.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = AndroidUtilities.dp(40);
        layoutParams.gravity = Gravity.BOTTOM;
        pagerTab.setLayoutParams(layoutParams);
        pagerTab.setViewPager(pager);
        pagerTab.setShouldExpand(true);
        pagerTab.setIndicatorColor(0xffffffff);
        pagerTab.setIndicatorHeight(AndroidUtilities.dp(2.0f));
        pagerTab.setUnderlineHeight(AndroidUtilities.dp(2.0f));
        pagerTab.setUnderlineColor(0x00000000);
        pagerTab.setTabBackground(0);
        pagerTab.setTextSize(AndroidUtilities.dp(14));
        pagerTab.setTextColor(0xFFffffff);
        pagerTab.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), Typeface.NORMAL);

        pagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                currentTab = i;
                switch (i) {
                    case 0 :
                        currentFragment = chatMessagesFragment;
                        break;
                    case 1:
                        currentFragment = userMessagesFragment;
                        break;
                }
                currentFragment.onResume();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        if (searchString == null) {
//            dialogsAdapter = new DialogsAdapter(context, serverOnly, dialogShowType);
//            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
//                dialogsAdapter.setOpenedDialogId(openedDialogId);
//            }
        }

        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

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
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        }
}

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    private class MessagePagesAdapter extends PagerAdapter {

        public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
            View localObject = null;
            localObject = views.get(paramInt);
            paramViewGroup.removeView(localObject);
        }

        public int getCount() {
            return views.size();
        }

        public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
            View localObject = null;
            localObject = views.get(paramInt);
            paramViewGroup.addView(localObject);
            return localObject;
        }

        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Titles[position];
        }
    }


}
