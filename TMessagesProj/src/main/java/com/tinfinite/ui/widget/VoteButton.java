package com.tinfinite.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.provider.PostsController;

import org.apache.http.Header;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;

import butterknife.ButterKnife;

/**
 * Created by zhaozhen on 14/12/3.
 */
public class VoteButton extends FrameLayout implements View.OnClickListener {
    public interface VoteButtonCallBack {
        void onVoted(int score);
    }

    /**
     * 防止用户狂点按钮导致过多请求，当一段时间用户没有再次Vote时则发送最终的赞的结果。
     */
    private static final long POST_VOTE_REQUEST_TIME_LIMIT = 1 * 1000;
    private String mNodePostId, mAuthorId;
    private int mUpVoteCount, mDownVoteCount;
    private int mVoteCount, mOldVoteCount;
    private boolean isUpVote, isOldUpVote;
    private boolean isDownVote, isOldDownVote;
    private ImageView mUpVoteButton, mDownVoteButton;
    private TextView mVoteCountTextView;
    private long mLatestClickTimestamp = 0;
    private boolean holdOnWatching = true;
    private Thread mWatcherTask;
    private VoteButtonCallBack mVoteButtonCallBack;
    private Runnable mWatcherRunnable = new Runnable() {
        @Override
        public void run() {
            while(true) {
                if(!holdOnWatching) {
                    break;
                }

                if(mLatestClickTimestamp !=0 &&
                        (System.currentTimeMillis() - mLatestClickTimestamp > POST_VOTE_REQUEST_TIME_LIMIT)
                        && mOldVoteCount != mVoteCount) {
                    VoteButton.this.post(new Runnable() {
                        @Override
                        public void run() {
                            T8Log.VOTEBTN.i("watch listen the timeout, post request for node post [" + mNodePostId + "]");
                            postRequest();
                        }
                    });
                    break;
                }

                try {
                    // 每隔1s检查一次
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public VoteButton(Context context) {
        super(context);
        init();
    }

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VoteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setNodePostInfo(String node_post_id, String author_id, int upVoteCount, int downVoteCount, boolean isUpVote, boolean isDownVote, VoteButtonCallBack voteButtonCallBack) {
        this.mNodePostId = node_post_id;
        this.mAuthorId = author_id;
        this.mUpVoteCount = upVoteCount;
        this.mDownVoteCount = downVoteCount;
        this.mOldVoteCount = this.mVoteCount = upVoteCount - downVoteCount;
        this.isOldUpVote = this.isUpVote = isUpVote;
        this.isOldDownVote = this.isDownVote = isDownVote;
        this.mVoteButtonCallBack = voteButtonCallBack;
        mVoteCountTextView.setText(String.valueOf(mVoteCount));
        if(isDownVote || isUpVote)
            mVoteCountTextView.setTextColor(getResources().getColor(R.color.dark_blue));
        else
            mVoteCountTextView.setTextColor(getResources().getColor(R.color.theme_t8_font_2));
        updateVoteButtonDrawable();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_vote_button, this);
        mUpVoteButton = ButterKnife.findById(this, R.id.btn_up_vote);
        mDownVoteButton = ButterKnife.findById(this, R.id.btn_down_vote);
        mVoteCountTextView = ButterKnife.findById(this, R.id.tv_vote_count);

        mUpVoteButton.setOnClickListener(this);
        mDownVoteButton.setOnClickListener(this);
        mVoteCountTextView.setText(String.valueOf(mVoteCount));
    }

    public void initViews(){
        isUpVote = false;
        isDownVote = false;

        updateVoteButtonDrawable();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        StringBuffer log = new StringBuffer("vote button dead, ");
        if(mOldVoteCount < mVoteCount) {
            log.append("user up vote the node post [" + mNodePostId + "], ");
        } else if(mOldVoteCount > mVoteCount) {
            log.append("user down vote the node post [" + mNodePostId + "], ");
        } else {
            log.append("user not yet vote the node post [" + mNodePostId + "], ");
        }
        if(mWatcherTask != null && mWatcherTask.isAlive() && holdOnWatching) {
            mWatcherTask.interrupt();
            holdOnWatching = false;
            postRequest();
            log.append(" ---> kill the watcher task");
        } else {
            log.append(" no request need post");
        }
        T8Log.VOTEBTN.i(log.toString());
    }

    private void invokeCallBack() {
        if(mVoteButtonCallBack != null) {
            mVoteButtonCallBack.onVoted(mVoteCount);
        }
    }

    private void postDownRequest() {
        T8Log.VOTEBTN.i("node post [" + mNodePostId + "] down vote.");
        Tracker t = ApplicationLoader.getInstance().getTracker(
                ApplicationLoader.TrackerName.APP_TRACKER);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("信息流")
                .setAction("踩")
                .build());
        ApiRequestHelper.postUpvoteOrDownvote(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.POST_DOWNVOTE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        T8Log.VOTEBTN.d(responseString);
                    }
                }, mNodePostId).execute();
            }
        });
    }

    private void postUpRequest() {
        T8Log.VOTEBTN.i("node post [" + mNodePostId + "] down vote.");
        // Get tracker.
        Tracker t = ApplicationLoader.getInstance().getTracker(
                ApplicationLoader.TrackerName.APP_TRACKER);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("信息流")
                .setAction("顶")
                .build());
        ApiRequestHelper.postUpvoteOrDownvote(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.POST_UPVOTE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onSuccess(String responseString) {
                        T8Log.VOTEBTN.d(responseString);
                    }
                }, mNodePostId).execute();
            }
        });
    }

    private void postRequest() {
        if(mVoteCount < mOldVoteCount) {
            if(isOldUpVote && (mOldVoteCount - mVoteCount == 1)) {
                postUpRequest(); // 以前赞过，且count变化为1，则为取消赞
            } else {
                postDownRequest();
            }
        } else if(mVoteCount > mOldVoteCount) {
            if(isOldDownVote && (mVoteCount - mOldVoteCount == 1)) {
                postDownRequest();// 以前踩过，且count变化为1，则为取消踩
            } else {
                postUpRequest();
            }
        }
    }

    private void refreshVoteCount(boolean upVote) {
       if(upVote) {
           if(isDownVote) {
               mVoteCount += 2;
           } else {
               if(isUpVote) {
                   mVoteCount -= 1;
               } else {
                   mVoteCount += 1;
               }
           }
           isUpVote = !isUpVote;
           isDownVote = false;
       } else {
           if(isUpVote) {
               mVoteCount -= 2;
           } else {
               if(isDownVote) {
                   mVoteCount += 1;
               } else {
                   mVoteCount -= 1;
               }
           }
           isDownVote = !isDownVote;
           isUpVote = false;
       }
        mVoteCountTextView.setText(String.valueOf(mVoteCount));
        if(isDownVote || isUpVote)
            mVoteCountTextView.setTextColor(getResources().getColor(R.color.dark_blue));
        else
            mVoteCountTextView.setTextColor(getResources().getColor(R.color.theme_t8_font_2));

        invokeCallBack();
        PostsController.getInstance().votePost(mNodePostId,  mVoteCount, isUpVote, isDownVote );
    }

    private void updateVoteButtonDrawable() {
        T8Log.VOTEBTN.i("node post [" + mNodePostId + "] is up vote : [" + isUpVote + "] - is down vote : [" + isDownVote + "]");
        if(isUpVote) {
            mUpVoteButton.setImageResource(R.drawable.cbox_up_selected);
            mDownVoteButton.setImageResource(R.drawable.cbox_down_default);
        } else if(isDownVote) {
            mUpVoteButton.setImageResource(R.drawable.cbox_up_default);
            mDownVoteButton.setImageResource(R.drawable.cbox_down_selected);
        } else if(!isDownVote && !isUpVote) {
            mUpVoteButton.setImageResource(R.drawable.cbox_up_default);
            mDownVoteButton.setImageResource(R.drawable.cbox_down_default);
        }
    }

    @Override
    public void onClick(View v) {
//        if (!T8LocalUser.INSTANCE.isLogin()) {
//            FragmentUtil.navigateToInNewActivity((Activity) getContext(), new PassportSigninFragment(), null);
//            return;
//        }
//
        if(mAuthorId.equals(T8UserConfig.getUserId())) {
            if(v == mUpVoteButton) {
                T8Toast.st("不能赞自己发布的信息");
            } else if(v == mDownVoteButton) {
                T8Toast.st("不能踩自己发布的信息");
            }
            return;
        }

        mLatestClickTimestamp = System.currentTimeMillis();

        if(v == mUpVoteButton) {
            refreshVoteCount(true);
        } else if(v == mDownVoteButton) {
            refreshVoteCount(false);
        }

        updateVoteButtonDrawable();

        if(mWatcherTask == null || !mWatcherTask.isAlive()) {
            mWatcherTask = new Thread(mWatcherRunnable);
            mWatcherTask.start();
        }
    }
}
