package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dlazaro66.qrcodereaderview.ViewfinderView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import com.tinfinite.utils.QRCodeUtil;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by PanJiafang on 15/2/11.
 */
public class GroupQRScanActivity extends BaseFragment implements QRCodeReaderView.OnQRCodeReadListener{

    private QRCodeReaderView readerView;
    private RelativeLayout layout;

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if(fragmentView == null){
            fragmentView = new LinearLayout(getParentActivity());
            fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
            fragmentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("AddGroup", R.string.AddGroup));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            View view = inflater.inflate(R.layout.group_qr_scan, null);

            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            layout = (RelativeLayout) view.findViewById(R.id.group_qrscan_container);
            layout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    readerView = new QRCodeReaderView(getParentActivity());
                    readerView.setOnQRCodeReadListener(GroupQRScanActivity.this);

                    int width = AndroidUtilities.getRealScreenSize().x;
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width / 2, width / 2);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    readerView.setEnabled(false);

                    ViewfinderView finderView = new ViewfinderView(getParentActivity());
                    params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                    layout.addView(readerView);
                    layout.addView(finderView, params);

                    Button button = new Button(getParentActivity());
                    button.setText(LocaleController.getString("LocalFile", R.string.LocalFile));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK);// 打开相册
                            galleryIntent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(galleryIntent, 0);
                        }
                    });
                    button.setBackgroundDrawable(null);
                    button.setPadding(0, AndroidUtilities.dp(20), 0, AndroidUtilities.dp(30));
                    button.setGravity(Gravity.CENTER);
                    button.setTextColor(Color.WHITE);
                    params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layout.addView(button, params);

                }
            }, 300);

            ((LinearLayout) fragmentView).addView(view);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        layout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (readerView != null)
                    readerView.getCameraManager().startPreview();
            }
        }, 300);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(readerView != null)
        readerView.getCameraManager().stopPreview();
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            try {
                InputStream inputStream = getParentActivity().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                int[] intArray = new int[bitmap.getWidth()*bitmap.getHeight()];
                //copy pixel data from the Bitmap into the 'intArray' array
                bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                if(readerView != null)
                    readerView.decodeFromFile(intArray, bitmap.getWidth(), bitmap.getHeight());

                bitmap.recycle();
                inputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Log.d("T81", text);

        if(readerView != null)
            readerView.setOnQRCodeReadListener(null);

        QRCodeUtil.joinGrpByQRCode(this, text, true);
    }

    @Override
    public void cameraNotFound() {

    }

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }
}
