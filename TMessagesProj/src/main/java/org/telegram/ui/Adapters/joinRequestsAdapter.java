/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.JoinRequestEntity;
import com.tinfinite.ui.adapter.BaseRecyclerAdapter;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.StrangerUtils;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessagesController;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.UserControlCell;

import java.util.ArrayList;

public class joinRequestsAdapter extends BaseRecyclerAdapter<JoinRequestEntity.RequestEntity> {

    private LoadingDialog loadingDialog;

    public joinRequestsAdapter(BaseFragment baseFragment, ArrayList<JoinRequestEntity.RequestEntity> datas) {
        super(baseFragment, datas);

        loadingDialog = new LoadingDialog(baseFragment.getParentActivity());
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
        super.onBindContentItemViewHolder(contentViewHolder, position);

        ContentViewHolder viewHolder = (ContentViewHolder) contentViewHolder;

        final JoinRequestEntity.RequestEntity entity = datas.get(position);

        viewHolder.cell.setData(entity);
        viewHolder.cell.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewHolder.cell.setButtonOnClickListener(new UserControlCell.JoinRequestHandler() {
            @Override
            public View.OnClickListener onApprove() {

                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TLRPC.User user = MessagesController.getInstance().getUser(Integer.parseInt(entity.getTelegram_user_id()));
                        if(user == null){
                            StrangerUtils.SearchForStranger(entity.getTelegram_username(), new StrangerUtils.SearchStrangerDelegate() {
                                @Override
                                public void getResult(TLRPC.User user) {
                                    if(user != null){
                                        MessagesController.getInstance().putUser(user, false);

                                        change_status(MessagesController.getInputUser(user), entity, true);
                                    }
                                }
                            });
                        } else {
                            TLRPC.InputUser inputUser = MessagesController.getInputUser(user);
                            change_status(inputUser, entity, true);
                        }
                    }
                };
            }

            @Override
            public View.OnClickListener onIgnore() {
                return new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        change_status(null, entity, false);
                    }
                };
            }
        });
    }

    private void change_status(final TLRPC.InputUser user, final JoinRequestEntity.RequestEntity entity, final boolean isAgree){
        loadingDialog.show();
        if(isAgree){
            if(user == null)
                return;
            TLRPC.TL_messages_addChatUser req = new TLRPC.TL_messages_addChatUser();
            req.chat_id = Integer.parseInt(entity.getTelegram_group_id());
            req.fwd_limit = 50;
            req.user_id = user;

            ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
                @Override
                public void run(final TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (error == null) {
                                change_status_with_server(user, entity, isAgree);
                            } else
                                change_status_with_server(user, entity, false);
                        }
                    });
                }
            }, true, RPCRequest.RPCRequestClassGeneric | RPCRequest.RPCRequestClassFailOnServerErrors);
        } else
            change_status_with_server(user, entity, false);
    }

    private void change_status_with_server(final TLRPC.InputUser user, final JoinRequestEntity.RequestEntity entity, final boolean isAgree){
        ApiRequestHelper.groupApplyChangeStatusParamsAsync(String.valueOf(UserConfig.getClientUserId()), isAgree, new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.GROUP_APPLY_CHANGE_STATUS.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        loadingDialog.dismiss();
                        int position = datas.indexOf(entity);
                        datas.remove(entity);
                        notifyContentItemRemoved(position);
                    }

                    @Override
                    public void onFailure() {
                        super.onFailure();
                        loadingDialog.dismiss();
                    }
                }, entity.getId()).execute();
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        UserControlCell cell = new UserControlCell(baseFragment.getParentActivity(), 16);
        return new ContentViewHolder(cell);
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder{

        public UserControlCell cell;

        public ContentViewHolder(View itemView) {
            super(itemView);

            cell = (UserControlCell) itemView;
        }
    }
}
