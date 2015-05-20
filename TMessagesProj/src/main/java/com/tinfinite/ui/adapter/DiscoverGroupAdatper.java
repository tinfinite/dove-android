package com.tinfinite.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tinfinite.entity.DiscoverEntity;
import com.tinfinite.ui.fragment.DiscoveryGroupStreamFragment;
import com.tinfinite.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BackupImageView;

import java.util.ArrayList;

/**
 * Created by PanJiafang on 15/3/28.
 */
public class DiscoverGroupAdatper extends BaseRecyclerAdapter<DiscoverEntity.Community>{
    private ArrayList<DiscoverEntity.Community> headerList = new ArrayList<>();

    public DiscoverGroupAdatper(BaseFragment context, ArrayList datas) {
        super(context, datas);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_discover, null);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder contentViewHolder, final int position) {
        ContentViewHolder viewHolder = (ContentViewHolder) contentViewHolder;

        DiscoverEntity.Community entity = datas.get(position);
        viewHolder.tv_name.setText(entity.getName());
        viewHolder.tv_description.setText(entity.getDescription());
//        viewHolder.iv.setUrl(entity.getImage());
        String thirdkey = entity.getThird_group_image_key();
        viewHolder.iv.setImage(Utils.getFileLocation(thirdkey), "50_50", context.getResources().getDrawable((R.drawable.default_profile_img_l)));
        String language = entity.getLanguage();
        if(!StringUtils.isEmpty(language)){
            language = language.equals("en") ? LocaleController.getInstance().sortedLanguages.get(1).name : LocaleController.getInstance().sortedLanguages.get(2).name;
            if(!language.equals(LocaleController.getCurrentLanguageName())){
                viewHolder.tv_language.setText(language);
            } else {
                viewHolder.tv_language.setText("");
            }
        } else {
            viewHolder.tv_language.setText("");
        }

        viewHolder.tv_points.setText(String.valueOf(entity.getPoints()));
        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClickListener != null)
                    itemClickListener.onItemClick(v, position);
            }
        });

    }

    private class ContentViewHolder extends RecyclerView.ViewHolder{
        public View container;
        public ImageView btn;
        public BackupImageView iv;
        public TextView tv_name;
        public TextView tv_language;
        public TextView tv_points;
        public TextView tv_description;

        public ContentViewHolder(final View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.discover_rl);
            btn = (ImageView) itemView.findViewById(R.id.item_discover_btn);
            iv = (BackupImageView) itemView.findViewById(R.id.item_discover_iv);
            iv.setRoundRadius(AndroidUtilities.dp(24));
            tv_name = (TextView) itemView.findViewById(R.id.item_discover_tv_name);
            tv_language = (TextView) itemView.findViewById(R.id.item_discover_tv_language);
            tv_points = (TextView) itemView.findViewById(R.id.item_discover_tv_points);
            tv_description = (TextView) itemView.findViewById(R.id.item_discover_tv_description);
        }
    }

    // recommend group, in discover group
    public void addHeaderList(ArrayList<DiscoverEntity.Community> paramArrayList) {
        headerList = paramArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        if (headerViewType == 0 ) {
            View section = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_section_discover, null);
            return new SectionViewHolder(section);
        }

        return onCreateContentItemViewHolder(parent, headerViewType);
    }

    @Override
    public int getHeaderItemCount() {
        return headerList.size() > 0 ? headerList.size() + 2 : 0;
    }

    @Override
    public void onBindHeaderItemViewHolder(RecyclerView.ViewHolder headerViewHolder, final int position) {
        if (position == 0) {
            SectionViewHolder viewHolder = (SectionViewHolder) headerViewHolder;
            viewHolder.section.setText(LocaleController.getString("Recommended", R.string.Recommended));
            return;
        } else if (position == headerList.size() + 1) {
            SectionViewHolder viewHolder = (SectionViewHolder) headerViewHolder;
            viewHolder.section.setText(LocaleController.getString("MostUpvoted", R.string.MostUpvoted));
            return;
        }

        ContentViewHolder viewHolder = (ContentViewHolder) headerViewHolder;

        DiscoverEntity.Community entity = headerList.get(position - 1);
        viewHolder.tv_name.setText(entity.getName());
        viewHolder.tv_description.setText(entity.getDescription());
//        viewHolder.iv.setUrl(entity.getImage());
        String thirdkey = entity.getThird_group_image_key();
        viewHolder.iv.setImage(Utils.getFileLocation(thirdkey), "50_50", context.getResources().getDrawable((R.drawable.default_profile_img_l)));
        String language = entity.getLanguage();
        if(!StringUtils.isEmpty(language)){
            language = language.equals("en") ? LocaleController.getInstance().sortedLanguages.get(1).name : LocaleController.getInstance().sortedLanguages.get(2).name;
            if(!language.equals(LocaleController.getCurrentLanguageName())){
                viewHolder.tv_language.setText(language);
            } else {
                viewHolder.tv_language.setText("");
            }
        } else {
            viewHolder.tv_language.setText("");
        }

        viewHolder.tv_points.setText(String.valueOf(entity.getPoints()));
        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0 || position == headerList.size() + 2)
                    return;
                DiscoverEntity.Community community = headerList.get(position - 1);
                if (community != null) {
                    baseFragment.presentFragment(new DiscoveryGroupStreamFragment(
                            DiscoveryGroupStreamFragment.createBundle(Integer.valueOf(community.getThird_group_id()), community)));
                }
            }
        });
    }

    @Override
    public int getHeaderItemViewType(int position) {
        if (position == 0  || position == headerList.size() + 1) {
            // section view type
            return 0;
        }

        return 1;
    }

    private class SectionViewHolder extends RecyclerView.ViewHolder{
        public TextView section;

        public SectionViewHolder(final View itemView) {
            super(itemView);

            section = (TextView) itemView.findViewById(R.id.section);
        }
    }
}
