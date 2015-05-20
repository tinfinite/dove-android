/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package com.tinfinite.provider;

import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.StreamResultEntity;
import com.tinfinite.provider.loader.PostLoader;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessagesStorage;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;

import java.util.ArrayList;

public class PostsController {
    private static volatile PostsController Instance = null;
    public static PostsController getInstance() {
        PostsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (PostsController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PostsController();
                }
            }
        }
        return localInstance;
    }

    public PostsController() {
        MessagesStorage.getInstance();
    }

    public void loadPost(final int filter_id, final int page) {
        // sort_type 0:最新 1：最热
        int sort_type;
        if (filter_id == -1) {
            sort_type = 1;
        } else {
            sort_type = 0;
        }

        if (page <= 0) {
            ApiRequestHelper.postQuery(String.valueOf(UserConfig.getClientUserId()), filter_id, sort_type, new ApiRequestHelper.BuildParamsCallBack() {
                @Override
                public void build(RequestParams params) {
                    ApiUrlHelper.POST_QUERY.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                        @Override
                        public void onFailure() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postDidLoaded);
                        }

                        @Override
                        public void onSuccess(String responseString) {
                            processLoadedPost(filter_id, responseString);
                        }
                    }).execute();
                }
            });
        } else { // load more
            Utilities.stageQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    final String postIds = PostsStorage.getInstance().getMorePostIds(filter_id, page);
                    T8Log.ZHAO_ZHEN.d("load more post postIds: " + postIds);
                    if (postIds != null) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ApiRequestHelper.postQueryByIDs(String.valueOf(UserConfig.getClientUserId()), postIds, new ApiRequestHelper.BuildParamsCallBack() {
                                    @Override
                                    public void build(RequestParams params) {
                                        ApiUrlHelper.POST_QUERYBYIDS.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                            @Override
                                            public void onFailure() {
                                                PostsStorage.getInstance().updateIdsDisplayed(filter_id, postIds,false);
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsEndReached);
                                            }

                                            @Override
                                            public void onSuccess(String responseString) {
                                                processLoadedPost(filter_id, responseString);
                                                PostsStorage.getInstance().updateIdsDisplayed(filter_id, postIds,true);

                                                int num = postIds.split(",").length;
                                                if (num < PostLoader.PAGE_DISPLAY_COUNT) {
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsEndReached);
                                                }
                                            }
                                        }).execute();
                                    }
                                });
                            }
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsEndReached);
                            }
                        });
                    }
                }
            });
        }
    }

    public void processLoadedPost(final int filter_id, final String postJson) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                ArrayList<NodeEntity> nodeEntities = null;
                ArrayList<String> postIds = null;
                if (postJson != null && postJson.length() > 0) {
                    StreamResultEntity resultEntity = new StreamResultEntity();
                    resultEntity = resultEntity.jsonParse(postJson);
                    if (resultEntity.getError() != null) {
                        return;
                    }

                    nodeEntities = resultEntity.getData();
                    postIds = resultEntity.getIds();
                    if (nodeEntities == null) {
                        return;
                    }
                    PostsStorage.getInstance().putPosts(filter_id, nodeEntities, postIds);
                }
            }
        });
    }

    public void addNewPost(final String filterId,final String post_id, final NodeEntity entity) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PostsStorage.getInstance().addNewPost(filterId, post_id, entity);
            }
        });
    }

    public void delPost(final String post_id) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PostsStorage.getInstance().delPost(post_id);
            }
        });
    }

    public void addBlockUser(final String telegram_user_id) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PostsStorage.getInstance().addBlockUser(telegram_user_id);
            }
        });
    }

    public void delBlockUser(final String telegram_user_id) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PostsStorage.getInstance().delBlockUser(telegram_user_id);
            }
        });
    }

    public void votePost(final String post_id, final int score, final boolean up_vote, final boolean down_vote) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PostsStorage.getInstance().votePost(post_id, score, up_vote, down_vote);
            }
        });
    }

    public void updateReplyCount(final String post_id, final int reply) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                PostsStorage.getInstance().updateReplyCount(post_id, reply);
            }
        });
    }
}
