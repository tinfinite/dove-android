package com.dlazaro66.qrcodereaderview;

import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.QRCodeReaderView.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.open.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/*
 * Copyright 2014 David Lázaro Esparcia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/**
 * QRCodeReaderView - Class which uses ZXING lib and let you easily integrate a QR decoder view.
 * Take some classes and made some modifications in the original ZXING - Barcode Scanner project.  
 *
 * @author David L�zaro
 */
public class QRCodeReaderView extends SurfaceView implements SurfaceHolder.Callback,Camera.PreviewCallback {

	public interface OnQRCodeReadListener {
		
		public void onQRCodeRead(String text, PointF[] points);
		public void cameraNotFound();
		public void QRCodeNotFoundOnCamImage();
	}
	
	private OnQRCodeReadListener mOnQRCodeReadListener;
	
	private static final String TAG = QRCodeReaderView.class.getName();
	
	private QRCodeReader mQRCodeReader;
    private int mPreviewWidth; 
    private int mPreviewHeight; 
    private SurfaceHolder mHolder;
    private CameraManager mCameraManager;
    
	public QRCodeReaderView(Context context) {
		super(context);
		init();
	}
	
	public QRCodeReaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void setOnQRCodeReadListener(OnQRCodeReadListener onQRCodeReadListener) {
		mOnQRCodeReadListener = onQRCodeReadListener;
	}
	
	public CameraManager getCameraManager() {
		return mCameraManager;
	}

	@SuppressWarnings("deprecation")
	private void init() {
		if (checkCameraHardware(getContext())){
			mCameraManager = new CameraManager(getContext());

			mHolder = this.getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  // Need to set this flag despite it's deprecated
		} else {
			Log.e(TAG, "Error: Camera not found");
			mOnQRCodeReadListener.cameraNotFound();
		}
	}

    private int parentWidth, parentHeight;
    public void setWidthAndHeight(int width, int height){
        this.parentWidth = width;
        this.parentHeight = height;
    }
	
	/****************************************************
	 * SurfaceHolder.Callback,Camera.PreviewCallback
	 ****************************************************/
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// Indicate camera, our View dimensions
			mCameraManager.openDriver(holder,this.getWidth(),this.getHeight());
		} catch (IOException e) {
			Log.w(TAG, "Can not openDriver: "+e.getMessage());
			mCameraManager.closeDriver();
		} catch (RuntimeException e){
            Log.w(TAG, "Can not openDriver: "+e.getMessage());
            Toast.makeText(getContext(), R.string.openCameraFailure, Toast.LENGTH_SHORT).show();
            mCameraManager.closeDriver();
        }

		try {
			mQRCodeReader = new QRCodeReader();
			mCameraManager.startPreview();
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + e.getMessage());
			mCameraManager.closeDriver();
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "sur`faceDestroyed");
		mCameraManager.getCamera().setPreviewCallback(null);
		mCameraManager.getCamera().stopPreview();
		mCameraManager.getCamera().release();
		mCameraManager.closeDriver();
	}
	
	// Called when camera take a frame 
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(data, mPreviewWidth, mPreviewHeight, this.getWidth(), this.getHeight());

		HybridBinarizer hybBin = new HybridBinarizer(source);
		BinaryBitmap bitmap = new BinaryBitmap(hybBin);

		try {
			Result result = mQRCodeReader.decode(bitmap);  
				
			// Notify We're found a QRCode
			if (mOnQRCodeReadListener != null) {
					// Transform resultPoints to View coordinates
					PointF[] transformedPoints = transformToViewCoordinates(result.getResultPoints());
					mOnQRCodeReadListener.onQRCodeRead(result.getText(), transformedPoints);
			}
			
		} catch (ChecksumException e) {
			Log.d(TAG, "ChecksumException");
			e.printStackTrace();
		} catch (NotFoundException e) {
			// Notify QR not found
			if (mOnQRCodeReadListener != null) {
				mOnQRCodeReadListener.QRCodeNotFoundOnCamImage();
			}
		} catch (FormatException e) {
			Log.d(TAG, "FormatException");
			e.printStackTrace();
		} finally {
			mQRCodeReader.reset();
		}
	}

    /**
     * 从图片中扫描
     * @param data
     * @param witdh
     * @param height
     */
    public void decodeFromFile(int[]data, int witdh, int height){
        RGBLuminanceSource source = new RGBLuminanceSource(witdh, height, data);
//        PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(getNV21(data, witdh, height), witdh, height);
        HybridBinarizer hybBin = new HybridBinarizer(source);
        BinaryBitmap bitmap = new BinaryBitmap(hybBin);

        if(mQRCodeReader == null)
            mQRCodeReader = new QRCodeReader();

        try {
            Result result = mQRCodeReader.decode(bitmap);

            // Notify We're found a QRCode
            if (mOnQRCodeReadListener != null) {
                // Transform resultPoints to View coordinates
                PointF[] transformedPoints = transformToViewCoordinates(result.getResultPoints());
                mOnQRCodeReadListener.onQRCodeRead(result.getText(), transformedPoints);
            }

        } catch (ChecksumException e) {
            Log.d(TAG, "ChecksumException");
            e.printStackTrace();
        } catch (NotFoundException e) {
            // Notify QR not found
            if (mOnQRCodeReadListener != null) {
                mOnQRCodeReadListener.QRCodeNotFoundOnCamImage();
            }
        } catch (FormatException e) {
            Log.d(TAG, "FormatException");
            e.printStackTrace();
        } finally {
            mQRCodeReader.reset();
        }
    }

    // untested function
    byte [] getNV21(int[] data, int inputWidth, int inputHeight) {

        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        encodeYUV420SP(yuv, data, inputWidth, inputHeight);

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");

		if (mHolder.getSurface() == null){
			Log.e(TAG, "Error: preview surface does not exist");
			return;
		}

		//preview_width = width;
		//preview_height = height;

		mPreviewWidth = mCameraManager.getPreviewSize().x;
		mPreviewHeight = mCameraManager.getPreviewSize().y;


		mCameraManager.stopPreview();
		mCameraManager.getCamera().setPreviewCallback(this);

		mCameraManager.startPreview();
	}

	/**
	 * Transform result to surfaceView coordinates
	 *
	 * This method is needed because coordinates are given in landscape camera coordinates.
	 * Now is working but transform operations aren't very explained
	 *
	 * TODO re-write this method explaining each single value
	 *
	 * @return a new PointF array with transformed points
	 */
	private PointF[] transformToViewCoordinates(ResultPoint[] resultPoints) {

		PointF[] transformedPoints = new PointF[resultPoints.length];
		int index = 0;
		if (resultPoints != null){
			float previewX = mCameraManager.getPreviewSize().x;
			float previewY = mCameraManager.getPreviewSize().y;
			float scaleX = this.getWidth()/previewY;
			float scaleY = this.getHeight()/previewX;

			for (ResultPoint point :resultPoints){
				PointF tmppoint = new PointF((previewY- point.getY())*scaleX, point.getX()*scaleY);
				transformedPoints[index] = tmppoint;
				index++;
			}
		}
		return transformedPoints;

	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// this device has a camera
			return true;
		}
		else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
			// this device has a front camera
			return true;
		}
		else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
			// this device has any camera
			return true;
		}
		else {
			// no camera on this device
			return false;
		}
	}
	
}
