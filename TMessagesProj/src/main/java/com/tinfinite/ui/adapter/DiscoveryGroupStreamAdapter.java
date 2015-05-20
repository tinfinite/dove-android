package com.tinfinite.ui.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.utils.Utils;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BackupImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/4/25.
 */
public class DiscoveryGroupStreamAdapter extends GroupStreamAdapter {
    private DiscoverEntity.Community community;
    private boolean showEmpty = false;
    public DiscoveryGroupStreamAdapter(BaseFragment baseFragment, int chat_id, DiscoverEntity.Community community) {
        super(baseFragment, chat_id);
        this.community = community;
    }

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public int getContentItemCount() {
        return getCursor() == null || getCursor().getCount() == 0 ? 1 : getCursor().getCount();
    }

    public void setCommunity(DiscoverEntity.Community community) {
        this.community = community;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        View view = LayoutInflater.from(baseFragment.getParentActivity()).inflate(R.layout.header_discovery_group, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        if(contentViewType == 1) {
            View view = LayoutInflater.from(baseFragment.getParentActivity()).inflate(R.layout.view_empty_discover_groupstream, parent, false);
            return new EmptyViewHolder(view);
        }
        return super.onCreateContentItemViewHolder(parent, contentViewType);
    }

    @Override
    public int getContentItemViewType(int position) {
        if(getCursor() == null || getCursor().getCount() == 0)
            return 1;
        return super.getContentItemViewType(position);
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, int position) {
        if(getCursor() == null || getCursor().getCount() == 0)
            return;
//        else if(contentViewHolder instanceof EmptyViewHolder) {
//            View view = LayoutInflater.from(baseFragment.getParentActivity()).inflate(R.layout.row_stream, (ViewGroup)(contentViewHolder.itemView), false);
//            contentViewHolder = new ContentViewHolder(view);
//        }
        super.onBindContentItemViewHolder(contentViewHolder, position);
    }

    @Override
    public void onBindHeaderItemViewHolder(RecyclerView.ViewHolder headerViewHolder, int position) {
        HeaderViewHolder viewHolder = (HeaderViewHolder) headerViewHolder;
        viewHolder.imageView.setImage(Utils.getFileLocation(community.getThird_group_image_key()), "50_50", baseFragment.getParentActivity().getResources().getDrawable(R.drawable.default_profile_img_l));
        viewHolder.tv_name.setText(community.getName());
        viewHolder.tv_desc.setText(community.getDescription());
        viewHolder.tv_language.setText(community.getLanguage());
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{

        @InjectView(R.id.header_discover_iv)
        public BackupImageView imageView;
        @InjectView(R.id.header_discover_tv_name)
        public TextView tv_name;
        @InjectView(R.id.header_discover_tv_description)
        public TextView tv_desc;
        @InjectView(R.id.header_discover_tv_language)
        public TextView tv_language;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);

            imageView.setRoundRadius(AndroidUtilities.dp(24));
            tv_language.setVisibility(View.GONE);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder{

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
