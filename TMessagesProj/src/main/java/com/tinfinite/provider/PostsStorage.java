package com.tinfinite.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.tinfinite.entity.NodeEntity;
import com.tinfinite.provider.contract.AuthorModel;
import com.tinfinite.provider.contract.IdsModel;
import com.tinfinite.provider.contract.PostModel;
import com.tinfinite.provider.loader.PostLoader;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;

import java.util.ArrayList;

public class PostsStorage {
    private static final String TAG = "PostsStorage";
    private static final boolean DBG = false;
    private DispatchQueue storageQueue = new DispatchQueue("nodestorageQueue");

    private static volatile PostsStorage Instance = null;
    public static PostsStorage getInstance() {
        PostsStorage localInstance = Instance;
        if (localInstance == null) {
            synchronized (PostsStorage.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PostsStorage();
                }
            }
        }
        return localInstance;
    }

    public PostsStorage() {
        storageQueue.setPriority(Thread.MAX_PRIORITY);
    }

    public DispatchQueue getStorageQueue() {
        return storageQueue;
    }

    public void putPosts(final int chat_id, final ArrayList<NodeEntity> postEntities, final ArrayList<String> postIds) {
        storageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                int postCount;
                postCount = postEntities.size();
                final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
                NodeEntity post;

                for (int i = 0; i < postCount; i++) {
                    post = postEntities.get(i);
                    try {
                        final ArrayList<ContentProviderOperation> postOperations =
                                new ArrayList<ContentProviderOperation>();

                        // Build an assert operation to ensure the node is not already exist
                        // is exist update else insert
                        final ContentProviderOperation.Builder assertBuilder = ContentProviderOperation
                                .newAssertQuery(PostModel.CONTENT_URI);
                        assertBuilder.withSelection(PostModel.T8_ID + "=? AND " + PostModel.FILTER_ID + "=?", new String[]{post.getId(), String.valueOf(chat_id)});
                        assertBuilder.withExpectedCount(0);
                        postOperations.add(assertBuilder.build());

                        // Build an insert operation to add the post
                        final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
                                .newInsert(PostModel.CONTENT_URI);

                        insertBuilder.withValue(PostModel.T8_ID, post.getId());
                        insertBuilder.withValue(PostModel.FILTER_ID, String.valueOf(chat_id));
                        insertBuilder.withValue(PostModel.AUTHOR_ID, post.getAuthor().getTg_user_id());
                        insertBuilder.withValue(PostModel.VOTE_SCORE, post.getTotal_score());
                        insertBuilder.withValue(PostModel.REPLY_COUNT, post.getTotal_reply());
                        insertBuilder.withValue(PostModel.IS_UP_VOTE, post.isIs_upvote() ? 1 : 0);
                        insertBuilder.withValue(PostModel.IS_DOWN_VOTE, post.isIs_downvote() ? 1 : 0);
                        insertBuilder.withValue(PostModel.JSON, post.toString());

                        postOperations.add(insertBuilder.build());
                        for (ContentProviderOperation operation : postOperations) {
                            log(operation.toString());
                        }

                        // Apply batch
                        if (!postOperations.isEmpty()) {
                            if (resolver == null) {
                                log("resolver is null!!");
                            }
                            resolver.applyBatch(T8provider.AUTHORITY, postOperations);
                        }
                    } catch (RemoteException e) {
                        // Something went wrong, bail without success
                        if (DBG) {
                            Log.e(TAG, "Problem persisting user edits for node ID " +
                                    String.valueOf(post.getId()), e);
                        }
                    } catch (OperationApplicationException e) {
                        // The assert could have failed because the node is already added,
                        // update node
                        log("update node ID " + String.valueOf(post.getId()) + e);
                        updatePost(resolver, post, chat_id);
                    }
                }

                // storage postIds
                saveIds(chat_id, postIds);
            }
        });
    }

    private void updatePost(final ContentResolver resolver, final NodeEntity post, final int chat_id) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(PostModel.T8_ID, post.getId());
//        contentValues.put(PostModel.AUTHOR_ID, post.getAuthor().getId());
        contentValues.put(PostModel.VOTE_SCORE, post.getTotal_score());
        contentValues.put(PostModel.REPLY_COUNT, post.getTotal_reply());
        contentValues.put(PostModel.IS_UP_VOTE, post.isIs_upvote() ? "1" : "0");
        contentValues.put(PostModel.IS_DOWN_VOTE, post.isIs_downvote() ? "1" : "0");
        contentValues.put(PostModel.JSON, post.toString());

        resolver.update(PostModel.CONTENT_URI, contentValues, PostModel.T8_ID+ "=?", new String[]{post.getId()});
    }

    private void saveIds(final int filter_id, final ArrayList<String> postIds) {
        storageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                int id_count;
                if (postIds == null) {
                    return;
                }
                id_count = postIds.size();

                final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
                // delete first
                resolver.delete(IdsModel.CONTENT_URI, IdsModel.FILTER_ID + "=?", new String[] {String.valueOf(filter_id)});

                // insert in a batch
                ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

                // for display the new post first, sort by key, so insert from the last one
                // first NodeLoader.PAGE_DISPLAY_COUNT is displayed
                if (id_count <= PostLoader.PAGE_DISPLAY_COUNT) {
                    for (int i = id_count - 1; i >= 0; i--) {
                        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(IdsModel.CONTENT_URI);
                        builder.withValue(IdsModel.FILTER_ID, filter_id);
                        builder.withValue(IdsModel.POST_ID, postIds.get(i));
                        builder.withValue(IdsModel.DISPLAY, IdsModel.ID_DISPLAYED);
                        operationList.add(builder.build());
                    }
                } else {
                    for (int i = id_count - 1; i >= PostLoader.PAGE_DISPLAY_COUNT; i--) {
                        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(IdsModel.CONTENT_URI);
                        builder.withValue(IdsModel.FILTER_ID, filter_id);
                        builder.withValue(IdsModel.POST_ID, postIds.get(i));
                        operationList.add(builder.build());
                    }
                    for (int i = PostLoader.PAGE_DISPLAY_COUNT - 1; i >= 0; i--) {
                        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(IdsModel.CONTENT_URI);
                        builder.withValue(IdsModel.FILTER_ID, filter_id);
                        builder.withValue(IdsModel.POST_ID, postIds.get(i));
                        builder.withValue(IdsModel.DISPLAY, IdsModel.ID_DISPLAYED);
                        operationList.add(builder.build());
                    }
                }

                try {
                    ContentProviderResult[] results = resolver.applyBatch(T8provider.AUTHORITY,
                            operationList);
                } catch (RemoteException e) {
                    log(String.format("%s: %s", e.toString(), e.getMessage()));
                } catch (OperationApplicationException e) {
                    log(String.format("%s: %s", e.toString(), e.getMessage()));
                } finally {
                    operationList.clear();
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postDidLoaded);
                        }
                    });
                }
            }
        });
    }

    public String getMorePostIds(final int filterId, final int page) {
        int offset = page * PostLoader.PAGE_DISPLAY_COUNT;
        StringBuilder limit = new StringBuilder();
        limit.append(offset);
        limit.append(",");
        limit.append(PostLoader.PAGE_DISPLAY_COUNT);

        final Uri IDS_URI_WITH_LIMIT = IdsModel.CONTENT_URI.buildUpon()
                .appendQueryParameter(T8provider.LIMIT_PARAM_KEY, limit.toString()).build();
        log("getMoreNodeIds " + IDS_URI_WITH_LIMIT);

        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
//        Cursor cursor = resolver.query(IDS_URI_WITH_LIMIT, new String[] {IdsModel.POST_ID},
//                IdsModel.FILTER_ID + "=?" + " AND " + IdsModel.DISPLAY+ "=?", new String[] {String.valueOf(filterId), String.valueOf(IdsModel.ID_NOT_DISPLAYED)},
//                IdsModel.DEFAULT_SORT_ORDER);
        Cursor cursor = resolver.query(IDS_URI_WITH_LIMIT, new String[] {IdsModel.POST_ID},
                IdsModel.FILTER_ID + "=?", new String[] {String.valueOf(filterId)},
                IdsModel.DEFAULT_SORT_ORDER);

        if ((cursor == null) || (cursor.getCount() == 0)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try {
            cursor.moveToPosition(-1);
            int nodeIdColumn = cursor.getColumnIndex(IdsModel.POST_ID);

            while(cursor.moveToNext()) {
                sb.append(cursor.getString(nodeIdColumn));
                sb.append(",");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() -1);
            }
        } catch (Exception e) {
            log(e.getStackTrace().toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        String idsToUpdate = sb.toString();

//        ContentValues contentValues = new ContentValues();
//        contentValues.put(IdsModel.DISPLAY, String.valueOf(IdsModel.ID_DISPLAYED));
//
//        if (idsToUpdate != null && idsToUpdate.length() > 0) {
//            String tempStr[] = idsToUpdate.split(",");
//            StringBuilder ids = new StringBuilder();
//
//            for(int i = 0; i < tempStr.length; i++) {
//                if (ids.length() > 0) {
//                    ids.append("\",\"");
//                }
//                ids.append(tempStr[i]);
//            }
//
//            ids.append("\"");
//            ids.insert(0, "\"");
//            log("updateIdsDisplayed ids to update  " + ids.toString());
//
//            if (idsToUpdate.contains(",")) {
//                resolver.update(IdsModel.CONTENT_URI, contentValues,
//                        IdsModel.FILTER_ID+ "=\"" + filterId + "\"" + " AND " + IdsModel.POST_ID + " in (" + ids.toString() +")" , null);
//            } else {
//                resolver.update(IdsModel.CONTENT_URI, contentValues,
//                        IdsModel.FILTER_ID+ "=\"" + filterId + "\"" + " AND " + IdsModel.POST_ID + " =" + ids.toString() , null);
//            }
//        } else {
//            log("no load more ids to update!");
//        }

        return idsToUpdate;
    }

    public void updateIdsDisplayed(final int filterId, final String idsToUpdate, final boolean display) {
        storageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
                ContentValues contentValues = new ContentValues();
                if (display ) {
                    contentValues.put(IdsModel.DISPLAY, String.valueOf(IdsModel.ID_DISPLAYED));
                } else {
                    contentValues.put(IdsModel.DISPLAY, String.valueOf(IdsModel.ID_NOT_DISPLAYED));
                }


                if (idsToUpdate != null && idsToUpdate.length() > 0) {
                    String tempStr[] = idsToUpdate.split(",");
                    StringBuilder ids = new StringBuilder();

                    for(int i = 0; i < tempStr.length; i++) {
                        if (ids.length() > 0) {
                            ids.append("\",\"");
                        }
                        ids.append(tempStr[i]);
                    }

                    ids.append("\"");
                    ids.insert(0, "\"");
                    log("updateIdsDisplayed ids to update  " + ids.toString());

                    if (idsToUpdate.contains(",")) {
                        resolver.update(IdsModel.CONTENT_URI, contentValues,
                                IdsModel.FILTER_ID+ "=\"" + filterId + "\"" + " AND " + IdsModel.POST_ID + " in (" + ids.toString() +")" , null);
                    } else {
                        resolver.update(IdsModel.CONTENT_URI, contentValues,
                                IdsModel.FILTER_ID+ "=\"" + filterId + "\"" + " AND " + IdsModel.POST_ID + " =" + ids.toString() , null);
                    }
                } else {
                    log("no load more ids to update!");
                }
            }
        });
    }

    public void addNewPost(final String filter_id, String post_id, NodeEntity post) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
        post.setId(String.valueOf(post_id));

        ContentValues IdValues = new ContentValues();
        IdValues.put(IdsModel.FILTER_ID, filter_id);
        IdValues.put(IdsModel.POST_ID, post_id);
        IdValues.put(IdsModel.DISPLAY, IdsModel.ID_DISPLAYED);
        resolver.insert(IdsModel.CONTENT_URI, IdValues);

        ContentValues postValues = new ContentValues();
        postValues.put(PostModel.T8_ID, post_id);
        postValues.put(PostModel.FILTER_ID, filter_id);
        postValues.put(PostModel.VOTE_SCORE, post.getTotal_score());
        postValues.put(PostModel.REPLY_COUNT, post.getTotal_reply());
        postValues.put(PostModel.IS_UP_VOTE, post.isIs_upvote() ? "1" : "0");
        postValues.put(PostModel.IS_DOWN_VOTE, post.isIs_downvote() ? "1" : "0");
        postValues.put(PostModel.JSON, post.toString());
        resolver.insert(PostModel.CONTENT_URI, postValues);
    }

    public void delPost(final String post_id) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
        resolver.delete(PostModel.CONTENT_URI, PostModel.T8_ID + "=?", new String[] {post_id});
    }

    public void addBlockUser(final String telegram_user_id) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();

        try {
            final ArrayList<ContentProviderOperation> authorOperations =
                    new ArrayList<ContentProviderOperation>();

            // Build an assert operation to ensure the node is not already exist
            // is exist update else insert
            final ContentProviderOperation.Builder assertBuilder = ContentProviderOperation
                    .newAssertQuery(AuthorModel.CONTENT_URI);
            assertBuilder.withSelection(AuthorModel.TELEGRAM_ID + "=?", new String[]{telegram_user_id});
            assertBuilder.withExpectedCount(0);
            authorOperations.add(assertBuilder.build());

            // Build an insert operation to add the post
            final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
                    .newInsert(AuthorModel.CONTENT_URI);

            insertBuilder.withValue(AuthorModel.TELEGRAM_ID, telegram_user_id);
            insertBuilder.withValue(AuthorModel.BLOCKED, AuthorModel.AUTHOR_BLOCKED);

            authorOperations.add(insertBuilder.build());
            for (ContentProviderOperation operation : authorOperations) {
                log(operation.toString());
            }

            // Apply batch
            if (!authorOperations.isEmpty()) {
                if (resolver == null) {
                    log("resolver is null!!");
                }
                resolver.applyBatch(T8provider.AUTHORITY, authorOperations);
            }
        } catch (RemoteException e) {
            // Something went wrong, bail without success
            if (DBG) {
                Log.e(TAG, "Problem persisting user edits for node ID " +
                        telegram_user_id, e);
            }
        } catch (OperationApplicationException e) {
            // The assert could have failed because the node is already added,
            // update node
            log("update node ID " + telegram_user_id + e);
        }
    }

    public void delBlockUser(final String telegram_user_id) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
        resolver.delete(AuthorModel.CONTENT_URI, AuthorModel.TELEGRAM_ID + "=?", new String[] {telegram_user_id});
    }

    public boolean isBlockUser(final String telegram_user_id) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();

        Cursor cursor = resolver.query(AuthorModel.CONTENT_URI,null,
                AuthorModel.TELEGRAM_ID+ "=? ", new String[] {String.valueOf(telegram_user_id)},
                null);

        if ((cursor == null) || (cursor.getCount() == 0)) {
            return false;
        }
        return true;
    }

    public void votePost(final String post_id, final int score, boolean up_vote, boolean down_vote) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(PostModel.VOTE_SCORE, score);
        contentValues.put(PostModel.IS_UP_VOTE, up_vote ? "1" : "0");
        contentValues.put(PostModel.IS_DOWN_VOTE, down_vote ? "1" : "0");

        resolver.update(PostModel.CONTENT_URI, contentValues, PostModel.T8_ID+ "=?", new String[]{post_id});
    }

    public void updateReplyCount(final String post_id, final int reply) {
        final ContentResolver resolver = ApplicationLoader.applicationContext.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(PostModel.REPLY_COUNT, reply);
        resolver.update(PostModel.CONTENT_URI, contentValues, PostModel.T8_ID+ "=?", new String[]{post_id});
    }

    private void log(String msg) {
        if (DBG) {
            Log.d(TAG, msg);
        }
    }
}
