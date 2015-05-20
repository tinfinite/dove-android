package com.tinfinite.provider.contract;

import android.net.Uri;

import com.tinfinite.provider.T8provider;

public class IdsModel {
    public static final String TABLE_NAME       = "post_ids";
    public static final String ID               = "_id";

    public static final String FILTER_ID        = "ids_filter_id";
    public static final String POST_ID          = "ids_post_id";
    public static final String DISPLAY          = "ids_display";

    /*Default sort order*/
    public static final String DEFAULT_SORT_ORDER = "_id desc";
    /*Content URI*/
    public static final Uri CONTENT_URI = Uri.withAppendedPath(T8provider.AUTHORITY_URI, "ids");
    /*CONTRACT*/
    public static final int ID_DISPLAYED = 1;
    public static final int ID_NOT_DISPLAYED = 0;
}
