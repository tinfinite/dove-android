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

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessagesController;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.LoadingCell;

public class DialogsAdapter extends BaseFragmentAdapter {
    // deove add start
    public static final int CHAT_DIALOG = 1;
    public static final int USER_DIALOG = 2;
    public static final int ALL_DIALOG  = 0;
    private int adapterType = ALL_DIALOG;
    // deove add end

    private Context mContext;
    private boolean serverOnly;
    private long openedDialogId;
    private int currentCount;

    public DialogsAdapter(Context context, boolean onlyFromServer) {
        mContext = context;
        serverOnly = onlyFromServer;
    }

    public DialogsAdapter(Context context, boolean onlyFromServer, int adapterType) {
        mContext = context;
        serverOnly = onlyFromServer;
        this.adapterType = adapterType;
    }

    public void setAdapterType(int type) {
        adapterType = type;
        notifyDataSetChanged();
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    public boolean isDataSetChanged() {
        int current = currentCount;
        return current != getCount();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        int count;
        if (serverOnly) {
            if (adapterType == CHAT_DIALOG) {
                count = MessagesController.getInstance().chat_dialogsServerOnly.size();
            } else if (adapterType == USER_DIALOG) {
                count = MessagesController.getInstance().user_dialogsServerOnly.size();
            } else {
                count = MessagesController.getInstance().dialogsServerOnly.size();
            }
        } else {
            if (adapterType == CHAT_DIALOG) {
                count = MessagesController.getInstance().chat_dialogs.size();
            } else if (adapterType == USER_DIALOG) {
                count = MessagesController.getInstance().user_dialogs.size();
            } else {
                count = MessagesController.getInstance().dialogs.size();
            }
        }

        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return 0;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        currentCount = count;
        return count;
    }

    @Override
    public TLRPC.TL_dialog getItem(int i) {
        if (serverOnly) {
            if (i < 0 || i >= MessagesController.getInstance().dialogsServerOnly.size()) {
                return null;
            }
            if (adapterType == CHAT_DIALOG) {
                return MessagesController.getInstance().chat_dialogsServerOnly.get(i);
            } else if (adapterType == USER_DIALOG) {
                return MessagesController.getInstance().user_dialogsServerOnly.get(i);
            } else {
                return MessagesController.getInstance().dialogsServerOnly.get(i);
            }
        } else {
            if (i < 0 || i >= MessagesController.getInstance().dialogs.size()) {
                return null;
            }
            if (adapterType == CHAT_DIALOG) {
                return MessagesController.getInstance().chat_dialogs.get(i);
            } else if (adapterType == USER_DIALOG) {
                return MessagesController.getInstance().user_dialogs.get(i);
            } else {
                return MessagesController.getInstance().dialogs.get(i);
            }
        }
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
        if (type == 1) {
            if (view == null) {
                view = new LoadingCell(mContext);
            }
        } else if (type == 0) {
            if (view == null) {
                view = new DialogCell(mContext);
            }
            if (view instanceof DialogCell) { //TODO finally i need to find this crash
                ((DialogCell) view).useSeparator = (i != getCount() - 1);
                TLRPC.TL_dialog dialog = null;

                if (serverOnly) {
                    if (adapterType == CHAT_DIALOG) {
                        dialog = MessagesController.getInstance().chat_dialogsServerOnly.get(i);
                    } else if (adapterType == USER_DIALOG) {
                        dialog = MessagesController.getInstance().user_dialogsServerOnly.get(i);
                    } else {
                        dialog = MessagesController.getInstance().dialogsServerOnly.get(i);
                    }
                } else {
                    if (adapterType == CHAT_DIALOG) {
                        dialog = MessagesController.getInstance().chat_dialogs.get(i);
                    } else if (adapterType == USER_DIALOG) {
                        dialog = MessagesController.getInstance().user_dialogs.get(i);
                    } else {
                        dialog = MessagesController.getInstance().dialogs.get(i);

                    }
                    if (AndroidUtilities.isTablet()) {
                        if (dialog.id == openedDialogId) {
                            view.setBackgroundColor(0x0f000000);
                        } else {
                            view.setBackgroundColor(0);
                        }
                    }
                }

                if (adapterType == CHAT_DIALOG || adapterType == ALL_DIALOG) {
                    long unreadPostCount = 0;
                    if (MessagesController.getInstance().dialogUnreadPost.containsKey(String.valueOf(-dialog.id))) {
                        unreadPostCount = MessagesController.getInstance().dialogUnreadPost.get(String.valueOf(-dialog.id));
                    }
                    ((DialogCell) view).setDialog(dialog, i, unreadPostCount, serverOnly);
                } else {
                    ((DialogCell) view).setDialog(dialog, i, 0, serverOnly);
                }
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (serverOnly) {
            if (adapterType == CHAT_DIALOG) {
                if (i == MessagesController.getInstance().chat_dialogsServerOnly.size()) {
                    return 1;
                }
            } else if (adapterType == USER_DIALOG) {
                if (i == MessagesController.getInstance().user_dialogsServerOnly.size()) {
                    return 1;
                }
            }
        } else {
            if (adapterType == CHAT_DIALOG) {
                if (i == MessagesController.getInstance().chat_dialogs.size()) {
                    return 1;
                }
            } else if (adapterType == USER_DIALOG) {
                if (i == MessagesController.getInstance().user_dialogs.size()) {
                    return 1;
                }
            }
        }

        if (serverOnly && i == MessagesController.getInstance().dialogsServerOnly.size() || !serverOnly && i == MessagesController.getInstance().dialogs.size()) {
            return 1;
        }

        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        int count;


        if (serverOnly) {
            if (adapterType == CHAT_DIALOG) {
                count = MessagesController.getInstance().chat_dialogsServerOnly.size();
            } else if (adapterType == USER_DIALOG) {
                count = MessagesController.getInstance().user_dialogsServerOnly.size();
            } else {
                count = MessagesController.getInstance().dialogsServerOnly.size();
            }
        } else {
            if (adapterType == CHAT_DIALOG) {
                count = MessagesController.getInstance().chat_dialogs.size();
            } else if (adapterType == USER_DIALOG) {
                count = MessagesController.getInstance().user_dialogs.size();
            } else {
                count = MessagesController.getInstance().dialogs.size();
            }
        }

        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return true;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        return count == 0;
    }
}
