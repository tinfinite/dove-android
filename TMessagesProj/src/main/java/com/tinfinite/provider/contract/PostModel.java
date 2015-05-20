package com.tinfinite.provider.contract;

import android.net.Uri;

import com.tinfinite.provider.T8provider;

public class PostModel {
    public static final String TABLE_NAME           = "post_table";
    public static final String ID                   = "_id";

    public static final String T8_ID                = "post_t8_id";
    public static final String FILTER_ID            = "post_filter_id";
    public static final String AUTHOR_ID            = "post_author_telegram_id";
    public static final String VOTE_SCORE           = "post_vote_score";
    public static final String REPLY_COUNT          = "post_reply_count";
    public static final String IS_UP_VOTE           = "post_is_up_vote";
    public static final String IS_DOWN_VOTE         = "post_is_down_vote";
    public static final String BLOCKED              = "post_blocked";
    public static final String JSON                 = "post_json";

    /*Content URI*/
    public static final Uri CONTENT_URI = Uri.withAppendedPath(T8provider.AUTHORITY_URI, "posts");
    /*CONTRACT*/
    public static final int POST_BLOCKED        = 1;
    public static final int POST_NOT_BLOCKED    = 0;
}
