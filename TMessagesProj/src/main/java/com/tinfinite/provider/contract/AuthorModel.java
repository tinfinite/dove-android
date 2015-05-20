package com.tinfinite.provider.contract;

import android.net.Uri;

import com.tinfinite.provider.T8provider;


public class AuthorModel {
    public static final String TABLE_NAME       = "author_table";
    public static final String ID               = "_id";

    public static final String T8_ID            = "author_t8_id";
    public static final String LOCALE           = "author_locale";
    public static final String TELEGRAM_ID      = "author_telegram_id";
    public static final String USERNAME         = "author_username";
    public static final String FIRST_NAME       = "author_first_name";
    public static final String LAST_NAME        = "author_last_name";
    public static final String AVATAR_URL       = "author_avatar_url";
    public static final String BLOCKED          = "author_blocked";
    /*
    **** not used now
    public static final String GENDER           = "author_gender";
    public static final String RELATION         = "author_relation";
    public static final String DISTANCE         = "author_distance";
    **/

    /*Content URI*/
    public static final Uri CONTENT_URI = Uri.withAppendedPath(T8provider.AUTHORITY_URI, "author");

    // contract
    public static final int AUTHOR_BLOCKED      = 1;
    public static final int AUTHOR_NOT_BLOCKED  = 0;

}
