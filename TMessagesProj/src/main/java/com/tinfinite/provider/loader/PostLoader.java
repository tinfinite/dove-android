/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.tinfinite.provider.loader;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

import com.tinfinite.provider.T8provider;
import com.tinfinite.provider.contract.AuthorModel;
import com.tinfinite.provider.contract.IdsModel;
import com.tinfinite.provider.contract.PostModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Group Member loader. Loads all group members from the given groupId
 */
public final class PostLoader extends CursorLoader {
    /*VIEW_NODE Content URI*/
    public static final Uri CONTENT_URI = Uri.withAppendedPath(T8provider.AUTHORITY_URI, "view_nodes");
    /*CONTRACT*/
    public static final String EXTRA_LOAD_MORE_IDS = "extra_load_more_ids";
    public static final int PAGE_DISPLAY_COUNT = 20;

    public static class PostQuery {
        private static final String[] PROJECTION = new String[] {
                PostModel.JSON,         //POST_JSON_COLUMN = 0
                PostModel.VOTE_SCORE,
                PostModel.IS_UP_VOTE,
                PostModel.IS_DOWN_VOTE,
                PostModel.REPLY_COUNT,

                IdsModel.FILTER_ID,
                IdsModel.ID,
//                IdsModel.POST_ID,
                IdsModel.DISPLAY,

                PostModel.FILTER_ID,
//                PostModel.T8_ID,
//                PostModel.AUTHOR_ID,
//                PostModel.BLOCKED,

//                AuthorModel.T8_ID,
//                AuthorModel.LOCALE,
//                AuthorModel.TELEGRAM_ID,
//                AuthorModel.USERNAME,
//                AuthorModel.FIRST_NAME,
//                AuthorModel.AVATAR_URL,
//                AuthorModel.LAST_NAME,
//                AuthorModel.AVATAR_URL,
                AuthorModel.BLOCKED,
        };


        public static final int POST_JSON_COLUMN            = 0;
        public static final int POST_SCORE_COLUMN           = 1;
        public static final int POST_ISUPVOTE_COLUMN        = 2;
        public static final int POST_ISDOWNVOTE_COLUMN      = 3;
        public static final int POST_REPLY_COUNT_COLUMN     = 4;
    }

    private final int mFiterId;

    /**
     * @return NodeLoader object which can be used in group editor.
     */
    public static PostLoader constructLoaderForNodeQuery(
            Context context, int fiterId) {
        return new PostLoader(context, fiterId, PostQuery.PROJECTION);
    }

    private PostLoader(Context context, int filterId, String[] projection) {
        super(context);
        mFiterId = filterId;
        setUri(createUri());
        setProjection(projection);
        setSelection(createSelection());
        setSelectionArgs(createSelectionArgs());
        setSortOrder(IdsModel.DEFAULT_SORT_ORDER);
    }

    private Uri createUri() {
        return CONTENT_URI;
    }

    private String createSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append(IdsModel.FILTER_ID + "=?");
        selection.append(" AND " + PostModel.FILTER_ID + "=?");
        selection.append(" AND " + AuthorModel.BLOCKED + " is NULL");
        selection.append(" AND " + IdsModel.DISPLAY + "=?");
        return selection.toString();
    }

    private String[] createSelectionArgs() {
        List<String> selectionArgs = new ArrayList<String>();
        selectionArgs.add(String.valueOf(mFiterId));
        selectionArgs.add(String.valueOf(mFiterId));
        selectionArgs.add(String.valueOf(IdsModel.ID_DISPLAYED));
        return selectionArgs.toArray(new String[0]);
    }
}
