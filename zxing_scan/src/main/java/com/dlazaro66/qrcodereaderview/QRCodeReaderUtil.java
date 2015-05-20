package com.dlazaro66.qrcodereaderview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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

import java.io.IOException;

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
public class QRCodeReaderUtil extends SurfaceView{

	public interface OnQRCodeReadListener {

		public void onQRCodeRead(String text, PointF[] points);
		public void QRCodeNotFoundOnCamImage();
	}

	private static final String TAG = QRCodeReaderUtil.class.getName();


	public QRCodeReaderUtil(Context context) {
		super(context);
	}

    /**
     * 从图片中扫描
     * @param data
     * @param witdh
     * @param height
     */
    public static void decodeFromFile(int[]data, int witdh, int height, OnQRCodeReadListener mOnQRCodeReadListener){
        RGBLuminanceSource source = new RGBLuminanceSource(witdh, height, data);
//        PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(getNV21(data, witdh, height), witdh, height);
        HybridBinarizer hybBin = new HybridBinarizer(source);
        BinaryBitmap bitmap = new BinaryBitmap(hybBin);

	    QRCodeReader mQRCodeReader = new QRCodeReader();

        try {
            Result result = mQRCodeReader.decode(bitmap);

            // Notify We're found a QRCode
            if (mOnQRCodeReadListener != null) {
                // Transform resultPoints to View coordinates
                mOnQRCodeReadListener.onQRCodeRead(result.getText(), null);
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

    public static boolean hasQRCode(Bitmap bit){
        if(bit == null || bit.isRecycled())
            return false;
        int[] intArray = new int[bit.getWidth()*bit.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bit.getPixels(intArray, 0, bit.getWidth(), 0, 0, bit.getWidth(), bit.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(bit.getWidth(), bit.getHeight(), intArray);
//        PlanarYUVLuminanceSource source = mCameraManager.buildLuminanceSource(getNV21(data, witdh, height), witdh, height);
        HybridBinarizer hybBin = new HybridBinarizer(source);
        BinaryBitmap bitmap = new BinaryBitmap(hybBin);

        QRCodeReader mQRCodeReader = new QRCodeReader();

        try {
            Result result = mQRCodeReader.decode(bitmap);

            return true;
        } catch (ChecksumException e) {
            Log.d(TAG, "ChecksumException");
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            Log.d(TAG, "FormatException");
            e.printStackTrace();
        } finally {
            mQRCodeReader.reset();
        }
        return false;
    }

}
