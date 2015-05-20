package com.tinfinite.ui.views;

import android.content.Context;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tinfinite.entity.NodeEntity;
import com.tinfinite.ui.widget.LinkTextView;

import org.apache.commons.lang3.StringUtils;
import org.telegram.messenger.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/29.
 */
public class StreamPostView extends BaseView{

    @InjectView(R.id.view_stream_post_tv_comment)
    public TextView tv_content;
    @InjectView(R.id.view_stream_post_imageview)
    public StreamImageView imageView;
    @InjectView(R.id.view_stream_post_urlthumbview)
    public StreamUrlThumbView urlThumbView;

    public StreamPostView(Context context) {
        super(context);
        init();
    }

    public StreamPostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamPostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_post, this, false);
        ButterKnife.inject(this, view);
        addView(view);
    }

    public void setContent(NodeEntity.PostNodeEntity postNodeEntity){
        String text = postNodeEntity.getText();
        String image = postNodeEntity.getImage();
        String url = postNodeEntity.getUrl();

        if(StringUtils.isEmpty(text))
            tv_content.setVisibility(GONE);
        else {
            tv_content.setVisibility(VISIBLE);
            tv_content.setText(text);
            tv_content.setMovementMethod(LinkTextView.LinkTextViewMovementMethod.getInstance());
        }

        if(StringUtils.isEmpty(image))
            imageView.setVisibility(GONE);
        else {
            imageView.setBaseFragment(baseFragment);
            imageView.setVisibility(VISIBLE);
            String[] urlStrings = image.split(",");
            ArrayList<String> urlArrays = new ArrayList<>();
            for(String urlString : urlStrings){
                urlArrays.add(urlString);
            }
            imageView.setContent(urlArrays);
            imageView.setBaseFragment(baseFragment);
        }

        if(!StringUtils.isEmpty(url))
            urlThumbView.setContent(url, postNodeEntity.getUrl_title(), postNodeEntity.getUrl_image(), postNodeEntity.getUrl_description());
        else {
            urlThumbView.setVisibility(GONE);
        }
    }
}
