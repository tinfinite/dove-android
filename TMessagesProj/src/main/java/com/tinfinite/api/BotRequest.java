package com.tinfinite.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.api.ApiHttpClient;

import org.telegram.messenger.UserConfig;

import java.util.Locale;

/**
 * Created by caiying on 11/21/14.
 */
public class BotRequest {

    private static RequestParams getBotRequestParams(){
        RequestParams requestParams = new RequestParams();
        requestParams.put("client_id", UserConfig.getClientUserId());
        requestParams.put("phone", UserConfig.getPhoneNumberE164());
        requestParams.put("locale", Locale.getDefault().getLanguage());
        requestParams.put("platform", "android");
        requestParams.put("app", "dove");

        T8Log.PAN_JIA_FANG.d(requestParams.toString());

        return requestParams;
    }
    /**
     * 特殊的网络请求，Server地址不同
     * @param responseHandler
     * @return
     */
    public static void buildDot(ResponseHandlerInterface responseHandler){
        String path = new StringBuffer().append("http://").append("csbot.tinfinite.com/bot/newbie").append("/").toString();
        ApiHttpClient.INSTANCE.post(path, getBotRequestParams(), responseHandler, false);
    }

}
