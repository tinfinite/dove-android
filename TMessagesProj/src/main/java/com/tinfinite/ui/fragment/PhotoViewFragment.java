package com.tinfinite.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import org.telegram.messenger.R;


public class PhotoViewFragment extends Fragment {
    private static final String TAG = "PhotoViewFragment";
    private static final boolean DBG = true;

    public static final String ARG_PHOTO_IMAGE_URL= "arg_photo_image_url";

    private String mImageUrl;
    private SimpleDraweeView simpleDraweeView;
    private static final String KEY_STATE = "state";
    private boolean mHasRequest;

    public static PhotoViewFragment newInstance(String Url) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_IMAGE_URL, Url);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageUrl = getArguments().getString(ARG_PHOTO_IMAGE_URL);
            log("mImageUrl " + mImageUrl);
        }
        mHasRequest = false;
        if (savedInstanceState != null) {
            mHasRequest = savedInstanceState.getBoolean(KEY_STATE);
            log("onCreate mHasRequest " + mHasRequest);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        log("onSaveInstanceState");
        outState.putBoolean(KEY_STATE, mHasRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_view, container, false);
        simpleDraweeView = (SimpleDraweeView) view.findViewById(R.id.photoview_iv);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!mHasRequest) {
            log("loadBitmap");
            Uri lowResUri = Uri.parse(mImageUrl+"?imageView2/3/w/200/h/200");
            Uri highResUri = Uri.parse(mImageUrl+"?imageView2/2/w/640");

            simpleDraweeView.setController(Fresco.newDraweeControllerBuilder()
                    .setLowResImageRequest(ImageRequest.fromUri(lowResUri))
                    .setImageRequest(ImageRequest.fromUri(highResUri))
                    .setOldController(simpleDraweeView.getController())
                    .build());
        }
    }

    void log(String msg) {
        if (DBG) {
            Log.e(TAG, msg);
        }
    }
}
