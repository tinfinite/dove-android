package com.tinfinite.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.ui.widget.VoteButton;

import org.telegram.android.NotificationCenter;
import org.telegram.messenger.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/28.
 */
public class StreamFooterView extends BaseView {

    @InjectView(R.id.view_stream_footer_layout_reply)
    public RelativeLayout layout_reply;
    @InjectView(R.id.view_stream_footer_tv_reply)
    public TextView tv_reply;
    @InjectView(R.id.view_stream_footer_layout_btns)
    public RelativeLayout layout_btns;
    @InjectView(R.id.view_stream_footer_tv_block)
    public TextView tv_block;
    @InjectView(R.id.view_stream_footer_tv_delete)
    public TextView tv_delete;
    @InjectView(R.id.view_stream_footer_votebtn)
    public VoteButton voteButton;
    @InjectView(R.id.view_stream_footer_divider)
    public TextView tv_divider;

    private boolean canSendNotify = false;

    private ViewClickDelegate delegate;

    public StreamFooterView(Context context) {
        super(context);
        init();
    }

    public StreamFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_footer, null);
        ButterKnife.inject(this, view);
        addView(view);
    }

    public void initView() {
        voteButton.setNodePostInfo("", "", 0, 0, false, false, null);
    }

    public void setContent(final NodeEntity data, final int reply, final int score, final boolean is_upvote, final boolean is_downvote) {
        final String node_id = data.getId();
        final String author_id = data.getAuthor().getId();
//        final int score = data.getTotal_score();
//        final boolean is_upvote = data.isIs_upvote();
//        final boolean is_downvote = data.isIs_downvote();

        voteButton.setNodePostInfo(node_id, author_id, score, 0, is_upvote, is_downvote, new VoteButton.VoteButtonCallBack() {
            @Override
            public void onVoted(int score) {
                if (canSendNotify)
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.postDidupdated, score);
            }
        });

//        int reply = data.getTotal_reply();
        tv_reply.setText(reply == 0 ? "" : String.valueOf(reply));
        if(data.getAuthor().getId().equals(T8UserConfig.getUserId())) {
            tv_delete.setVisibility(VISIBLE);
            tv_block.setVisibility(GONE);
        } else {
            tv_delete.setVisibility(GONE);
            tv_block.setVisibility(VISIBLE);
        }

        layout_reply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(delegate != null)
                    delegate.commentClick();
            }
        });

        layout_btns.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tv_delete.getVisibility() == VISIBLE) {
                    if(delegate != null)
                        delegate.deleteClick(data);
                } else {
                    if(delegate != null)
                        delegate.blockClick(data);
                }
            }
        });
    }

    public void setViewClickDelegate(ViewClickDelegate delegate){
        this.delegate = delegate;
    }

    public void dismissDivider() {
        tv_divider.setVisibility(GONE);
    }

    public void canSendNotify(boolean enable) {
        canSendNotify = enable;
    }

    public interface ViewClickDelegate{
        public void deleteClick(NodeEntity nodeEntity);

        public void commentClick();

        public void blockClick(NodeEntity nodeEntity);
    }
}
