package com.tinfinite.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tinfinite.provider.contract.AuthorModel;
import com.tinfinite.provider.contract.IdsModel;
import com.tinfinite.provider.contract.PostModel;

import java.util.ArrayList;

public class T8provider extends ContentProvider {
    private static final boolean DBG = false;
    private static final String TAG = "T8provider";
    /*Authority*/
    public static final String AUTHORITY = "com.tinfinite.dove";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    /*CONTRACT*/
    public static final String LIMIT_PARAM_KEY = "limit";

    /*Match Code*/
    public static final int VIEW_POSTS          = 1002;
    public static final int VIEW_POST_ID        = 1003;
    public static final int IDS                 = 1004;
    public static final int IDS_ID              = 1005;
    public static final int POSTS               = 1006;
    public static final int POSTS_ID            = 1007;
    public static final int AUTHOR              = 1008;
    public static final int AUTHOR_ID           = 1009;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "view_nodes",      VIEW_POSTS);
        uriMatcher.addURI(AUTHORITY, "view_nodes/#",    VIEW_POST_ID);
        uriMatcher.addURI(AUTHORITY, "posts",           POSTS);
        uriMatcher.addURI(AUTHORITY, "posts/#",         POSTS_ID);
        uriMatcher.addURI(AUTHORITY, "author",          AUTHOR);
        uriMatcher.addURI(AUTHORITY, "author/#",        AUTHOR_ID);
        uriMatcher.addURI(AUTHORITY, "ids",             IDS);
        uriMatcher.addURI(AUTHORITY, "ids/#",           IDS_ID);
    }

    private static final ProjectionMap viewNodesProjectionMap = ProjectionMap.builder()
            // ids
            .add(IdsModel.ID)
            .add(IdsModel.FILTER_ID)
            .add(IdsModel.POST_ID)
            .add(IdsModel.DISPLAY)

            .add(PostModel.T8_ID)
            .add(PostModel.FILTER_ID)
            .add(PostModel.AUTHOR_ID)
            .add(PostModel.VOTE_SCORE)
            .add(PostModel.REPLY_COUNT)
            .add(PostModel.IS_UP_VOTE)
            .add(PostModel.IS_DOWN_VOTE)
            .add(PostModel.BLOCKED)
            .add(PostModel.JSON)

            .add(AuthorModel.T8_ID)
            .add(AuthorModel.LOCALE)
            .add(AuthorModel.TELEGRAM_ID)
            .add(AuthorModel.USERNAME)
            .add(AuthorModel.FIRST_NAME)
            .add(AuthorModel.LAST_NAME)
            .add(AuthorModel.AVATAR_URL)
            .add(AuthorModel.BLOCKED)
            .build();

    private static final ProjectionMap postsProjectionMap = ProjectionMap.builder()
            .add(PostModel.T8_ID)
            .build();

    private static final ProjectionMap authorProjectionMap = ProjectionMap.builder()
            .add(AuthorModel.T8_ID)
            .add(AuthorModel.TELEGRAM_ID)
            .build();

    private static final ProjectionMap idsProjectionMap = ProjectionMap.builder()
            .add(IdsModel.POST_ID)
            .add(IdsModel.FILTER_ID)
            .add(IdsModel.DISPLAY)
            .build();

    private ContentResolver resolver = null;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        resolver = context.getContentResolver();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        String limit = getLimit(uri);
        final int match = uriMatcher.match(uri);

        switch (match) {
            case VIEW_POSTS:
                sqlBuilder.setTables(DatabaseHelper.Views.VIEW_NODE);
                sqlBuilder.setProjectionMap(viewNodesProjectionMap);
                break;
            case VIEW_POST_ID:
                String view_node_id = uri.getPathSegments().get(1);
                sqlBuilder.setTables(DatabaseHelper.Views.VIEW_NODE);
                sqlBuilder.setProjectionMap(viewNodesProjectionMap);
                sqlBuilder.appendWhere(PostModel.ID+ "=" + view_node_id);
                break;
            case POSTS:
                sqlBuilder.setTables(PostModel.TABLE_NAME);
                sqlBuilder.setProjectionMap(postsProjectionMap);
                break;
            case POSTS_ID:
                String post_id = uri.getPathSegments().get(1);
                sqlBuilder.setTables(PostModel.TABLE_NAME);
                sqlBuilder.setProjectionMap(postsProjectionMap);
                sqlBuilder.appendWhere(PostModel.ID+ "=" + post_id);
                break;
            case AUTHOR:
                sqlBuilder.setTables(AuthorModel.TABLE_NAME);
                sqlBuilder.setProjectionMap(authorProjectionMap);
                break;
            case AUTHOR_ID:
                String author_id = uri.getPathSegments().get(1);
                sqlBuilder.setTables(AuthorModel.TABLE_NAME);
                sqlBuilder.setProjectionMap(authorProjectionMap);
                sqlBuilder.appendWhere(AuthorModel.ID+ "=" + author_id);
                break;
            case IDS:
                sqlBuilder.setTables(IdsModel.TABLE_NAME);
                sqlBuilder.setProjectionMap(idsProjectionMap);
                break;
            case IDS_ID:
                String ids_id= uri.getPathSegments().get(1);
                sqlBuilder.setTables(IdsModel.TABLE_NAME);
                sqlBuilder.setProjectionMap(idsProjectionMap);
                sqlBuilder.appendWhere(IdsModel.ID+ "=" + ids_id);
                break;
        }

        Cursor cursor = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder, limit);
        cursor.setNotificationUri(resolver, AUTHORITY_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = 0;
        final SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
        log("insert values "+ values.toString());

        final int match = uriMatcher.match(uri);
        switch (match) {
            case POSTS:
                id = db.insert(PostModel.TABLE_NAME, PostModel.ID, values);
                if(id < 0) {
                    throw new SQLiteException("Unable to insert " + values + " for " + uri);
                }
                break;
            case IDS:
                id = db.insert(IdsModel.TABLE_NAME, IdsModel.ID, values);
                if(id < 0) {
                    throw new SQLiteException("Unable to insert " + values + " for " + uri);
                }
                break;
            case AUTHOR:
                id = db.insert(AuthorModel.TABLE_NAME, AuthorModel.ID, values);
                if(id < 0) {
                    throw new SQLiteException("Unable to insert " + values + " for " + uri);
                }
                break;
            default:
                break;
        }

        Uri newUri = ContentUris.withAppendedId(uri, id);
        resolver.notifyChange(AUTHORITY_URI, null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
        int count = 0;

        switch(uriMatcher.match(uri)) {
            case IDS: {
                count = db.delete(IdsModel.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case POSTS:
                count = db.delete(PostModel.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                count = db.delete(AuthorModel.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }

        resolver.notifyChange(AUTHORITY_URI, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
        int count = 0;
        log("update values " + values.toString());

        switch(uriMatcher.match(uri)) {
            case POSTS: {
                count = db.update(PostModel.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case POSTS_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.update(PostModel.TABLE_NAME, values, PostModel.ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""), selectionArgs);
                break;
            }
            case AUTHOR: {
                count = db.update(AuthorModel.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case AUTHOR_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.update(AuthorModel.TABLE_NAME, values, AuthorModel.ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""), selectionArgs);
                break;
            }
            case IDS: {
                count = db.update(IdsModel.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case IDS_ID: {
                String id = uri.getPathSegments().get(1);
                count = db.update(IdsModel.TABLE_NAME, values, IdsModel.ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " and (" + selection + ')' : ""), selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }

        resolver.notifyChange(AUTHORITY_URI, null);
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
        db.beginTransaction();
        try {
            int numOperations = operations.size();
            ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
                db.yieldIfContendedSafely();
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /*
        final Uri IDS_URI_WITH_LIMIT = IdsModle.CONTENT_URI.buildUpon()
            .appendQueryParameter(T8provider.LIMIT_PARAM_KEY, String.valueOf(limit)).build();
    * */
    private String getLimit(Uri uri) {
        String limitParam = getQueryParameter(uri, T8provider.LIMIT_PARAM_KEY);
        if (limitParam == null) {
            return null;
        }
//        // Make sure that the limit is a non-negative integer.
//        try {
//            int l = Integer.parseInt(limitParam);
//            if (l < 0) {
//                log("Invalid limit parameter: " + limitParam);
//                return null;
//            }
//            return String.valueOf(l);
//
//        } catch (NumberFormatException ex) {
//            log("Invalid limit parameter: " + limitParam);
//            return null;
//        }

        return limitParam;
    }

    static String getQueryParameter(Uri uri, String parameter) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return null;
        }

        int queryLength = query.length();
        int parameterLength = parameter.length();

        String value;
        int index = 0;
        while (true) {
            index = query.indexOf(parameter, index);
            if (index == -1) {
                return null;
            }

            // Should match against the whole parameter instead of its suffix.
            // e.g. The parameter "param" must not be found in "some_param=val".
            if (index > 0) {
                char prevChar = query.charAt(index - 1);
                if (prevChar != '?' && prevChar != '&') {
                    // With "some_param=val1&param=val2", we should find second "param" occurrence.
                    index += parameterLength;
                    continue;
                }
            }

            index += parameterLength;

            if (queryLength == index) {
                return null;
            }

            if (query.charAt(index) == '=') {
                index++;
                break;
            }
        }

        int ampIndex = query.indexOf('&', index);
        if (ampIndex == -1) {
            value = query.substring(index);
        } else {
            value = query.substring(index, ampIndex);
        }

        return Uri.decode(value);
    }

    private void log(String s) {
        if (DBG) {
            Log.d(TAG, s);
        }
    }

}
