package com.tinfinite.utils;

import com.tinfinite.android.sdk.T8Toast;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.R;
import org.telegram.messenger.RPCRequest;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/4/13.
 */
public class StrangerUtils {
    public interface SearchStrangerDelegate{
        public void getResult(TLRPC.User user);
    }

    public static void SearchForStranger(final String user_name, final SearchStrangerDelegate delegate){
        TLRPC.TL_contacts_search req = new TLRPC.TL_contacts_search();
        req.q = user_name;
        req.limit = 50;
        ConnectionsManager.getInstance().performRpc(req, new RPCRequest.RPCRequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_contacts_found res = (TLRPC.TL_contacts_found) response;
                            ArrayList<TLRPC.User> users = res.users;
                            if (users != null && users.size() > 0) {
                                for (TLRPC.User user : users) {
                                    if (user.username.equals(user_name)) {
                                        boolean result = MessagesController.getInstance().putUser(user, false);
                                        if (result) {
                                            if(delegate != null)
                                                delegate.getResult(user);
                                        } else {
                                            T8Toast.lt(LocaleController.getString("StrangerNotFind", R.string.StrangerNotFind));
                                            if(delegate != null)
                                                delegate.getResult(null);
                                        }
                                    }
                                }
                            }
                        } else {
                            T8Toast.lt(LocaleController.getString("StrangerNotFind", R.string.StrangerNotFind));
                            if(delegate != null)
                                delegate.getResult(null);
                        }
                    }
                });
            }
        }, true, RPCRequest.RPCRequestClassGeneric | RPCRequest.RPCRequestClassFailOnServerErrors);
    }
}
