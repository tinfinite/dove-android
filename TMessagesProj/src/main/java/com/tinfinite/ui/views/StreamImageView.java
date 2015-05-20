package com.tinfinite.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.DraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.ui.fragment.T8PhotoViewer;
import com.tinfinite.utils.Utils;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/28.
 */
public class StreamImageView extends BaseView {

    @InjectView(R.id.view_stream_image_iv)
    public SimpleDraweeView iv;
    @InjectView(R.id.view_stream_image_gridview)
    public GridView gridView;

    private ArrayList<String> images;

    public StreamImageView(Context context) {
        super(context);
        init();
    }

    public StreamImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_image, this, false);
        ButterKnife.inject(this, view);
        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setContent(final ArrayList<String> images){
        this.images = images;
        int size = images.size();
        if(size == 1){
            gridView.setVisibility(GONE);
            iv.setVisibility(VISIBLE);
            String image_url = images.get(0);
            int width = Utils.getImageWidthFromUrl(image_url);
            int height = Utils.getImageHeightFromUrl(image_url);
            int maxWidth = AndroidUtilities.getRealScreenSize().x - AndroidUtilities.dp(32);
            if(width != 0 && height != 0) {
                if (width > height) {
                    height = maxWidth * height / width;
                    width = maxWidth;
                } else {
                    width = maxWidth * width / height;
                    height = maxWidth;
                }
                iv.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
            } else {
                iv.setLayoutParams(new RelativeLayout.LayoutParams(maxWidth, maxWidth));
            }
            Uri uri = Uri.parse(image_url + "?imageView2/2/w/640");
            iv.setImageURI(uri);
            if(image_url.endsWith(".gif")) {
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(ApplicationLoader.applicationContext.getResources());
                builder.setOverlay(ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.overlay_gif));
                iv.setHierarchy(builder.build());
            } else {
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(ApplicationLoader.applicationContext.getResources());
                iv.setHierarchy(builder.build());
            }
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                    T8Log.PAN_JIA_FANG.d("click the image");
                    T8PhotoViewer.getInstance().setParentActivity(baseFragment.getParentActivity());
                    final ArrayList<Object> arrayList = new ArrayList<>();
                    for (String image : images) {
                        arrayList.add(new MediaController.PhotoEntry(0, 0, 0, image.endsWith(".gif") ? image : image+"?imageView2/2/w/640", 0, false));
                    }

                    T8PhotoViewer.getInstance().openPhotoForSelect(arrayList, 0, 2, new T8PhotoViewer.PhotoViewerProvider() {
                        @Override
                        public T8PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
                            int coords[] = new int[2];
                            iv.getLocationInWindow(coords);
                            T8PhotoViewer.PlaceProviderObject object = new T8PhotoViewer.PlaceProviderObject();
                            object.viewX = coords[0];
                            object.viewY = coords[1] - AndroidUtilities.statusBarHeight;

                            iv.setDrawingCacheEnabled(true);
                            Bitmap cacheBmp = iv.getDrawingCache();
                            object.thumb  = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
                            iv.destroyDrawingCache();

                            object.width = iv.getWidth();
                            object.height = iv.getHeight();

                            return object;
                        }

                        @Override
                        public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
                            return null;
                        }

                        @Override
                        public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {

                        }

                        @Override
                        public void willHidePhotoViewer() {

                        }

                        @Override
                        public boolean isPhotoChecked(int index) {
                            return false;
                        }

                        @Override
                        public void setPhotoChecked(int index) {

                        }

                        @Override
                        public void cancelButtonPressed() {

                        }

                        @Override
                        public void sendButtonPressed(int index) {

                        }

                        @Override
                        public int getSelectedCount() {
                            return 0;
                        }

                        @Override
                        public void onQRCodeRead(String text, PointF[] points) {

                        }

                        @Override
                        public void updatePhotoAtIndex(int index) {

                        }
                    });
                }
            });
        } else if(size > 1){
            gridView.setVisibility(VISIBLE);
            iv.setVisibility(GONE);

            if(size > 3){
                int height = getItemWidth()*2 + AndroidUtilities.dp(16);
                gridView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            } else {
                int height = getItemWidth();
                gridView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            }
            gridView.setAdapter(new ImageAdapter());
        }
    }

    private class ImageAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_stream_image_view, null);
                convertView.setLayoutParams(new GridView.LayoutParams(getItemWidth(), getItemWidth()));
                coreImageView = (SimpleDraweeView) convertView.findViewById(R.id.row_stream_image_view_iv);
                convertView.setTag(coreImageView);
            } else
                coreImageView = (SimpleDraweeView) convertView.getTag();
                Uri uri = Uri.parse(images.get(position) + "?imageView2/3/w/200/h/200");
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .build();
//            coreImageView.setController(controller);
            if(images.get(position).endsWith(".gif")) {
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(ApplicationLoader.applicationContext.getResources());
                builder.setOverlay(ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.overlay_gif));
                coreImageView.setHierarchy(builder.build());
            } else {
                GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(ApplicationLoader.applicationContext.getResources());
                coreImageView.setHierarchy(builder.build());
            }
            coreImageView.setImageURI(uri);
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    T8Log.PAN_JIA_FANG.d("click the image");
                    T8PhotoViewer.getInstance().setParentActivity(baseFragment.getParentActivity());
                    final ArrayList<Object> arrayList = new ArrayList<>();
                    for (String image : images) {
                        arrayList.add(new MediaController.PhotoEntry(0, 0, 0, image, 0, false));
                    }

                    T8PhotoViewer.getInstance().openPhotoForSelect(arrayList, position, 2, new T8PhotoViewer.PhotoViewerProvider() {
                        @Override
                        public T8PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
                            int coords[] = new int[2];
                            v.getLocationInWindow(coords);
                            T8PhotoViewer.PlaceProviderObject object = new T8PhotoViewer.PlaceProviderObject();
                            object.viewX = coords[0];
                            object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                            v.setDrawingCacheEnabled(true);
                            Bitmap cacheBmp = v.getDrawingCache();
                            object.thumb  = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
                            v.destroyDrawingCache();

                            object.width = v.getWidth();
                            object.height = v.getHeight();
                            return object;
                        }

                        @Override
                        public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
                            return null;
                        }

                        @Override
                        public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {

                        }

                        @Override
                        public void willHidePhotoViewer() {

                        }

                        @Override
                        public boolean isPhotoChecked(int index) {
                            return false;
                        }

                        @Override
                        public void setPhotoChecked(int index) {

                        }

                        @Override
                        public void cancelButtonPressed() {

                        }

                        @Override
                        public void sendButtonPressed(int index) {

                        }

                        @Override
                        public int getSelectedCount() {
                            return 0;
                        }

                        @Override
                        public void onQRCodeRead(String text, PointF[] points) {

                        }

                        @Override
                        public void updatePhotoAtIndex(int index) {

                        }
                    });
                }
            });

            return convertView;
        }
        SimpleDraweeView coreImageView;
    }

    private int getItemWidth(){
        int width = 0;
        width = (AndroidUtilities.getRealScreenSize().x - AndroidUtilities.dp(16)*4) / 3;
        return width;
    }

    private class MControllerListener extends BaseControllerListener<ImageInfo>{
        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
            if (animatable != null) {
                // app-specific logic to enable animation starting
                animatable.start();
            }
        }
    }
}
