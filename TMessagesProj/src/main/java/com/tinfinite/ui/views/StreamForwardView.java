package com.tinfinite.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.ui.widget.LinkTextView;
import com.tinfinite.utils.StrangerUtils;
import com.tinfinite.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/28.
 */
public class StreamForwardView extends BaseView {

    @InjectView(R.id.viewstream_forward_layout_comment)
    public LinearLayout layout_comment;
    @InjectView(R.id.view_stream_forward_tv_comment)
    public TextView tv_comment;

    @InjectView(R.id.view_stream_forward_layout_content_1)
    public LinearLayout layout_content_1;
    @InjectView(R.id.view_stream_forward_tv_author_1)
    public TextView tv_author_1;
    @InjectView(R.id.view_stream_forward_tv_content_1)
    public TextView tv_content_1;

    @InjectView(R.id.view_stream_forward_layout_content_2)
    public LinearLayout layout_content_2;
    @InjectView(R.id.view_stream_forward_tv_author_2)
    public TextView tv_author_2;
    @InjectView(R.id.view_stream_forward_tv_content_2)
    public TextView tv_content_2;

    @InjectView(R.id.view_stream_forward_tv_content_more)
    public TextView tv_content_more;

    @InjectView(R.id.view_stream_forward_tv_forwardby)
    public TextView tv_forwardby;

    @InjectView(R.id.view_stream_forward_layout_forward)
    public LinearLayout layout_forward;
    @InjectView(R.id.view_stream_forward_layout_forwardby)
    public LinearLayout layout_forwardby;

    @InjectView(R.id.view_stream_forward_imageview)
    public StreamImageView imageView;

    @InjectView(R.id.view_stream_forward_urlthumbview)
    public StreamUrlThumbView urlThumbView;

    private String url;


    private NodeEntity.ForwardNodeEntity forwardNode;

    private ArrayList<NodeEntity.ForwardContentEntity> texts;
    private ArrayList<NodeEntity.ForwardContentEntity> images;

    public StreamForwardView(Context context) {
        super(context);
        init();
    }

    public StreamForwardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamForwardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_forward, this, false);
        ButterKnife.inject(this, view);
        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setContent(int chat_id, NodeEntity.ForwardNodeEntity forwardNode, NodeEntity.AuthorEntity authorEntity){
        this.forwardNode = forwardNode;

        String comment = forwardNode.getComment();

        if(StringUtils.isEmpty(comment)){//如果无评论，显示forwardby
            layout_comment.setVisibility(GONE);

            layout_forward.setBackgroundColor(getResources().getColor(R.color.transparent));

            if(chat_id <= 0)
                layout_forwardby.setVisibility(GONE);
            else
                layout_forwardby.setVisibility(GONE);
            setForwarby(tv_forwardby, authorEntity);
        } else {
            layout_comment.setVisibility(VISIBLE);
            tv_comment.setText(comment);
            tv_comment.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

            layout_forward.setBackgroundResource(R.drawable.stream_forword_selector);
//            layout_forward.setBackgroundColor(getResources().getColor(R.color.forward_bg_with_comment));

            layout_forwardby.setVisibility(GONE);
        }

        ArrayList<NodeEntity.ForwardContentEntity> datas = forwardNode.getContent();
        texts = new ArrayList<>();
        images = new ArrayList<>();
        url = null;
        for(NodeEntity.ForwardContentEntity contentEntity : datas){
            if(contentEntity.getMessagetype() == NodeEntity.ForwardContentEntity.MESSAGE_TPYE_TEXT) {
                texts.add(contentEntity);
                url = Utils.getUrlFromText(contentEntity.getMessagecontent());
            }
            else
                images.add(contentEntity);
        }

        //如果文本内容太多，显示省略号
        int size = texts.size();
        NodeEntity.ForwardContentEntity contentEntity;
        switch (size){
            case 0:
                layout_content_1.setVisibility(GONE);
                layout_content_2.setVisibility(GONE);
                tv_content_more.setVisibility(GONE);
                break;
            case 1:
                contentEntity = texts.get(0);
                layout_content_1.setVisibility(VISIBLE);
                getContentAuthor(tv_author_1, contentEntity);
                tv_content_1.setText(contentEntity.getMessagecontent());
                tv_content_1.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

                layout_content_2.setVisibility(GONE);
                tv_content_more.setVisibility(GONE);
                break;
            case 2:
                contentEntity = texts.get(0);

                layout_content_1.setVisibility(VISIBLE);
                layout_content_2.setVisibility(VISIBLE);

                getContentAuthor(tv_author_1, contentEntity);
                tv_content_1.setText(contentEntity.getMessagecontent());
                tv_content_1.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

                contentEntity = texts.get(1);
                getContentAuthor(tv_author_2, contentEntity);
                tv_content_2.setText(contentEntity.getMessagecontent());
                tv_content_2.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

                tv_content_more.setVisibility(GONE);
                break;
            default:  //如果多于2条文本展示
                contentEntity = texts.get(1);

                layout_content_1.setVisibility(VISIBLE);
                layout_content_2.setVisibility(VISIBLE);

                getContentAuthor(tv_author_2, contentEntity);
                tv_content_2.setText(contentEntity.getMessagecontent());
                tv_content_2.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

                contentEntity = texts.get(0);
                getContentAuthor(tv_author_1, contentEntity);
                tv_content_1.setText(contentEntity.getMessagecontent());
                tv_content_1.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

                tv_content_more.setVisibility(VISIBLE);
        }

        //处理图片
        if(images.size() == 0){
            imageView.setVisibility(GONE);
        } else {
            imageView.setVisibility(VISIBLE);

            ArrayList<String> img_urls = new ArrayList<>();
            for(NodeEntity.ForwardContentEntity content : images){
                img_urls.add(content.getMessagecontent());
            }

            imageView.setContent(img_urls);
            imageView.setBaseFragment(baseFragment);
        }

        //处理转发的链接
//        if(!StringUtils.isEmpty(url))
//            urlThumbView.setContent(url, new StreamUrlThumbView.LoadUrlDelegate() {
//                @Override
//                public void loadResult(String url) {
//                    if(url == null)
//                        urlThumbView.setVisibility(GONE);
//                    else
//                        urlThumbView.setVisibility(VISIBLE);
//                }
//            });
//        else
            urlThumbView.setVisibility(GONE);
    }

    private void setForwarby(TextView tv, final NodeEntity.AuthorEntity author){
        final String username = author.getUsername();
        String name = author.getLast_name() + author.getFirst_name();
        if(StringUtils.isEmpty(username)){
            tv.setText(name);
            tv.setTextColor(getResources().getColor(R.color.dark_gray));
        } else {
            tv.setText(username);
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                    T8Log.PAN_JIA_FANG.d("clikc the forwardby and baseFragment is "+baseFragment);
                    TLRPC.User user = MessagesController.getInstance().getUser(Integer.parseInt(author.getTg_user_id()));
                    if (user == null) {
                        if(loadingDialog != null)
                            loadingDialog.show();
                        StrangerUtils.SearchForStranger(username, new StrangerUtils.SearchStrangerDelegate() {
                            @Override
                            public void getResult(TLRPC.User user) {
                                if (loadingDialog != null)
                                    loadingDialog.dismiss();
                                if (user != null) {
                                    Bundle args = new Bundle();
                                    args.putInt("user_id", user.id);
                                    baseFragment.presentFragment(new ProfileActivity(args));
                                }
                            }
                        });
                    } else {
                        Bundle args = new Bundle();
                        args.putInt("user_id", Integer.parseInt(author.getTg_user_id()));
                        baseFragment.presentFragment(new ProfileActivity(args));
                    }
                }
            });
        }
    }

    private String getContentAuthor(TextView tv, final NodeEntity.ForwardContentEntity contentEntity){
        String username = contentEntity.getUser().getUsername();
            if(contentEntity.getUser().isAnonymous()) {
                tv.setTextColor(Color.BLACK);
                tv.setOnClickListener(null);
            }
            else {
                tv.setTextColor(getResources().getColor(R.color.dark_blue));
                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        T8Log.PAN_JIA_FANG.d("clikc the tv_name and baseFragment is "+baseFragment);
                        String user_id = contentEntity.getUser().getUser_id();
                        String user_name = contentEntity.getUser().getUsername();
                        TLRPC.User user = MessagesController.getInstance().getUser(Integer.parseInt(user_id));
                        if (user == null) {
                            if(loadingDialog != null)
                                loadingDialog.show();
                            StrangerUtils.SearchForStranger(user_name, new StrangerUtils.SearchStrangerDelegate() {
                                @Override
                                public void getResult(TLRPC.User user) {
                                    if (loadingDialog != null)
                                        loadingDialog.dismiss();
                                    if (user != null) {
                                        Bundle args = new Bundle();
                                        args.putInt("user_id", user.id);
                                        baseFragment.presentFragment(new ProfileActivity(args));
                                    }
                                }
                            });
                        } else {
                            Bundle args = new Bundle();
                            args.putInt("user_id", Integer.parseInt(user_id));
                            baseFragment.presentFragment(new ProfileActivity(args));
                        }
                    }
                });
            }
        tv.setText("@"+username);
        return username;
    }
}
