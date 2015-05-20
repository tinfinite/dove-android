package com.tinfinite.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tinfinite.entity.NodeEntity;

import org.apache.commons.lang3.StringUtils;
import org.telegram.messenger.R;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/3/25.
 */
public class Forward2StreamAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NodeEntity.ForwardContentEntity> forwardContents;

    public Forward2StreamAdapter(Context context, ArrayList<NodeEntity.ForwardContentEntity> forwardContents) {
        this.context = context;
        this.forwardContents = forwardContents;
    }

    @Override
    public int getCount() {
        return forwardContents == null ? 0 : forwardContents.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static ViewHolder viewHolder;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.row_forward2stream, null);
            viewHolder = new ViewHolder();
            viewHolder.iv = (SimpleDraweeView) convertView.findViewById(R.id.row_forward2stream_iv);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.row_forward2stream_tv_name);
            viewHolder.tv_content = (TextView) convertView.findViewById(R.id.row_forward2stream_tv_content);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        NodeEntity.ForwardContentEntity contentEntity = forwardContents.get(position);
        NodeEntity.ForwardUserEntity userEntity = contentEntity.getUser();
        String username = "";
        if(StringUtils.isEmpty(userEntity.getUsername()))
            username = userEntity.getLast_name()+userEntity.getFirst_name();
        else
            username = userEntity.getUsername();
        viewHolder.tv_name.setText(username);

        int type = contentEntity.getMessagetype();
        if(type == NodeEntity.ForwardContentEntity.MESSAGE_TPYE_TEXT){
            viewHolder.tv_content.setText(contentEntity.getMessagecontent());
            viewHolder.iv.setVisibility(View.GONE);
        } else {
            viewHolder.tv_content.setText(R.string.AttachPhoto);
            viewHolder.iv.setVisibility(View.VISIBLE);
            viewHolder.iv.setImageURI(Uri.parse("file://"+contentEntity.getMessagecontent()));
        }

        return convertView;
    }

    private class ViewHolder{
        SimpleDraweeView iv;
        TextView tv_name;
        TextView tv_content;
    }
}
