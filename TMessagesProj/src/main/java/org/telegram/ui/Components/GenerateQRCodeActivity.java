/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui.Components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class GenerateQRCodeActivity extends BaseFragment {
    private static final String TAG = "GenerateQRCodeActivity";
    private static final String URL = "http://dove.tinfinite.com/Dove?chat_id=%d&chat_name=%s";
    private final static int gallery_menu_save = 1;
    private final static int gallery_menu_share = 2;

    private String QRCodeContent;
    private int chat_id;
    private ImageView QRCodeImageView;
    private Bitmap QRCodeBitmap;
    private TextView chatName;
    private String chatTitle;
    private ActionBarMenuItem menuItem;

    public GenerateQRCodeActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        chat_id = getArguments().getInt("chat_id", 0);
        TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_id);
        chatTitle = chat.title;
        QRCodeContent = String.format(URL, chat_id, chatTitle);
        Log.d(TAG, "QRCodeContent " + QRCodeContent);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if (fragmentView == null) {
            ActionBarMenu menu = actionBar.createMenu();
            menuItem = menu.addItem(0, R.drawable.ic_ab_other);
            menuItem.setNeedOffset(false);
            menuItem.addSubItem(gallery_menu_save, LocaleController.getString("SaveToGallery", R.string.SaveToGallery), 0);
            menuItem.addSubItem(gallery_menu_share , LocaleController.getString("Forward", R.string.Forward), 0);

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setAllowOverlayTitle(true);
            actionBar.setTitle(LocaleController.getString("GroupQRCode", R.string.GroupQRCode));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        if (!QRCodeBitmap.isRecycled()) {
                            QRCodeImageView.setImageBitmap(null);
                            QRCodeBitmap.recycle();
                        }
                        finishFragment();
                    } else if (id == gallery_menu_save) {
                        saveQRCodeFile(null);
                    } else if (id == gallery_menu_share) {
                        Intent intent = new Intent("android.intent.action.SHARE_QRCODE");
                        intent.setType("image/jpeg");
                        File shareImage = generateQRCodeFile();
                        saveQRCodeFile(shareImage);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareImage));
                        getParentActivity().startActivity(intent);
                    }
                }
            });
            fragmentView = inflater.inflate(R.layout.group_qrcode_layout, null);
            QRCodeImageView = (ImageView) fragmentView.findViewById(R.id.qrcode_image);
            QRCodeBitmap = generateQRCode(QRCodeContent);
            QRCodeImageView.setImageBitmap(QRCodeBitmap);
            chatName = (TextView) fragmentView.findViewById(R.id.chat_name);
            chatName.setText(chatTitle);

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
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private Bitmap generateQRCode(String content) {
        try {
//            QRCodeWriter writer = new QRCodeWriter();
            MultiFormatWriter writer = new MultiFormatWriter();
            Map<EncodeHintType,Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 800, 800, hints);
            return bitMatrix2Bitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getMagicDrawingCache(View view) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight,Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void saveQRCodeFile(File srcFile) {
        final Bitmap bitmap = (Bitmap) getMagicDrawingCache(fragmentView.findViewById(R.id.qrcode_image_container));
        File dest = null;
        if (srcFile != null) {
            dest = srcFile;
        } else {
            dest = Utilities.generatePicturePath();
        }

        final File destFile = dest;
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!destFile.exists()) {
                            destFile.createNewFile();
                        }
                        FileOutputStream out = new FileOutputStream(destFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.flush();
                        out.close();

                        Utilities.addMediaToGallery(Uri.fromFile(destFile));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }finally {
                        bitmap.recycle();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File generateQRCodeFile() {
        try {
            File storageDir = getAlbumDir();
            return new File(storageDir, "IMG_" + "QRCodeFile" + ".jpg");
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return null;
    }

    private static File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Dove");
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()){
                        FileLog.d("tmessages", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            FileLog.d("tmessages", "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

}
