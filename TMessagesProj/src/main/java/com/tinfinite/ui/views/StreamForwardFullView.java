package com.tinfinite.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
public class StreamForwardFullView extends BaseView {

    @InjectView(R.id.viewstream_forward_layout_comment)
    public LinearLayout layout_comment;
    @InjectView(R.id.view_stream_forward_tv_comment)
    public TextView tv_comment;

    @InjectView(R.id.view_stream_forward_tv_forwardby)
    public TextView tv_forwardby;

    @InjectView(R.id.view_stream_forward_layout_forward)
    public LinearLayout layout_forward;
    @InjectView(R.id.view_stream_forward_layout_forwardby)
    public LinearLayout layout_forwardby;

    private String url;


    private NodeEntity.ForwardNodeEntity forwardNode;
    private ArrayList<NodeEntity.ForwardContentEntity> datas;
    private ContentAdapter adapter;

    public StreamForwardFullView(Context context) {
        super(context);
        init();
    }

    public StreamForwardFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamForwardFullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_forward_full, this, false);
        ButterKnife.inject(this, view);
        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setContent(int chat_id, NodeEntity.ForwardNodeEntity forwardNode, NodeEntity.AuthorEntity authorEntity){
        this.forwardNode = forwardNode;

        String comment = forwardNode.getComment();

        if(StringUtils.isEmpty(comment)){//如果无评论，显示forwardby
            layout_comment.setVisibility(GONE);

            layout_forward.setBackgroundColor(getResources().getColor(R.color.white));

            if(chat_id <= 0)
                layout_forwardby.setVisibility(VISIBLE);
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

        datas = forwardNode.getContent();

        if(datas.size() > 0) {
            adapter = new ContentAdapter();

            layout_forward.removeAllViews();
            for(int i = 0; i < datas.size(); i++){
                layout_forward.addView(adapter.getView(i, null, layout_forward));
            }
        }

//        texts = new ArrayList<>();
//        images = new ArrayList<>();
//        url = null;
//        for(NodeEntity.ForwardContentEntity contentEntity : datas){
//            if(contentEntity.getMessagetype() == NodeEntity.ForwardContentEntity.MESSAGE_TPYE_TEXT) {
//                texts.add(contentEntity);
//                url = Utils.getUrlFromText(contentEntity.getMessagecontent());
//            }
//            else
//                images.add(contentEntity);
//        }

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
                    T8Log.PAN_JIA_FANG.d("click forwardby");
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

    private class ContentAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return forwardNode == null ? 0 : datas == null ? 0 : datas.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        ViewHoler viewHoler;
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_stream_forward_full, parent, false);
                viewHoler = new ViewHoler();
                viewHoler.tv_name = (TextView) convertView.findViewById(R.id.row_stream_forward_full_name);
                viewHoler.tv_content = (TextView) convertView.findViewById(R.id.row_stream_forward_full_content);
                viewHoler.imageView = (StreamImageView) convertView.findViewById(R.id.row_stream_forward_full_iv);
                viewHoler.urlThumbView = (StreamUrlThumbView) convertView.findViewById(R.id.row_stream_forward_full_url);
                convertView.setTag(viewHoler);
            } else
                viewHoler = (ViewHoler) convertView.getTag();

            NodeEntity.ForwardContentEntity contentEntity = datas.get(position);

            getContentAuthor(viewHoler.tv_name, contentEntity);

            int type = contentEntity.getMessagetype();
            if(type == NodeEntity.ForwardContentEntity.MESSAGE_TPYE_IMAGE){
                viewHoler.tv_content.setVisibility(GONE);
                viewHoler.urlThumbView.setVisibility(GONE);

                viewHoler.imageView.setVisibility(VISIBLE);

                ArrayList<String> urls = new ArrayList<>();
                urls.add(contentEntity.getMessagecontent());

                viewHoler.imageView.setContent(urls);
                viewHoler.imageView.setBaseFragment(baseFragment);
            } else{
                viewHoler.imageView.setVisibility(GONE);
                viewHoler.tv_content.setVisibility(VISIBLE);

                String content = contentEntity.getMessagecontent();

                viewHoler.tv_content.setText(content);
                viewHoler.tv_content.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());

                String url = Utils.getUrlFromText(content);
//                if(url != null)
//                    viewHoler.urlThumbView.setContent(url, new StreamUrlThumbView.LoadUrlDelegate() {
//                        @Override
//                        public void loadResult(String url) {
//                            if(url == null)
//                                viewHoler.urlThumbView.setVisibility(GONE);
//                            else
//                                viewHoler.urlThumbView.setVisibility(VISIBLE);
//                        }
//                    });
//                else
                    viewHoler.urlThumbView.setVisibility(GONE);
            }

            return convertView;
        }

        private class ViewHoler{
            TextView tv_name;
            TextView tv_content;
            StreamImageView imageView;
            StreamUrlThumbView urlThumbView;
        }
    }
}
