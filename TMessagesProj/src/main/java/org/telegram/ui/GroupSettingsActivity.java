package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckProfileCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextMultiValueCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;

/**
 * Created by PanJiafang on 15/4/3.
 */
public class GroupSettingsActivity extends BaseFragment {
    ListView listView;
    private ListAdapter listAdapter;

    private int rowCount = 0;
    private int settingsNotificationsRow;
    private int stickToTopRow;
    private int emptyRow = -1;
    private int emptyTopRow;
    private int forwardNameRow;
    private int forwardDescRow;
    private int footerRow;
    private int introRow = -1;
    private int introDescRow = -1;
    private int languageRow = -1;
    private int groupTypeRow = -1;
    private int groupTypeInfoRow = -1;

    private TLRPC.ChatParticipants info;

    private boolean stickToTop;
    private long stick_to_top_dialog_id;

    private boolean showName = true;
    private boolean showNameGloble;

    private MaterialDialog languageDialog;
    private MaterialDialog groupTypeDialog;
    private MaterialDialog introDialog;

    private LoadingDialog loadingDialog;

    public static Bundle bundleCreate(long id){
        Bundle bundle = new Bundle();
        bundle.putLong("stick_to_top_dialog_id", id);
        return bundle;
    }

    public GroupSettingsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        updateRowsIds();

        return true;
    }

    @Override
    public View createView(final Context context, LayoutInflater inflater) {
        if(fragmentView == null){

            stick_to_top_dialog_id = getArguments().getLong("stick_to_top_dialog_id", 0);

            listAdapter = new ListAdapter(getParentActivity());

            fragmentView = new LinearLayout(getParentActivity());
            fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
            fragmentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            LinearLayout frameLayout = (LinearLayout) fragmentView;

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("GroupSetting", R.string.GroupSetting));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            loadingDialog = new LoadingDialog(getParentActivity());


            listView = new ListView(getParentActivity());
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setVerticalScrollBarEnabled(false);
            AndroidUtilities.setListViewEdgeEffectColor(listView, AvatarDrawable.getProfileBackColorForId(5));
            frameLayout.addView(listView);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP;
            listView.setLayoutParams(layoutParams);

            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    if (getParentActivity() == null) {
                        Log.d("zhaozhen", "parentactivity is null");
                        return;
                    }
                    if (position == settingsNotificationsRow) {
                        Bundle args = new Bundle();
                        int chat_id = info.chat_id;
                        args.putLong("dialog_id", -chat_id);
                        presentFragment(new ProfileNotificationsActivity(args));
                    } else if(position == introRow){
                        if(info != null && info.admin_id == UserConfig.getClientUserId()) {
                            if(introDialog == null) {
                                final EditText et = new EditText(context);
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.leftMargin = AndroidUtilities.dp(24);
                                params.rightMargin = AndroidUtilities.dp(24);
                                et.setLayoutParams(params);
                                et.setGravity(Gravity.LEFT);
                                et.setText(PreferenceUtils.getGroupDesc(info.chat_id));

                                introDialog = new MaterialDialog.Builder(context)
                                        .customView(et, false)
                                        .titleColorRes(android.R.color.black)
                                        .title(R.string.Description)
                                        .positiveColorRes(android.R.color.black)
                                        .positiveText(R.string.Update)
                                        .negativeColorRes(android.R.color.black)
                                        .negativeText(R.string.Cancel)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                super.onPositive(dialog);
                                                String chatname = MessagesController.getInstance().getChat(info.chat_id).title;
                                                final String desc = et.getText().toString();

                                                loadingDialog.show();

                                                ApiRequestHelper.communityUpdateDescription(String.valueOf(UserConfig.getClientUserId()), String.valueOf(info.chat_id), chatname, desc, new ApiRequestHelper.BuildParamsCallBack() {
                                                    @Override
                                                    public void build(RequestParams params) {
                                                        ApiUrlHelper.COMMUNITY_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                                            @Override
                                                            public void onSuccess(String responseString) {
                                                                loadingDialog.dismiss();
                                                                if (responseString.contains("\"result\":true")) {
                                                                    PreferenceUtils.setGroupDesc(info.chat_id, desc);
                                                                    listAdapter.notifyDataSetChanged();
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure() {
                                                                loadingDialog.dismiss();
                                                            }
                                                        }).execute();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                super.onNegative(dialog);
                                            }
                                        })
                                        .build();
                            }
                            if(!introDialog.isShowing())
                                introDialog.show();
                        }
                    } else if (position == languageRow) {
                        if(info != null && info.admin_id == UserConfig.getClientUserId()) {
                            if(languageDialog == null)
                                languageDialog = new MaterialDialog.Builder(getParentActivity())
                                    .title(R.string.Language)
                                    .titleColor(Color.BLACK)
                                    .items(new CharSequence[]{LocaleController.getInstance().sortedLanguages.get(1).name, LocaleController.getInstance().sortedLanguages.get(2).name})
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                            String result = LocaleController.getInstance().sortedLanguages.get(which+1).name;
                                            final String language = result.equals("English") ? "en" : "zh";
                                            String chatname = MessagesController.getInstance().getChat(info.chat_id).title;

                                            loadingDialog.show();

                                            ApiRequestHelper.communityUpdateLanguage(String.valueOf(UserConfig.getClientUserId()), String.valueOf(info.chat_id), chatname, language, new ApiRequestHelper.BuildParamsCallBack() {
                                                @Override
                                                public void build(RequestParams params) {
                                                    ApiUrlHelper.COMMUNITY_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                                        @Override
                                                        public void onSuccess(String responseString) {
                                                            loadingDialog.dismiss();
                                                            if (responseString.contains("\"result\":true")) {
                                                                PreferenceUtils.setGroupLanguage(info.chat_id, language);
                                                                listAdapter.notifyDataSetChanged();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure() {
                                                            loadingDialog.dismiss();
                                                        }
                                                    }).execute();
                                                }
                                            });
                                        }
                                    })
                                    .itemColor(Color.BLACK)
                                    .negativeText(LocaleController.getString("Cancel", R.string.Cancel))
                                    .negativeColor(Color.BLACK)
                                    .build();
                            if(!languageDialog.isShowing())
                                languageDialog.show();
                        }
                    } else if (position == groupTypeRow) {
                        if(info != null && info.admin_id == UserConfig.getClientUserId()) {
                            if(groupTypeDialog == null)
                                groupTypeDialog = new MaterialDialog.Builder(getParentActivity())
                                    .title(R.string.GroupType)
                                    .titleColor(Color.BLACK)
                                    .items(new CharSequence[]{LocaleController.getString("", R.string.GroupTypePublic), LocaleController.getString("", R.string.GroupTypePrivate)})
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, final int which, CharSequence text) {
                                            String chatname = MessagesController.getInstance().getChat(info.chat_id).title;

                                            loadingDialog.show();

                                            ApiRequestHelper.communityUpdateType(String.valueOf(UserConfig.getClientUserId()), String.valueOf(info.chat_id), chatname, String.valueOf(which^0), new ApiRequestHelper.BuildParamsCallBack() {
                                                @Override
                                                public void build(RequestParams params) {
                                                    ApiUrlHelper.COMMUNITY_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                                        @Override
                                                        public void onSuccess(String responseString) {
                                                            loadingDialog.dismiss();
                                                            if (responseString.contains("\"result\":true")) {
                                                                PreferenceUtils.setGroupType(info.chat_id, which == 0);
                                                                listAdapter.notifyDataSetChanged();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure() {
                                                            loadingDialog.dismiss();
                                                        }
                                                    }).execute();
                                                }
                                            });
                                        }
                                    })
                                    .itemColor(Color.BLACK)
                                    .negativeText(LocaleController.getString("Cancel", R.string.Cancel))
                                    .negativeColor(Color.BLACK)
                                    .build();
                            if(!groupTypeDialog.isShowing())
                                groupTypeDialog.show();
                        }
                    } else if (position == stickToTopRow) {
                        if (view instanceof TextCheckProfileCell) {
                            stickToTop = !stickToTop;
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("topdialog", Activity.MODE_PRIVATE);
                            String originDialogs = preferences.getString("stick_to_top_dialogs", "");
                            String updateDialogs = updateTopPreference(originDialogs, String.valueOf(stick_to_top_dialog_id), stickToTop);
                            if (!updateDialogs.equals(originDialogs)) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("stick_to_top_dialogs", updateDialogs);
                                editor.commit();
                                syncStickTopGroup(updateDialogs);
                                MessagesController.getInstance().stickDialogToTop(updateDialogs);
                                MessagesController.getInstance().resortDialogsWithStickToTop();
                            }

                            ((TextCheckProfileCell) view).setChecked(stickToTop);
                        }
                    } else if (position == forwardNameRow) {
                        if(!showNameGloble) {
                            T8Toast.lt(LocaleController.getString("", R.string.GroupSettingForwardNameShowEditFirst));
                            return;
                        }
                        MaterialDialog dialog = new MaterialDialog.Builder(context)
                                .title(LocaleController.getString("GroupSettingForwardNameShow", R.string.GroupSettingForwardNameShow))
                                .items(new CharSequence[] {
                                    LocaleController.getString("", R.string.GroupSettingForwardNameShowAllow),
                                    LocaleController.getString("", R.string.GroupSettingForwardNameShowDisAllow)})
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, final int which, CharSequence text) {
                                        loadingDialog.show();
                                        ApiRequestHelper.anonymousGroupEdit(String.valueOf(UserConfig.getClientUserId()), which^1, new ApiRequestHelper.BuildParamsCallBack() {
                                            @Override
                                            public void build(RequestParams params) {
                                                ApiUrlHelper.ANONYMOUS_GROUP_SYNC.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                                    @Override
                                                    public void onFailure() {
                                                        loadingDialog.dismiss();
                                                    }

                                                    @Override
                                                    public void onSuccess(String responseString) {
                                                        loadingDialog.dismiss();
                                                        if (responseString.contains("\"result\":true")) {
                                                            PreferenceUtils.setChatStatus(info.chat_id, which == 0);
                                                            listAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }, T8UserConfig.getUserId()).execute();
                                            }
                                        });
                                    }
                                })
                                .negativeText(LocaleController.getString("Cancel", R.string.Cancel))
                                .negativeColorRes(android.R.color.black)
                                .build();
                        dialog.show();
                    }
                }
            });

        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }


        return fragmentView;
    }

    public void setChatInfo(TLRPC.ChatParticipants chatParticipants) {
        info = chatParticipants;
    }

    private void updateRowsIds() {
        rowCount = 0;
        emptyTopRow = rowCount++;
        settingsNotificationsRow = rowCount++;
        introRow = rowCount++;
//        introDescRow = rowCount++;
        languageRow = rowCount++;
        groupTypeRow = rowCount++;
        groupTypeInfoRow = rowCount++;
        stickToTopRow = rowCount++;
        forwardNameRow = rowCount++;
        forwardDescRow = rowCount++;
        footerRow = rowCount++;
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return info == null ? 0 :rowCount;
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
        public boolean isEnabled(int position) {
            if(position == emptyRow || position == forwardDescRow || position == footerRow || position == groupTypeInfoRow)
                return false;
            return true;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (i == settingsNotificationsRow) {
                view = new TextCell(mContext, true);
                TextCell textCell = (TextCell) view;
                textCell.setText(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds));
                textCell.showDivider(true);
            } else if ( i == introRow) {
                view = new TextMultiValueCell(mContext, true);
                TextMultiValueCell textCell = (TextMultiValueCell) view;
                String value = PreferenceUtils.getGroupDesc(info.chat_id);
                textCell.setTextAndValue(LocaleController.getString("", R.string.Description), value);
                textCell.showDivider(true);
            } else if ( i == languageRow) {
                view = new TextSettingsCell(mContext);
                TextSettingsCell textCell = (TextSettingsCell) view;
                String language = PreferenceUtils.getGroupLanguage(info.chat_id);
                String value = language.equals("en") ? LocaleController.getInstance().sortedLanguages.get(1).name : LocaleController.getInstance().sortedLanguages.get(2).name;
                textCell.setTextAndValue(LocaleController.getString("", R.string.Language), value, true);
            } else if ( i == groupTypeRow) {
                view = new TextSettingsCell(mContext);
                TextSettingsCell textCell = (TextSettingsCell) view;
                boolean isShownInDiscover = PreferenceUtils.getGroupType(info.chat_id);
                String value = isShownInDiscover ? LocaleController.getString("GroupShowingInDiscover", R.string.GroupTypePublic)
                        : LocaleController.getString("GroupNotShownInDiscover", R.string.GroupTypePrivate);
                textCell.setTextAndValue(LocaleController.getString("GroupShowInDiscover", R.string.GroupType), value, true);
            } else if (i == stickToTopRow) {
                view = new TextCheckProfileCell(mContext, true);
                TextCheckProfileCell cell = (TextCheckProfileCell) view;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("topdialog", Activity.MODE_PRIVATE);
                String originDialogs = preferences.getString("stick_to_top_dialogs", "");
                stickToTop = originDialogs.contains(String.valueOf(stick_to_top_dialog_id)) ? true : false;
                cell.setTextAndCheck(LocaleController.getString("StickToTop", R.string.StickToTop), stickToTop, false);
                cell.showDivider(true);
            } else if (i == emptyRow){
                view = new EmptyCell(mContext);
                EmptyCell cell = (EmptyCell) view;
                cell.setHeight(AndroidUtilities.dp(16));
                cell.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.greydivider));
            } else if (i == emptyTopRow){
                view = new EmptyCell(mContext);
                EmptyCell cell = (EmptyCell) view;
                cell.setHeight(AndroidUtilities.dp(16));
                cell.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.greydivider));
            } else if (i == footerRow){
                view = new EmptyCell(mContext);
                EmptyCell cell = (EmptyCell) view;
                    cell.setHeight(AndroidUtilities.dp(160));
//                if(info != null && info.admin_id == UserConfig.getClientUserId()) {
//                }
//                else {
//                    cell.setHeight(AndroidUtilities.dp(208));
//                }
                cell.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.greydivider_top));
            } else if(i == forwardNameRow){
                view = new TextSettingsCell(mContext);
                TextSettingsCell textCell = (TextSettingsCell) view;
                String value;
                showNameGloble = PreferenceUtils.getGlobleStatus();
                showName = PreferenceUtils.getChatStatus(info.chat_id);
                value = showNameGloble && showName ? LocaleController.getString("", R.string.GroupSettingForwardNameShowAllow) : LocaleController.getString("", R.string.GroupSettingForwardNameShowDisAllow);
                textCell.setTextAndValue(LocaleController.getString("GroupSettingForwardNameShow", R.string.GroupSettingForwardNameShow), value, false);
            } else if(i == forwardDescRow){
                view = new TextInfoPrivacyCell(mContext);
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) view;
                cell.setText(LocaleController.getString("GroupSettingForwardNameDesc", R.string.GroupSettingForwardNameDesc));
                cell.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.greydivider));
            } else if(i == groupTypeInfoRow){
                view = new TextInfoPrivacyCell(mContext);
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) view;
                cell.setText(LocaleController.getString("", R.string.GroupTypeHint));
                cell.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.greydivider));
            } else if(i == introDescRow){
                view = new TextInfoPrivacyCell(mContext);
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) view;
                cell.setText(PreferenceUtils.getGroupDesc(info.chat_id));
                cell.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.greydivider));
            }
            return view;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private String updateTopPreference(String src, String to, boolean top) {
        StringBuilder sb = new StringBuilder(src);
        to = to + ",";

        int index = sb.indexOf(to);
        if (top) {
            if (index != -1) {
                sb.delete(index, index + to.length());
            }
            sb.insert(0, to);
        } else {
            if (index != -1) {
                sb.delete(index, index + to.length());
            }
        }

        return sb.toString();
    }

    public static void syncStickTopGroup(String top_dialogs) {
        ApiRequestHelper.syncStickTopGroup(String.valueOf(UserConfig.getClientUserId()), 1, top_dialogs, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.SYNC_STICK_TOP_GRP.build(params,
                        new ApiHttpClient.DoveHttpResponseHandler() {
                            @Override
                            public void onSuccess(String responseString) {

                            }
                        }, T8UserConfig.getUserId()).execute();
            }
        });
    }
}
