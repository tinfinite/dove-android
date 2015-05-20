/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.EmptyCell;

public class DrawerLayoutAdapter extends BaseAdapter {

//    public static int PROFILE_POSITION = 0;
//    public static int EMPTY_POSITION = 1;
//    public static int NEW_GROUP = 2;
//    public static int NEW_SECRET_CHAT = 3;
//    public static int NEW_BROADCAST = 4;
//    public static int JOIN_GROUP = 5;
//    public static int DISCOVERY = 6;
//    public static int DIVIDER_POSITION = 7;
//    public static int CONTACTS = 8;
//    public static int INVITE = 9;
//    public static int SETTING = 10;
//    public static int FAQ = 11;

    public static int PROFILE_POSITION = 0;
    public static int EMPTY_POSITION = 1;
    public static int PUBLIC_STREAM = 2;
    public static int DISCOVER_GROUP = 3;
    public static int NEW_GROUP = 4;
    public static int DIVIDER_POSITION = 5;
    public static int CONTACTS = 6;
    public static int SETTING = 7;

    private Context mContext;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return !(i == EMPTY_POSITION || i == DIVIDER_POSITION);
    }

    @Override
    public int getCount() {
//        return UserConfig.isClientActivated() ? 11 : 0;
        return UserConfig.isClientActivated() ? 8 : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
            ((DrawerProfileCell) view).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, 8);
            }
        } else if (type == 2) {
            if (view == null) {
                view = new DividerCell(mContext);
            }
        } else if (type == 3) {
            if (view == null) {
                view = new DrawerActionCell(mContext);
            }
            DrawerActionCell actionCell = (DrawerActionCell) view;
//            if (i == NEW_GROUP) {
//                actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
//            } else if (i == NEW_SECRET_CHAT) {
//                actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
//            } else if (i == NEW_BROADCAST) {
//                actionCell.setTextAndIcon(LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), R.drawable.menu_broadcast);
//            } else if (i == JOIN_GROUP) {
//                actionCell.setTextAndIcon(LocaleController.getString("AddGroup", R.string.AddGroup), R.drawable.menu_scan);
//            } else if (i == DISCOVERY) {
//                actionCell.setTextAndIcon(LocaleController.getString("Discover", R.string.Discover), R.drawable.menu_discover);
//            } else if (i == CONTACTS) {
//                actionCell.setTextAndIcon(LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts);
//            } else if (i == INVITE) {
//                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
//            } else if (i == SETTING) {
//                actionCell.setTextAndIcon(LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings);
//            }

            if (i == PUBLIC_STREAM) {
                boolean showBadge = false;
                if (MessagesController.getInstance().dialogUnreadPost.containsKey(String.valueOf(-1))) {
                    long unreadPostCount = MessagesController.getInstance().dialogUnreadPost.get(String.valueOf(-1));
                    if (unreadPostCount > 0) {
                        showBadge = true;
                    }
                }
                actionCell.setTextAndIcon(LocaleController.getString("PublicStream", R.string.PublicStream), R.drawable.menu_public_stream);
                actionCell.showBadgeView(showBadge);
            } else if (i == DISCOVER_GROUP) {
                actionCell.setTextAndIcon(LocaleController.getString("DiscoverGroup", R.string.DiscoverGroup), R.drawable.menu_discover);
            } else if (i == NEW_GROUP) {
                actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
            } else if (i == CONTACTS) {
                actionCell.setTextAndIcon(LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts);
            } else if (i == SETTING) {
                actionCell.setTextAndIcon(LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings);
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (i == PROFILE_POSITION) {
            return 0;
        } else if (i == EMPTY_POSITION) {
            return 1;
        } else if (i == DIVIDER_POSITION) {
            return 2;
        }
        return 3;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return !UserConfig.isClientActivated();
    }
}
