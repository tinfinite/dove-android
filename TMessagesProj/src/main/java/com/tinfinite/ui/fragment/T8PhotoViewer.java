/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.tinfinite.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.facebook.imageformat.ImageFormat;
import com.facebook.imageformat.ImageFormatChecker;
import com.tinfinite.ui.AnimationCompat.AnimatorListenerAdapterProxy;
import com.tinfinite.ui.AnimationCompat.AnimatorSetProxy;
import com.tinfinite.ui.AnimationCompat.ObjectAnimatorProxy;
import com.tinfinite.ui.AnimationCompat.ViewProxy;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ImageLoader;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.query.SharedMediaQuery;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.ClippingImageView;
import org.telegram.ui.Components.GifDrawable;
import org.telegram.ui.Components.PhotoCropView;
import org.telegram.ui.Components.PhotoFilterView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

public class T8PhotoViewer implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, NotificationCenter.NotificationCenterDelegate {

    private int classGuid;
    private PhotoViewerProvider placeProvider;
    private boolean isVisible = false;

    private Activity parentActivity;

    private ActionBar actionBar;
    private boolean isActionBarVisible = true;

    private static Drawable[] progressDrawables = null;

    private WindowManager.LayoutParams windowLayoutParams;
    private FrameLayoutDrawer containerView;
    private FrameLayoutTouchListener windowView;
    private ClippingImageView animatingImageView;
    private FrameLayout bottomLayout;
    private TextView nameTextView;
    private TextView dateTextView;
    private ColorDrawable backgroundDrawable = new ColorDrawable(0xff000000);
    private CheckBox checkImageView;
    private RadialProgressView radialProgressViews[] = new RadialProgressView[3];
    private GifDrawable gifDrawable;
    private AnimatorSetProxy currentActionBarAnimation;
    private PhotoCropView photoCropView;
    private PhotoFilterView photoFilterView;
    private AlertDialog visibleDialog = null;
    private boolean canShowBottom = true;
    private int sendPhotoType = 0;

    private int animationInProgress = 0;
    private long transitionAnimationStartTime = 0;
    private Runnable animationEndRunnable = null;
    private PlaceProviderObject showAfterAnimation;
    private PlaceProviderObject hideAfterAnimation;
    private boolean disableShowCheck = false;

    private int currentEditMode;

    private ImageReceiver leftImage = new ImageReceiver();
    private ImageReceiver centerImage = new ImageReceiver();
    private ImageReceiver rightImage = new ImageReceiver();
    private int currentIndex;
    private String currentFileNames[] = new String[3];
    private PlaceProviderObject currentPlaceObject;
    private String currentPathObject;
    private Bitmap currentThumb = null;

    private int avatarsUserId;
    private long currentDialogId;
    private int totalImagesCount;

    private boolean draggingDown = false;
    private float dragY;
    private float translationX = 0;
    private float translationY = 0;
    private float scale = 1;
    private float animateToX;
    private float animateToY;
    private float animateToScale;
    private float animationValue;
    private long animationStartTime;
    private AnimatorSetProxy imageMoveAnimation;
    private GestureDetector gestureDetector;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator(1.5f);
    private float pinchStartDistance = 0;
    private float pinchStartScale = 1;
    private float pinchCenterX;
    private float pinchCenterY;
    private float pinchStartX;
    private float pinchStartY;
    private float moveStartX;
    private float moveStartY;
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private boolean canZoom = true;
    private boolean changingPage = false;
    private boolean zooming = false;
    private boolean moving = false;
    private boolean doubleTap = false;
    private boolean invalidCoords = false;
    private boolean canDragDown = true;
    private boolean zoomAnimation = false;
    private boolean discardTap = false;
    private int switchImageAfterAnimation = 0;
    private VelocityTracker velocityTracker = null;
    private Scroller scroller = null;

    private ArrayList<Object> imagesArrLocals = new ArrayList<>();

    private final static int PAGE_SPACING = AndroidUtilities.dp(30);

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.FileDidFailedLoad) {
            String location = (String) args[0];
            for (int a = 0; a < 3; a++) {
                if (currentFileNames[a] != null && currentFileNames[a].equals(location)) {
                    radialProgressViews[a].setProgress(1.0f, true);
                    checkProgress(a, true);
                    break;
                }
            }
        } else if (id == NotificationCenter.FileDidLoaded) {
            String location = (String) args[0];
            for (int a = 0; a < 3; a++) {
                if (currentFileNames[a] != null && currentFileNames[a].equals(location)) {
                    radialProgressViews[a].setProgress(1.0f, true);
                    checkProgress(a, true);
                    if (a == 0) {
                        createGifForCurrentImage();
                    }
                    break;
                }
            }
        } else if (id == NotificationCenter.FileLoadProgressChanged) {
            String location = (String) args[0];
            for (int a = 0; a < 3; a++) {
                if (currentFileNames[a] != null && currentFileNames[a].equals(location)) {
                    Float progress = (Float) args[1];
                    radialProgressViews[a].setProgress(progress, true);
                }
            }
        }
    }

    private static class RadialProgressView {

        private long lastUpdateTime = 0;
        private float radOffset = 0;
        private float currentProgress = 0;
        private float animationProgressStart = 0;
        private long currentProgressTime = 0;
        private float animatedProgressValue = 0;
        private RectF progressRect = new RectF();
        private int backgroundState = -1;
        private View parent = null;
        private int size = AndroidUtilities.dp(64);
        private int previousBackgroundState = -2;
        private float animatedAlphaValue = 1.0f;
        private float alpha = 1.0f;
        private float scale = 1.0f;

        private static DecelerateInterpolator decelerateInterpolator = null;
        private static Paint progressPaint = null;

        public RadialProgressView(Context context, View parentView) {
            if (decelerateInterpolator == null) {
                decelerateInterpolator = new DecelerateInterpolator(1.5f);
                progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                progressPaint.setStyle(Paint.Style.STROKE);
                progressPaint.setStrokeCap(Paint.Cap.ROUND);
                progressPaint.setStrokeWidth(AndroidUtilities.dp(2));
                progressPaint.setColor(0xffffffff);
            }
            parent = parentView;
        }

        private void updateAnimation() {
            long newTime = System.currentTimeMillis();
            long dt = newTime - lastUpdateTime;
            lastUpdateTime = newTime;

            if (animatedProgressValue != 1) {
                radOffset += 360 * dt / 3000.0f;
                float progressDiff = currentProgress - animationProgressStart;
                if (progressDiff > 0) {
                    currentProgressTime += dt;
                    if (currentProgressTime >= 300) {
                        animatedProgressValue = currentProgress;
                        animationProgressStart = currentProgress;
                        currentProgressTime = 0;
                    } else {
                        animatedProgressValue = animationProgressStart + progressDiff * decelerateInterpolator.getInterpolation(currentProgressTime / 300.0f);
                    }
                }
                parent.invalidate();
            }
            if (animatedProgressValue >= 1 && previousBackgroundState != -2) {
                animatedAlphaValue -= dt / 200.0f;
                if (animatedAlphaValue <= 0) {
                    animatedAlphaValue = 0.0f;
                    previousBackgroundState = -2;
                }
                parent.invalidate();
            }
        }

        public void setProgress(float value, boolean animated) {
            if (!animated) {
                animatedProgressValue = value;
                animationProgressStart = value;
            } else {
                animationProgressStart = animatedProgressValue;
            }
            currentProgress = value;
            currentProgressTime = 0;
        }

        public void setBackgroundState(int state, boolean animated) {
            lastUpdateTime = System.currentTimeMillis();
            if (animated && backgroundState != state) {
                previousBackgroundState = backgroundState;
                animatedAlphaValue = 1.0f;
            } else {
                previousBackgroundState = -2;
            }
            backgroundState = state;
            parent.invalidate();
        }

        public void setAlpha(float value) {
            alpha = value;
        }

        public void setScale(float value) {
            scale = value;
        }

        public void onDraw(Canvas canvas) {
            int sizeScaled = (int) (size * scale);
            int x = (canvas.getWidth() - sizeScaled) / 2;
            int y = (canvas.getHeight() - sizeScaled) / 2;

            if (previousBackgroundState >= 0 && previousBackgroundState < 4) {
                Drawable drawable = progressDrawables[previousBackgroundState];
                if (drawable != null) {
                    drawable.setAlpha((int) (255 * animatedAlphaValue * alpha));
                    drawable.setBounds(x, y, x + sizeScaled, y + sizeScaled);
                    drawable.draw(canvas);
                }
            }

            if (backgroundState >= 0 && backgroundState < 4) {
                Drawable drawable = progressDrawables[backgroundState];
                if (drawable != null) {
                    if (previousBackgroundState != -2) {
                        drawable.setAlpha((int) (255 * (1.0f - animatedAlphaValue) * alpha));
                    } else {
                        drawable.setAlpha((int) (255 * alpha));
                    }
                    drawable.setBounds(x, y, x + sizeScaled, y + sizeScaled);
                    drawable.draw(canvas);
                }
            }

            if (backgroundState == 0 || backgroundState == 1 || previousBackgroundState == 0 || previousBackgroundState == 1) {
                int diff = AndroidUtilities.dp(1);
                if (previousBackgroundState != -2) {
                    progressPaint.setAlpha((int) (255 * animatedAlphaValue * alpha));
                } else {
                    progressPaint.setAlpha((int) (255 * alpha));
                }
                progressRect.set(x + diff, y + diff, x + sizeScaled - diff, y + sizeScaled - diff);
                canvas.drawArc(progressRect, -90 + radOffset, Math.max(4, 360 * animatedProgressValue), false, progressPaint);
                updateAnimation();
            }
        }
    }

    public static class PlaceProviderObject {
        public ImageReceiver imageReceiver;
        public int viewX;
        public int viewY;
        public View parentView;
        public Bitmap thumb;
        public int user_id;
        public int index;
        public int size;
        public int radius;
        // dove add
        public int width;
        public int height;
    }

    public static class EmptyPhotoViewerProvider implements PhotoViewerProvider {
        @Override
        public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
            return null;
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
    }

    public interface PhotoViewerProvider {
        PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index);

        Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index);

        void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index);

        void willHidePhotoViewer();

        boolean isPhotoChecked(int index);

        void setPhotoChecked(int index);

        void cancelButtonPressed();

        void sendButtonPressed(int index);

        public int getSelectedCount();
        public void onQRCodeRead(String text, PointF[] points);
        public void updatePhotoAtIndex(int index);
    }

    private class FrameLayoutTouchListener extends FrameLayout {
        public FrameLayoutTouchListener(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return getInstance().onTouchEvent(event);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            getInstance().onLayout(changed, left, top, right, bottom);
        }
    }

    private class FrameLayoutDrawer extends FrameLayout {
        public FrameLayoutDrawer(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            getInstance().onDraw(canvas);
        }
    }

    private static volatile T8PhotoViewer Instance = null;

    public static T8PhotoViewer getInstance() {
        T8PhotoViewer localInstance = Instance;
        if (localInstance == null) {
            synchronized (T8PhotoViewer.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new T8PhotoViewer();
                }
            }
        }
        return localInstance;
    }

    public void setParentActivity(Activity activity) {
        if (parentActivity == activity) {
            return;
        }
        parentActivity = activity;

        if (progressDrawables == null) {
            progressDrawables = new Drawable[4];
            progressDrawables[0] = parentActivity.getResources().getDrawable(R.drawable.circle_big);
            progressDrawables[1] = parentActivity.getResources().getDrawable(R.drawable.cancel_big);
            progressDrawables[2] = parentActivity.getResources().getDrawable(R.drawable.load_big);
            progressDrawables[3] = parentActivity.getResources().getDrawable(R.drawable.play_big);
        }

        scroller = new Scroller(activity);

        windowView = new FrameLayoutTouchListener(activity);
        windowView.setBackgroundDrawable(backgroundDrawable);
        windowView.setFocusable(false);

        animatingImageView = new ClippingImageView(windowView.getContext());
        windowView.addView(animatingImageView);

        containerView = new FrameLayoutDrawer(activity);
        containerView.setFocusable(false);
        windowView.addView(containerView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) containerView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        containerView.setLayoutParams(layoutParams);

        windowLayoutParams = new WindowManager.LayoutParams();
        windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        windowLayoutParams.format = PixelFormat.TRANSLUCENT;
        windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowLayoutParams.gravity = Gravity.TOP;
        windowLayoutParams.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        actionBar = new ActionBar(activity);
        actionBar.setBackgroundColor(0x7F000000);
        actionBar.setOccupyStatusBar(false);
        actionBar.setItemsBackground(R.drawable.bar_selector_white);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.formatString("Of", R.string.Of, 1, 1));
        containerView.addView(actionBar);
        layoutParams = (FrameLayout.LayoutParams) actionBar.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        actionBar.setLayoutParams(layoutParams);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    closePhoto(true, false);
                }
            }

        });

        bottomLayout = new FrameLayout(containerView.getContext());
        containerView.addView(bottomLayout);
        layoutParams = (FrameLayout.LayoutParams) bottomLayout.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = AndroidUtilities.dp(48);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        bottomLayout.setLayoutParams(layoutParams);
        bottomLayout.setBackgroundColor(0x7f000000);

        radialProgressViews[0] = new RadialProgressView(containerView.getContext(), containerView);
        radialProgressViews[0].setBackgroundState(0, false);
        radialProgressViews[1] = new RadialProgressView(containerView.getContext(), containerView);
        radialProgressViews[1].setBackgroundState(0, false);
        radialProgressViews[2] = new RadialProgressView(containerView.getContext(), containerView);
        radialProgressViews[2].setBackgroundState(0, false);

        nameTextView = new TextView(containerView.getContext());
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setSingleLine(true);
        nameTextView.setMaxLines(1);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setGravity(Gravity.LEFT);
        bottomLayout.addView(nameTextView);
        layoutParams = (FrameLayout.LayoutParams) nameTextView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.leftMargin = AndroidUtilities.dp(16);
        layoutParams.rightMargin = AndroidUtilities.dp(50);
        layoutParams.topMargin = AndroidUtilities.dp(5);
        nameTextView.setLayoutParams(layoutParams);

        dateTextView = new TextView(containerView.getContext());
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        dateTextView.setSingleLine(true);
        dateTextView.setMaxLines(1);
        dateTextView.setEllipsize(TextUtils.TruncateAt.END);
        dateTextView.setTextColor(0xffffffff);
        dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        dateTextView.setGravity(Gravity.LEFT);
        bottomLayout.addView(dateTextView);
        layoutParams = (FrameLayout.LayoutParams) dateTextView.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.leftMargin = AndroidUtilities.dp(16);
        layoutParams.rightMargin = AndroidUtilities.dp(50);
        layoutParams.topMargin = AndroidUtilities.dp(25);
        dateTextView.setLayoutParams(layoutParams);

        gestureDetector = new GestureDetector(containerView.getContext(), this);
        gestureDetector.setOnDoubleTapListener(this);

        centerImage.setParentView(containerView);
        leftImage.setParentView(containerView);
        rightImage.setParentView(containerView);

        checkImageView = new CheckBox(containerView.getContext(), R.drawable.selectphoto_large);
        checkImageView.setDrawBackground(true);
        checkImageView.setSize(45);
        checkImageView.setCheckOffset(AndroidUtilities.dp(1));
        checkImageView.setColor(0xff3ccaef);
        containerView.addView(checkImageView);
        checkImageView.setVisibility(View.GONE);
        layoutParams = (FrameLayout.LayoutParams) checkImageView.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(45);
        layoutParams.height = AndroidUtilities.dp(45);
        layoutParams.gravity = Gravity.RIGHT;
        layoutParams.rightMargin = AndroidUtilities.dp(10);
        WindowManager manager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Activity.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
            layoutParams.topMargin = AndroidUtilities.dp(58);
        } else {
            layoutParams.topMargin = AndroidUtilities.dp(68);
        }
        checkImageView.setLayoutParams(layoutParams);
        checkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeProvider != null) {
                    placeProvider.setPhotoChecked(currentIndex);
                    checkImageView.setChecked(placeProvider.isPhotoChecked(currentIndex), true);
                    updateSelectedCount();
                }
            }
        });
    }

    private void toggleActionBar(boolean show, final boolean animated) {
        if (show) {
            actionBar.setVisibility(View.VISIBLE);
            if (canShowBottom) {
                bottomLayout.setVisibility(View.VISIBLE);
            }
        }
        isActionBarVisible = show;
        actionBar.setEnabled(show);
        bottomLayout.setEnabled(show);

        if (animated) {
            currentActionBarAnimation = new AnimatorSetProxy();
            currentActionBarAnimation.playTogether(
                    ObjectAnimatorProxy.ofFloat(actionBar, "alpha", show ? 1.0f : 0.0f),
                    ObjectAnimatorProxy.ofFloat(bottomLayout, "alpha", show ? 1.0f : 0.0f)
            );
            if (!show) {
                currentActionBarAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (currentActionBarAnimation.equals(animation)) {
                            actionBar.setVisibility(View.GONE);
                            if (canShowBottom) {
                                bottomLayout.clearAnimation();
                                bottomLayout.setVisibility(View.GONE);
                            }
                            currentActionBarAnimation = null;
                        }
                    }
                });
            }

            currentActionBarAnimation.setDuration(200);
            currentActionBarAnimation.start();
        } else {
            ViewProxy.setAlpha(actionBar, show ? 1.0f : 0.0f);
            ViewProxy.setAlpha(bottomLayout, show ? 1.0f : 0.0f);
            if (!show) {
                actionBar.setVisibility(View.GONE);
                if (canShowBottom) {
                    bottomLayout.clearAnimation();
                    bottomLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private String getFileName(int index) {
        if (index < 0) {
            return null;
        }
        if (!imagesArrLocals.isEmpty()) {
            if (index >= imagesArrLocals.size()) {
                return null;
            }
            Object object = imagesArrLocals.get(index);
            if (object instanceof MediaController.SearchImage) {
                MediaController.SearchImage searchImage = ((MediaController.SearchImage) object);
                if (searchImage.localUrl != null && searchImage.localUrl.length() > 0) {
                    File file = new File(searchImage.localUrl);
                    if (file.exists()) {
                        return file.getName();
                    } else {
                        searchImage.localUrl = "";
                    }
                }
                return Utilities.MD5(searchImage.imageUrl) + "." + ImageLoader.getHttpUrlExtension(searchImage.imageUrl);
            } else {
                if(currentPathObject == null)
                    return null;

                String key = Utilities.MD5(currentPathObject);
                String url = key + "." + ImageLoader.getHttpUrlExtension(currentPathObject);
                return url;
            }
        }
        return null;
    }

    private void updateSelectedCount() {
        if (placeProvider == null) {
            return;
        }
    }

    private void onPhotoShow(final ArrayList<Object> photos, int index, final PlaceProviderObject object) {
        classGuid = ConnectionsManager.getInstance().generateClassGuid();
        currentPathObject = null;
        currentIndex = -1;
        currentFileNames[0] = null;
        currentFileNames[1] = null;
        currentFileNames[2] = null;
        avatarsUserId = 0;
        currentDialogId = 0;
        totalImagesCount = 0;
        currentEditMode = 0;
        canShowBottom = true;
        imagesArrLocals.clear();
        currentThumb = object != null ? object.thumb : null;
        bottomLayout.setVisibility(View.VISIBLE);
        ViewProxy.setTranslationY(actionBar, 0);
        ViewProxy.setAlpha(checkImageView, 1.0f);
        checkImageView.clearAnimation();
        checkImageView.setVisibility(View.GONE);
        if (photoCropView != null) {
            photoCropView.clearAnimation();
            photoCropView.setVisibility(View.GONE);
        }
        if (photoFilterView != null) {
            photoFilterView.clearAnimation();
            photoFilterView.setVisibility(View.GONE);
        }

        for (int a = 0; a < 3; a++) {
            if (radialProgressViews[a] != null) {
                radialProgressViews[a].setBackgroundState(-1, false);
            }
        }

        if (photos != null) {
            if (sendPhotoType == 0) {
                checkImageView.setVisibility(View.VISIBLE);
            }
            imagesArrLocals.addAll(photos);
            setImageIndex(index, true);
            bottomLayout.clearAnimation();
            bottomLayout.setVisibility(View.GONE);
            canShowBottom = false;
            Object obj = imagesArrLocals.get(index);
            updateSelectedCount();
        }

        if (currentDialogId != 0 && totalImagesCount == 0) {
            SharedMediaQuery.getMediaCount(currentDialogId, SharedMediaQuery.MEDIA_PHOTOVIDEO, classGuid, true);
        } else if (avatarsUserId != 0) {
            MessagesController.getInstance().loadUserPhotos(avatarsUserId, 0, 80, 0, true, classGuid);
        }
    }

    private void setImages() {
        if (animationInProgress == 0) {
            setIndexToImage(centerImage, currentIndex);
            setIndexToImage(rightImage, currentIndex + 1);
            setIndexToImage(leftImage, currentIndex - 1);
        }
    }

    private void setImageIndex(int index, boolean init) {
        if (currentIndex == index) {
            return;
        }
        if (!init) {
            currentThumb = null;
        }
        int prevIndex = currentIndex;
        currentIndex = index;

        boolean sameImage = false;

        if (!imagesArrLocals.isEmpty()) {
            Object object = imagesArrLocals.get(index);
            if (index < 0 || index >= imagesArrLocals.size()) {
                closePhoto(false, false);
                return;
            }
            boolean fromCamera = false;
            if (object instanceof MediaController.PhotoEntry) {
                currentPathObject = ((MediaController.PhotoEntry) object).path;
                fromCamera = ((MediaController.PhotoEntry) object).bucketId == 0 && ((MediaController.PhotoEntry) object).dateTaken == 0 && imagesArrLocals.size() == 1;
            } else if (object instanceof MediaController.SearchImage) {
                currentPathObject = ((MediaController.SearchImage) object).imageUrl;
            }
            if (fromCamera) {
                actionBar.setTitle(LocaleController.getString("AttachPhoto", R.string.AttachPhoto));
            } else {
                actionBar.setTitle(LocaleController.formatString("Of", R.string.Of, currentIndex + 1, imagesArrLocals.size()));
            }
            if (sendPhotoType == 0) {
                checkImageView.setChecked(placeProvider.isPhotoChecked(currentIndex), false);
            }
        }

        currentFileNames[0] = getFileName(index);
        currentFileNames[1] = getFileName(index + 1);
        currentFileNames[2] = getFileName(index - 1);

//        if (currentPlaceObject != null) {
//            if (animationInProgress == 0) {
//                currentPlaceObject.imageReceiver.setVisible(true, true);
//            } else {
//                showAfterAnimation = currentPlaceObject;
//            }
//        }
//        currentPlaceObject = placeProvider.getPlaceForPhoto(null, null, currentIndex);
//        if (currentPlaceObject != null) {
//            if (animationInProgress == 0) {
//                currentPlaceObject.imageReceiver.setVisible(false, true);
//            } else {
//                hideAfterAnimation = currentPlaceObject;
//            }
//        }

        if (!sameImage) {
            draggingDown = false;
            translationX = 0;
            translationY = 0;
            scale = 1;
            animateToX = 0;
            animateToY = 0;
            animateToScale = 1;
            animationStartTime = 0;
            imageMoveAnimation = null;

            pinchStartDistance = 0;
            pinchStartScale = 1;
            pinchCenterX = 0;
            pinchCenterY = 0;
            pinchStartX = 0;
            pinchStartY = 0;
            moveStartX = 0;
            moveStartY = 0;
            zooming = false;
            moving = false;
            doubleTap = false;
            invalidCoords = false;
            canDragDown = true;
            changingPage = false;
            switchImageAfterAnimation = 0;
            canZoom = !imagesArrLocals.isEmpty() || (currentFileNames[0] != null && !currentFileNames[0].endsWith("mp4") && radialProgressViews[0].backgroundState != 0);
            updateMinMax(scale);
        }

        if (prevIndex == -1) {
            setImages();

            for (int a = 0; a < 3; a++) {
                checkProgress(a, false);
            }
        } else {
            checkProgress(0, false);
            if (prevIndex > currentIndex) {
                ImageReceiver temp = rightImage;
                rightImage = centerImage;
                centerImage = leftImage;
                leftImage = temp;

                RadialProgressView tempProgress = radialProgressViews[0];
                radialProgressViews[0] = radialProgressViews[2];
                radialProgressViews[2] = tempProgress;
                setIndexToImage(leftImage, currentIndex - 1);

                checkProgress(1, false);
                checkProgress(2, false);
            } else if (prevIndex < currentIndex) {
                ImageReceiver temp = leftImage;
                leftImage = centerImage;
                centerImage = rightImage;
                rightImage = temp;

                RadialProgressView tempProgress = radialProgressViews[0];
                radialProgressViews[0] = radialProgressViews[1];
                radialProgressViews[1] = tempProgress;
                setIndexToImage(rightImage, currentIndex + 1);

                checkProgress(1, false);
                checkProgress(2, false);
            }
        }

        createGifForCurrentImage();
    }

    private void createGifForCurrentImage() {
        if (gifDrawable != null) {
            gifDrawable.recycle();
            gifDrawable = null;
        }
        if (!imagesArrLocals.isEmpty()) {
            if (currentIndex >= 0 && currentIndex < imagesArrLocals.size()) {
                Object object = imagesArrLocals.get(currentIndex);
                if (object instanceof MediaController.PhotoEntry) {
                    currentPathObject = ((MediaController.PhotoEntry) object).path;
                }
                String key = Utilities.MD5(currentPathObject);
                String url = key + "." + ImageLoader.getHttpUrlExtension(currentPathObject);

                File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), url);

                    if (cacheFile.exists()) {
                        ImageFormat format = ImageFormatChecker.getImageFormat(cacheFile.getPath());
                        if (format != ImageFormat.GIF) {
                            return;
                        }
                        try {
                            gifDrawable = new GifDrawable(cacheFile);
                            gifDrawable.parentView = new WeakReference<View>(containerView);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                        if (gifDrawable != null) {
                            gifDrawable.start();
                        }
//                    }
                }
            }
        }
    }

    private void checkProgress(int a, boolean animated) {
        if (currentFileNames[a] != null) {
            int index = currentIndex;
            if (a == 1) {
                index += 1;
            } else if (a == 2) {
                index -= 1;
            }
            File f = null;
//            if (currentMessageObject != null) {
//                MessageObject messageObject = imagesArr.get(index);
//                f = FileLoader.getPathToMessage(messageObject.messageOwner);
//            } else if (currentFileLocation != null) {
//                TLRPC.FileLocation location = imagesArrLocations.get(index);
//                f = FileLoader.getPathToAttach(location, avatarsUserId != 0);
//            } else
            if (currentPathObject != null) {
                f = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_DOCUMENT), currentFileNames[a]);
                if (!f.exists()) {
                    f = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), currentFileNames[a]);
                }
            }
            if (f != null && f.exists()) {
                if (currentFileNames[a].endsWith("mp4")) {
                    radialProgressViews[a].setBackgroundState(3, animated);
                } else {
                    radialProgressViews[a].setBackgroundState(-1, animated);
                }
            } else {
                if (currentFileNames[a].endsWith("mp4")) {
                    if (!FileLoader.getInstance().isLoadingFile(currentFileNames[a])) {
                        radialProgressViews[a].setBackgroundState(2, false);
                    } else {
                        radialProgressViews[a].setBackgroundState(1, false);
                    }
                } else {
                    radialProgressViews[a].setBackgroundState(0, animated);
                }
                Float progress = ImageLoader.getInstance().getFileProgress(currentFileNames[a]);
                if (progress == null) {
                    progress = 0.0f;
                }
                radialProgressViews[a].setProgress(progress, false);
            }
            if (a == 0) {
                canZoom = !imagesArrLocals.isEmpty() || (currentFileNames[0] != null && !currentFileNames[0].endsWith("mp4") && radialProgressViews[0].backgroundState != 0);
            }
        } else {
            radialProgressViews[a].setBackgroundState(-1, animated);
        }
    }

    private void setIndexToImage(ImageReceiver imageReceiver, int index) {
        imageReceiver.setOrientation(0, false);
        if (!imagesArrLocals.isEmpty()) {
            imageReceiver.setParentMessageObject(null);
            if (index >= 0 && index < imagesArrLocals.size()) {
                Object object = imagesArrLocals.get(index);
                int size = (int) (AndroidUtilities.getPhotoSize() / AndroidUtilities.density);
                Bitmap placeHolder = null;
                if (currentThumb != null && imageReceiver == centerImage) {
                    placeHolder = currentThumb;
                }
                if (placeHolder == null) {
                    placeHolder = placeProvider.getThumbForPhoto(null, null, index);
                }
                String path = null;
                int imageSize = 0;
                if (object instanceof MediaController.PhotoEntry) {
                    MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) object;
                    if (photoEntry.imagePath != null) {
                        path = photoEntry.imagePath;
                    } else {
                        imageReceiver.setOrientation(photoEntry.orientation, false);
                        path = photoEntry.path;
                    }
                } else if (object instanceof MediaController.SearchImage) {
                    MediaController.SearchImage photoEntry = (MediaController.SearchImage) object;
                    if (photoEntry.imagePath != null) {
                        path = photoEntry.imagePath;
                    } else {
                        path = photoEntry.imageUrl;
                        imageSize = ((MediaController.SearchImage) object).size;
                    }
                }
                imageReceiver.setImage(path, String.format(Locale.US, "%d_%d", size, size), placeHolder != null ? new BitmapDrawable(null, placeHolder) : null, imageSize);
            } else {
                imageReceiver.setImageBitmap((Bitmap) null);
            }
        }

    }

//    public boolean isShowingImage(MessageObject object) {
//        return isVisible && !disableShowCheck && object != null && currentMessageObject != null && currentMessageObject.getId() == object.getId();
//    }
//
//    public boolean isShowingImage(TLRPC.FileLocation object) {
//        return isVisible && !disableShowCheck && object != null && currentFileLocation != null && object.local_id == currentFileLocation.local_id && object.volume_id == currentFileLocation.volume_id && object.dc_id == currentFileLocation.dc_id;
//    }

    public boolean isShowingImage(String object) {
        return isVisible && !disableShowCheck && object != null && currentPathObject != null && object.equals(currentPathObject);
    }

    public void openPhoto(final MessageObject messageObject, final PhotoViewerProvider provider) {
        openPhoto(messageObject, null, null, null, 0, provider);
    }

    public void openPhoto(final TLRPC.FileLocation fileLocation, final PhotoViewerProvider provider) {
        openPhoto(null, fileLocation, null, null, 0, provider);
    }

    public void openPhoto(final ArrayList<MessageObject> messages, final int index, final PhotoViewerProvider provider) {
        openPhoto(messages.get(index), null, messages, null, index, provider);
    }

    public void openPhotoForSelect(final ArrayList<Object> photos, final int index, int type, final PhotoViewerProvider provider) {
        sendPhotoType = type;
        openPhoto(null, null, null, photos, index, provider);
    }

    private boolean checkAnimation() {
        if (animationInProgress != 0) {
            if (Math.abs(transitionAnimationStartTime - System.currentTimeMillis()) >= 500) {
                if (animationEndRunnable != null) {
                    animationEndRunnable.run();
                    animationEndRunnable = null;
                }
                animationInProgress = 0;
            }
        }
        return animationInProgress != 0;
    }

    public void openPhoto(final MessageObject messageObject, final TLRPC.FileLocation fileLocation, final ArrayList<MessageObject> messages, final ArrayList<Object> photos, final int index, final PhotoViewerProvider provider) {
        if (parentActivity == null || isVisible || provider == null && checkAnimation() || messageObject == null && fileLocation == null && messages == null && photos == null) {
            return;
        }

        final PlaceProviderObject object = provider.getPlaceForPhoto(messageObject, fileLocation, index);
        if (object == null && photos == null) {
            return;
        }

        try {
            WindowManager wm = (WindowManager) parentActivity.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(windowView);
        } catch (Exception e) {
            //don't promt
        }

        WindowManager wm = (WindowManager) parentActivity.getSystemService(Context.WINDOW_SERVICE);
        try {
            wm.addView(windowView, windowLayoutParams);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return;
        }

        actionBar.setTitle(LocaleController.formatString("Of", R.string.Of, 1, 1));

        placeProvider = provider;

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }


        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileLoadProgressChanged);

        isVisible = true;
        backgroundDrawable.setAlpha(255);
        toggleActionBar(true, false);

        if (object != null) {
            disableShowCheck = true;
            animationInProgress = 1;
            onPhotoShow( photos, index, object);

            AndroidUtilities.lockOrientation(parentActivity);

//            final Rect drawRegion = object.imageReceiver.getDrawRegion();
//            int orientation = object.imageReceiver.getOrientation();

            animatingImageView.setVisibility(View.VISIBLE);
//            animatingImageView.setRadius(object.radius);
//            animatingImageView.setOrientation(orientation);
//            animatingImageView.setNeedRadius(object.radius != 0);
            animatingImageView.setImageBitmap(object.thumb);

            ViewProxy.setAlpha(animatingImageView, 1.0f);
            ViewProxy.setPivotX(animatingImageView, 0.0f);
            ViewProxy.setPivotY(animatingImageView, 0.0f);
            ViewProxy.setScaleX(animatingImageView, 1.0f);
            ViewProxy.setScaleY(animatingImageView, 1.0f);
            ViewProxy.setTranslationX(animatingImageView, object.viewX);
            ViewProxy.setTranslationY(animatingImageView, object.viewY);
            final ViewGroup.LayoutParams layoutParams = animatingImageView.getLayoutParams();
            layoutParams.width = object.width;
            layoutParams.height = object.height;
            animatingImageView.setLayoutParams(layoutParams);

            containerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    containerView.getViewTreeObserver().removeOnPreDrawListener(this);

                    float scaleX = (float) AndroidUtilities.displaySize.x / layoutParams.width;
                    float scaleY = (float) (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight) / layoutParams.height;
                    float scale = scaleX > scaleY ? scaleY : scaleX;
                    float width = layoutParams.width * scale;
                    float height = layoutParams.height * scale;
                    float xPos = (AndroidUtilities.displaySize.x - width) / 2.0f;
                    float yPos = (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight - height) / 2.0f;
//                    int clipHorizontal = Math.abs(drawRegion.left - object.imageReceiver.getImageX());
//                    int clipVertical = Math.abs(drawRegion.top - object.imageReceiver.getImageY());
//
//                    int coords2[] = new int[2];
//                    object.parentView.getLocationInWindow(coords2);
//                    int clipTop = coords2[1] - AndroidUtilities.statusBarHeight - (object.viewY + drawRegion.top);
//                    if (clipTop < 0) {
//                        clipTop = 0;
//                    }
//                    int clipBottom = (object.viewY + drawRegion.top + layoutParams.height) - (coords2[1] + object.parentView.getHeight() - AndroidUtilities.statusBarHeight);
//                    if (clipBottom < 0) {
//                        clipBottom = 0;
//                    }
//                    clipTop = Math.max(clipTop, clipVertical);
//                    clipBottom = Math.max(clipBottom, clipVertical);
                    int clipTop = 0;
                    int clipBottom = 0;
                    int clipHorizontal = 0;

                    AnimatorSetProxy animatorSet = new AnimatorSetProxy();
                    animatorSet.playTogether(
                            ObjectAnimatorProxy.ofFloat(animatingImageView, "scaleX", scale),
                            ObjectAnimatorProxy.ofFloat(animatingImageView, "scaleY", scale),
                            ObjectAnimatorProxy.ofFloat(animatingImageView, "translationX", xPos),
                            ObjectAnimatorProxy.ofFloat(animatingImageView, "translationY", yPos),
                            ObjectAnimatorProxy.ofInt(backgroundDrawable, "alpha", 0, 255),
                            ObjectAnimatorProxy.ofInt(animatingImageView, "clipHorizontal", clipHorizontal, 0),
                            ObjectAnimatorProxy.ofInt(animatingImageView, "clipTop", clipTop, 0),
                            ObjectAnimatorProxy.ofInt(animatingImageView, "clipBottom", clipBottom, 0),
                            ObjectAnimatorProxy.ofInt(animatingImageView, "radius", 0),
                            ObjectAnimatorProxy.ofFloat(containerView, "alpha", 0.0f, 1.0f)
                    );

                    animationEndRunnable = new Runnable() {
                        @Override
                        public void run() {
                            animationInProgress = 0;
                            setImages();
                            transitionAnimationStartTime = 0;
                            containerView.invalidate();
                            animatingImageView.setVisibility(View.GONE);
                            AndroidUtilities.unlockOrientation(parentActivity);
                            if (showAfterAnimation != null) {
                                showAfterAnimation.imageReceiver.setVisible(true, true);
                            }
                            if (hideAfterAnimation != null) {
                                hideAfterAnimation.imageReceiver.setVisible(false, true);
                            }
                        }
                    };

                    animatorSet.setDuration(200);
                    animatorSet.addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            if (animationEndRunnable != null) {
                                animationEndRunnable.run();
                                animationEndRunnable = null;
                            }
                        }

                        @Override
                        public void onAnimationCancel(Object animation) {
                            onAnimationEnd(animation);
                        }
                    });
                    transitionAnimationStartTime = System.currentTimeMillis();
                    animatorSet.start();

                    animatingImageView.setOnDrawListener(new ClippingImageView.onDrawListener() {
                        @Override
                        public void onDraw() {
                            disableShowCheck = false;
                            animatingImageView.setOnDrawListener(null);
//                            object.imageReceiver.setVisible(false, true);
                        }
                    });
                    return true;
                }
            });
        } else {
            ViewProxy.setAlpha(containerView, 1.0f);
            onPhotoShow( photos, index, object);
        }
    }

    public void closePhoto(boolean animated, boolean fromEditMode) {
        if (!fromEditMode && currentEditMode != 0) {
            if (currentEditMode == 1) {
                photoCropView.cancelAnimationRunnable();
            }
//            switchToEditMode(0);
            return;
        }
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.FileLoadProgressChanged);

        if (currentEditMode != 0) {
            if (currentEditMode == 2) {
                photoFilterView.shutdown();
                containerView.removeView(photoFilterView);
                photoFilterView = null;
            } else if (currentEditMode == 1) {
                photoCropView.setVisibility(View.GONE);
            }
            currentEditMode = 0;
        }

        if (parentActivity == null || !isVisible || checkAnimation() || placeProvider == null) {
            return;
        }

        ConnectionsManager.getInstance().cancelRpcsForClassGuid(classGuid);

        isActionBarVisible = false;

        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
        ConnectionsManager.getInstance().cancelRpcsForClassGuid(classGuid);

//        final PlaceProviderObject object = placeProvider.getPlaceForPhoto(null, null, currentIndex);
        final PlaceProviderObject object = null;

        if (animated) {
            AndroidUtilities.lockOrientation(parentActivity);

            animationInProgress = 1;
            int visibility = animatingImageView.getVisibility();
            animatingImageView.setVisibility(View.VISIBLE);
            containerView.invalidate();

            AnimatorSetProxy animatorSet = new AnimatorSetProxy();

            final ViewGroup.LayoutParams layoutParams = animatingImageView.getLayoutParams();
            Rect drawRegion = null;
            animatingImageView.setOrientation(centerImage.getOrientation());
            if (object != null) {
                animatingImageView.setNeedRadius(object.radius != 0);
                drawRegion = object.imageReceiver.getDrawRegion();
                layoutParams.width = drawRegion.right - drawRegion.left;
                layoutParams.height = drawRegion.bottom - drawRegion.top;
                animatingImageView.setImageBitmap(object.thumb);
            } else {
                animatingImageView.setNeedRadius(false);
                layoutParams.width = centerImage.getImageWidth();
                layoutParams.height = centerImage.getImageHeight();
                animatingImageView.setImageBitmap(centerImage.getBitmap());
            }
            animatingImageView.setLayoutParams(layoutParams);

            float scaleX = (float) AndroidUtilities.displaySize.x / layoutParams.width;
            float scaleY = (float) (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight) / layoutParams.height;
            float scale2 = scaleX > scaleY ? scaleY : scaleX;
            float width = layoutParams.width * scale * scale2;
            float height = layoutParams.height * scale * scale2;
            float xPos = (AndroidUtilities.displaySize.x - width) / 2.0f;
            float yPos = (AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight - height) / 2.0f;
            ViewProxy.setTranslationX(animatingImageView, xPos + translationX);
            ViewProxy.setTranslationY(animatingImageView, yPos + translationY);
            ViewProxy.setScaleX(animatingImageView, scale * scale2);
            ViewProxy.setScaleY(animatingImageView, scale * scale2);

            if (object != null) {
                object.imageReceiver.setVisible(false, true);
                int clipHorizontal = Math.abs(drawRegion.left - object.imageReceiver.getImageX());
                int clipVertical = Math.abs(drawRegion.top - object.imageReceiver.getImageY());

                int coords2[] = new int[2];
                object.parentView.getLocationInWindow(coords2);
                int clipTop = coords2[1] - AndroidUtilities.statusBarHeight - (object.viewY + drawRegion.top);
                if (clipTop < 0) {
                    clipTop = 0;
                }
                int clipBottom = (object.viewY + drawRegion.top + (drawRegion.bottom - drawRegion.top)) - (coords2[1] + object.parentView.getHeight() - AndroidUtilities.statusBarHeight);
                if (clipBottom < 0) {
                    clipBottom = 0;
                }

                clipTop = Math.max(clipTop, clipVertical);
                clipBottom = Math.max(clipBottom, clipVertical);

                animatorSet.playTogether(
                        ObjectAnimatorProxy.ofFloat(animatingImageView, "scaleX", 1),
                        ObjectAnimatorProxy.ofFloat(animatingImageView, "scaleY", 1),
                        ObjectAnimatorProxy.ofFloat(animatingImageView, "translationX", object.viewX + drawRegion.left),
                        ObjectAnimatorProxy.ofFloat(animatingImageView, "translationY", object.viewY + drawRegion.top),
                        ObjectAnimatorProxy.ofInt(backgroundDrawable, "alpha", 0),
                        ObjectAnimatorProxy.ofInt(animatingImageView, "clipHorizontal", clipHorizontal),
                        ObjectAnimatorProxy.ofInt(animatingImageView, "clipTop", clipTop),
                        ObjectAnimatorProxy.ofInt(animatingImageView, "clipBottom", clipBottom),
                        ObjectAnimatorProxy.ofInt(animatingImageView, "radius", object.radius),
                        ObjectAnimatorProxy.ofFloat(containerView, "alpha", 0.0f)
                );
            } else {
                animatorSet.playTogether(
                        ObjectAnimatorProxy.ofInt(backgroundDrawable, "alpha", 0),
                        ObjectAnimatorProxy.ofFloat(animatingImageView, "alpha", 0.0f),
                        ObjectAnimatorProxy.ofFloat(animatingImageView, "translationY", translationY >= 0 ? AndroidUtilities.displaySize.y : -AndroidUtilities.displaySize.y),
                        ObjectAnimatorProxy.ofFloat(containerView, "alpha", 0.0f)
                );
            }

            animationEndRunnable = new Runnable() {
                @Override
                public void run() {
                    AndroidUtilities.unlockOrientation(parentActivity);
                    animationInProgress = 0;
                    onPhotoClosed(object);
                }
            };

            animatorSet.setDuration(200);
            animatorSet.addListener(new AnimatorListenerAdapterProxy() {
                @Override
                public void onAnimationEnd(Object animation) {
                    if (animationEndRunnable != null) {
                        animationEndRunnable.run();
                        animationEndRunnable = null;
                    }
                }

                @Override
                public void onAnimationCancel(Object animation) {
                    onAnimationEnd(animation);
                }
            });
            transitionAnimationStartTime = System.currentTimeMillis();
            animatorSet.start();
        } else {
            AnimatorSetProxy animatorSet = new AnimatorSetProxy();
            animatorSet.playTogether(
                    ObjectAnimatorProxy.ofFloat(containerView, "scaleX", 0.9f),
                    ObjectAnimatorProxy.ofFloat(containerView, "scaleY", 0.9f),
                    ObjectAnimatorProxy.ofInt(backgroundDrawable, "alpha", 0),
                    ObjectAnimatorProxy.ofFloat(containerView, "alpha", 0.0f)
            );
            animationInProgress = 2;
            animationEndRunnable = new Runnable() {
                @Override
                public void run() {
                    animationInProgress = 0;
                    onPhotoClosed(object);
                    ViewProxy.setScaleX(containerView, 1.0f);
                    ViewProxy.setScaleY(containerView, 1.0f);
                    containerView.clearAnimation();
                }
            };
            animatorSet.setDuration(200);
            animatorSet.addListener(new AnimatorListenerAdapterProxy() {
                @Override
                public void onAnimationEnd(Object animation) {
                    if (animationEndRunnable != null) {
                        animationEndRunnable.run();
                        animationEndRunnable = null;
                    }
                }
            });
            transitionAnimationStartTime = System.currentTimeMillis();
            animatorSet.start();
        }
    }

    public void destroyPhotoViewer() {
        if (parentActivity == null || windowView == null) {
            return;
        }
        try {
            if (windowView.getParent() != null) {
                WindowManager wm = (WindowManager) parentActivity.getSystemService(Context.WINDOW_SERVICE);
                wm.removeViewImmediate(windowView);
            }
            windowView = null;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        Instance = null;
    }

    private void onPhotoClosed(PlaceProviderObject object) {
        isVisible = false;
        disableShowCheck = true;
        currentPathObject = null;
        currentThumb = null;
        if (gifDrawable != null) {
            gifDrawable.recycle();
            gifDrawable = null;
        }
        for (int a = 0; a < 3; a++) {
            if (radialProgressViews[a] != null) {
                radialProgressViews[a].setBackgroundState(-1, false);
            }
        }
        centerImage.setImageBitmap((Bitmap) null);
        leftImage.setImageBitmap((Bitmap) null);
        rightImage.setImageBitmap((Bitmap) null);
        containerView.post(new Runnable() {
            @Override
            public void run() {
                animatingImageView.setImageBitmap(null);
                try {
                    if (windowView.getParent() != null) {
                        WindowManager wm = (WindowManager) parentActivity.getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(windowView);
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        });
        if (placeProvider != null) {
            placeProvider.willHidePhotoViewer();
        }
        placeProvider = null;
        disableShowCheck = false;
        if (object != null) {
            object.imageReceiver.setVisible(true, true);
        }
    }

    public boolean isVisible() {
        return isVisible && placeProvider != null;
    }

    private void updateMinMax(float scale) {
        int maxW = (int) (centerImage.getImageWidth() * scale - getContainerViewWidth()) / 2;
        int maxH = (int) (centerImage.getImageHeight() * scale - getContainerViewHeight()) / 2;
        if (maxW > 0) {
            minX = -maxW;
            maxX = maxW;
        } else {
            minX = maxX = 0;
        }
        if (maxH > 0) {
            minY = -maxH;
            maxY = maxH;
        } else {
            minY = maxY = 0;
        }
        if (currentEditMode == 1) {
            maxX += photoCropView.getLimitX();
            maxY += photoCropView.getLimitY();
            minX -= photoCropView.getLimitWidth();
            minY -= photoCropView.getLimitHeight();
        }
    }

    private int getAdditionX() {
        if (currentEditMode != 0) {
            return AndroidUtilities.dp(14);
        }
        return 0;
    }

    private int getAdditionY() {
        if (currentEditMode != 0) {
            return AndroidUtilities.dp(14);
        }
        return 0;
    }

    private int getContainerViewWidth() {
        return getContainerViewWidth(currentEditMode);
    }

    private int getContainerViewWidth(int mode) {
        int width = containerView.getWidth();
        if (mode != 0) {
            width -= AndroidUtilities.dp(28);
        }
        return width;
    }

    private int getContainerViewHeight() {
        return getContainerViewHeight(currentEditMode);
    }

    private int getContainerViewHeight(int mode) {
        int height = containerView.getHeight();
        if (mode == 1) {
            height -= AndroidUtilities.dp(76);
        } else if (mode == 2) {
            height -= AndroidUtilities.dp(154);
        }
        return height;
    }

    private boolean onTouchEvent(MotionEvent ev) {
        if (animationInProgress != 0 || animationStartTime != 0) {
            if (animationStartTime == 0) {
                AndroidUtilities.unlockOrientation(parentActivity);
            }
            return false;
        }

        if (currentEditMode == 2) {
            photoFilterView.onTouch(ev);
            return true;
        }

        if (currentEditMode == 1) {
            if (ev.getPointerCount() == 1) {
                if (photoCropView.onTouch(ev)) {
                    updateMinMax(scale);
                    return true;
                }
            } else {
                photoCropView.onTouch(null);
            }
        }

        if (currentEditMode == 0 && ev.getPointerCount() == 1 && gestureDetector.onTouchEvent(ev)) {
            if (doubleTap) {
                doubleTap = false;
                moving = false;
                zooming = false;
                checkMinMax(false);
                return true;
            }
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN || ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            if (currentEditMode == 1) {
                photoCropView.cancelAnimationRunnable();
            }
            discardTap = false;
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            if (!draggingDown && !changingPage) {
                if (canZoom && ev.getPointerCount() == 2) {
                    pinchStartDistance = (float) Math.hypot(ev.getX(1) - ev.getX(0), ev.getY(1) - ev.getY(0));
                    pinchStartScale = scale;
                    pinchCenterX = (ev.getX(0) + ev.getX(1)) / 2.0f;
                    pinchCenterY = (ev.getY(0) + ev.getY(1)) / 2.0f;
                    pinchStartX = translationX;
                    pinchStartY = translationY;
                    zooming = true;
                    moving = false;
                    if (velocityTracker != null) {
                        velocityTracker.clear();
                    }
                } else if (ev.getPointerCount() == 1) {
                    moveStartX = ev.getX();
                    dragY = moveStartY = ev.getY();
                    draggingDown = false;
                    canDragDown = true;
                    AndroidUtilities.lockOrientation(parentActivity);
                    if (velocityTracker != null) {
                        velocityTracker.clear();
                    }
                }
            }
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (currentEditMode == 1) {
                photoCropView.cancelAnimationRunnable();
            }
            if (canZoom && ev.getPointerCount() == 2 && !draggingDown && zooming && !changingPage) {
                discardTap = true;
                scale = (float) Math.hypot(ev.getX(1) - ev.getX(0), ev.getY(1) - ev.getY(0)) / pinchStartDistance * pinchStartScale;
                translationX = (pinchCenterX - getContainerViewWidth() / 2) - ((pinchCenterX - getContainerViewWidth() / 2) - pinchStartX) * (scale / pinchStartScale);
                translationY = (pinchCenterY - getContainerViewHeight() / 2) - ((pinchCenterY - getContainerViewHeight() / 2) - pinchStartY) * (scale / pinchStartScale);
                updateMinMax(scale);
                containerView.invalidate();
            } else if (ev.getPointerCount() == 1) {
                if (velocityTracker != null) {
                    velocityTracker.addMovement(ev);
                }
                float dx = Math.abs(ev.getX() - moveStartX);
                float dy = Math.abs(ev.getY() - dragY);
                if (dx > AndroidUtilities.dp(3) || dy > AndroidUtilities.dp(3)) {
                    discardTap = true;
                }
                if (!(placeProvider instanceof EmptyPhotoViewerProvider) && currentEditMode == 0 && canDragDown && !draggingDown && scale == 1 && dy >= AndroidUtilities.dp(30) && dy / 2 > dx) {
                    draggingDown = true;
                    moving = false;
                    dragY = ev.getY();
                    if (isActionBarVisible && canShowBottom) {
                        toggleActionBar(false, true);
                    }
                    return true;
                } else if (draggingDown) {
                    translationY = ev.getY() - dragY;
                    containerView.invalidate();
                } else if (!invalidCoords && animationStartTime == 0) {
                    float moveDx = moveStartX - ev.getX();
                    float moveDy = moveStartY - ev.getY();
                    if (moving || currentEditMode != 0 || scale == 1 && Math.abs(moveDy) + AndroidUtilities.dp(12) < Math.abs(moveDx) || scale != 1) {
                        if (!moving) {
                            moveDx = 0;
                            moveDy = 0;
                            moving = true;
                            canDragDown = false;
                        }

                        moveStartX = ev.getX();
                        moveStartY = ev.getY();
                        updateMinMax(scale);
                        if (translationX < minX && (currentEditMode != 0 || !rightImage.hasImage()) || translationX > maxX && (currentEditMode != 0 || !leftImage.hasImage())) {
                            moveDx /= 3.0f;
                        }
                        if (maxY == 0 && minY == 0 && currentEditMode == 0) {
                            if (translationY - moveDy < minY) {
                                translationY = minY;
                                moveDy = 0;
                            } else if (translationY - moveDy > maxY) {
                                translationY = maxY;
                                moveDy = 0;
                            }
                        } else {
                            if (translationY < minY || translationY > maxY) {
                                moveDy /= 3.0f;
                            }
                        }

                        translationX -= moveDx;
                        if (scale != 1 || currentEditMode != 0) {
                            translationY -= moveDy;
                        }

                        containerView.invalidate();
                    }
                } else {
                    invalidCoords = false;
                    moveStartX = ev.getX();
                    moveStartY = ev.getY();
                }
            }
        } else if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL || ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            if (currentEditMode == 1) {
                photoCropView.startAnimationRunnable();
            }
            if (zooming) {
                invalidCoords = true;
                if (scale < 1.0f) {
                    updateMinMax(1.0f);
                    animateTo(1.0f, 0, 0, true);
                } else if (scale > 3.0f) {
                    float atx = (pinchCenterX - getContainerViewWidth() / 2) - ((pinchCenterX - getContainerViewWidth() / 2) - pinchStartX) * (3.0f / pinchStartScale);
                    float aty = (pinchCenterY - getContainerViewHeight() / 2) - ((pinchCenterY - getContainerViewHeight() / 2) - pinchStartY) * (3.0f / pinchStartScale);
                    updateMinMax(3.0f);
                    if (atx < minX) {
                        atx = minX;
                    } else if (atx > maxX) {
                        atx = maxX;
                    }
                    if (aty < minY) {
                        aty = minY;
                    } else if (aty > maxY) {
                        aty = maxY;
                    }
                    animateTo(3.0f, atx, aty, true);
                } else {
                    checkMinMax(true);
                }
                zooming = false;
            } else if (draggingDown) {
                if (Math.abs(dragY - ev.getY()) > getContainerViewHeight() / 6.0f) {
                    closePhoto(true, false);
                } else {
                    animateTo(1, 0, 0, false);
                }
                draggingDown = false;
            } else if (moving) {
                float moveToX = translationX;
                float moveToY = translationY;
                updateMinMax(scale);
                moving = false;
                canDragDown = true;
                float velocity = 0;
                if (velocityTracker != null && scale == 1) {
                    velocityTracker.computeCurrentVelocity(1000);
                    velocity = velocityTracker.getXVelocity();
                }

                if (currentEditMode == 0) {
                    if ((translationX < minX - getContainerViewWidth() / 3 || velocity < -AndroidUtilities.dp(650)) && rightImage.hasImage()) {
                        goToNext();
                        return true;
                    }
                    if ((translationX > maxX + getContainerViewWidth() / 3 || velocity > AndroidUtilities.dp(650)) && leftImage.hasImage()) {
                        goToPrev();
                        return true;
                    }
                }

                if (translationX < minX) {
                    moveToX = minX;
                } else if (translationX > maxX) {
                    moveToX = maxX;
                }
                if (translationY < minY) {
                    moveToY = minY;
                } else if (translationY > maxY) {
                    moveToY = maxY;
                }
                animateTo(scale, moveToX, moveToY, false);
            } else {
                AndroidUtilities.unlockOrientation(parentActivity);
            }
        }
        return false;
    }

    private void checkMinMax(boolean zoom) {
        float moveToX = translationX;
        float moveToY = translationY;
        updateMinMax(scale);
        if (translationX < minX) {
            moveToX = minX;
        } else if (translationX > maxX) {
            moveToX = maxX;
        }
        if (translationY < minY) {
            moveToY = minY;
        } else if (translationY > maxY) {
            moveToY = maxY;
        }
        animateTo(scale, moveToX, moveToY, zoom);
    }

    private void goToNext() {
        float extra = 0;
        if (scale != 1) {
            extra = (getContainerViewWidth() - centerImage.getImageWidth()) / 2 * scale;
        }
        switchImageAfterAnimation = 1;
        animateTo(scale, minX - getContainerViewWidth() - extra - PAGE_SPACING / 2, translationY, false);
    }

    private void goToPrev() {
        float extra = 0;
        if (scale != 1) {
            extra = (getContainerViewWidth() - centerImage.getImageWidth()) / 2 * scale;
        }
        switchImageAfterAnimation = 2;
        animateTo(scale, maxX + getContainerViewWidth() + extra + PAGE_SPACING / 2, translationY, false);
    }

    private void animateTo(float newScale, float newTx, float newTy, boolean isZoom) {
        animateTo(newScale, newTx, newTy, isZoom, 250);
    }

    private void animateTo(float newScale, float newTx, float newTy, boolean isZoom, int duration) {
        if (scale == newScale && translationX == newTx && translationY == newTy) {
            AndroidUtilities.unlockOrientation(parentActivity);
            return;
        }
        zoomAnimation = isZoom;
        animateToScale = newScale;
        animateToX = newTx;
        animateToY = newTy;
        animationStartTime = System.currentTimeMillis();
        imageMoveAnimation = new AnimatorSetProxy();
        imageMoveAnimation.playTogether(
                ObjectAnimatorProxy.ofFloat(this, "animationValue", 0, 1)
        );
        imageMoveAnimation.setInterpolator(interpolator);
        imageMoveAnimation.setDuration(duration);
        imageMoveAnimation.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationEnd(Object animation) {
                imageMoveAnimation = null;
                AndroidUtilities.unlockOrientation(parentActivity);
                containerView.invalidate();
            }
        });
        imageMoveAnimation.start();
        AndroidUtilities.lockOrientation(parentActivity);
    }

    public void setAnimationValue(float value) {
        animationValue = value;
        containerView.invalidate();
    }

    public float getAnimationValue() {
        return animationValue;
    }

    private void onDraw(Canvas canvas) {
        if (animationInProgress == 1 || !isVisible && animationInProgress != 2) {
            return;
        }

        float currentTranslationY;
        float currentTranslationX;
        float currentScale;
        float aty = -1;

        if (imageMoveAnimation != null) {
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }

            float ts = scale + (animateToScale - scale) * animationValue;
            float tx = translationX + (animateToX - translationX) * animationValue;
            float ty = translationY + (animateToY - translationY) * animationValue;
            if (currentEditMode == 1) {
                photoCropView.setAnimationProgress(animationValue);
            }

            if (animateToScale == 1 && scale == 1 && translationX == 0) {
                aty = ty;
            }
            currentScale = ts;
            currentTranslationY = ty;
            currentTranslationX = tx;
            containerView.invalidate();
        } else {
            if (animationStartTime != 0) {
                translationX = animateToX;
                translationY = animateToY;
                scale = animateToScale;
                animationStartTime = 0;
                if (currentEditMode == 1) {
                    photoCropView.setAnimationProgress(1);
                }
                updateMinMax(scale);
                AndroidUtilities.unlockOrientation(parentActivity);
                zoomAnimation = false;
            }
            if (!scroller.isFinished()) {
                if (scroller.computeScrollOffset()) {
                    if (scroller.getStartX() < maxX && scroller.getStartX() > minX) {
                        translationX = scroller.getCurrX();
                    }
                    if (scroller.getStartY() < maxY && scroller.getStartY() > minY) {
                        translationY = scroller.getCurrY();
                    }
                    containerView.invalidate();
                }
            }
            if (switchImageAfterAnimation != 0) {
                if (switchImageAfterAnimation == 1) {
                    setImageIndex(currentIndex + 1, false);
                } else if (switchImageAfterAnimation == 2) {
                    setImageIndex(currentIndex - 1, false);
                }
                switchImageAfterAnimation = 0;
            }
            currentScale = scale;
            currentTranslationY = translationY;
            currentTranslationX = translationX;
            if (!moving) {
                aty = translationY;
            }
        }

        if (currentEditMode == 0 && scale == 1 && aty != -1 && !zoomAnimation) {
            float maxValue = getContainerViewHeight() / 4.0f;
            backgroundDrawable.setAlpha((int) Math.max(127, 255 * (1.0f - (Math.min(Math.abs(aty), maxValue) / maxValue))));
        } else {
            backgroundDrawable.setAlpha(255);
        }

        ImageReceiver sideImage = null;
        Bitmap bitmap;
        if (currentEditMode == 0) {
            if (scale >= 1.0f && !zoomAnimation && !zooming) {
                if (currentTranslationX > maxX + AndroidUtilities.dp(5)) {
                    sideImage = leftImage;
                } else if (currentTranslationX < minX - AndroidUtilities.dp(5)) {
                    sideImage = rightImage;
                }
            }
            changingPage = sideImage != null;
        }

        if (sideImage == rightImage) {
            float tranlateX = currentTranslationX;
            float scaleDiff = 0;
            float alpha = 1;
            if (!zoomAnimation && tranlateX < minX) {
                alpha = Math.min(1.0f, (minX - tranlateX) / canvas.getWidth());
                scaleDiff = (1.0f - alpha) * 0.3f;
                tranlateX = -canvas.getWidth() - PAGE_SPACING / 2;
            }

            if (sideImage.getBitmap() != null) {
                canvas.save();
                canvas.translate(getContainerViewWidth() / 2, getContainerViewHeight() / 2);
                canvas.translate(canvas.getWidth() + PAGE_SPACING / 2 + tranlateX, 0);
                canvas.scale(1.0f - scaleDiff, 1.0f - scaleDiff);
                int bitmapWidth = sideImage.getBitmapWidth();
                int bitmapHeight = sideImage.getBitmapHeight();

                float scaleX = (float) getContainerViewWidth() / (float) bitmapWidth;
                float scaleY = (float) getContainerViewHeight() / (float) bitmapHeight;
                float scale = scaleX > scaleY ? scaleY : scaleX;
                int width = (int) (bitmapWidth * scale);
                int height = (int) (bitmapHeight * scale);

                sideImage.setAlpha(alpha);
                sideImage.setImageCoords(-width / 2, -height / 2, width, height);
                sideImage.draw(canvas);
                canvas.restore();
            }

            canvas.save();
            canvas.translate(tranlateX, currentTranslationY / currentScale);
            canvas.translate((canvas.getWidth() * (scale + 1) + PAGE_SPACING) / 2, -currentTranslationY / currentScale);
            radialProgressViews[1].setScale(1.0f - scaleDiff);
            radialProgressViews[1].setAlpha(alpha);
            radialProgressViews[1].onDraw(canvas);
            canvas.restore();
        }

        float tranlateX = currentTranslationX;
        float scaleDiff = 0;
        float alpha = 1;
        if (!zoomAnimation && tranlateX > maxX && currentEditMode == 0) {
            alpha = Math.min(1.0f, (tranlateX - maxX) / canvas.getWidth());
            scaleDiff = alpha * 0.3f;
            alpha = 1.0f - alpha;
            tranlateX = maxX;
        }
        if (centerImage.getBitmap() != null) {
            canvas.save();
            canvas.translate(getContainerViewWidth() / 2 + getAdditionX(), getContainerViewHeight() / 2 + getAdditionY());
            canvas.translate(tranlateX, currentTranslationY);
            canvas.scale(currentScale - scaleDiff, currentScale - scaleDiff);

            if (currentEditMode == 1) {
                photoCropView.setBitmapParams(currentScale, tranlateX, currentTranslationY);
            }

            int bitmapWidth = centerImage.getBitmapWidth();
            int bitmapHeight = centerImage.getBitmapHeight();

            float scaleX = (float) getContainerViewWidth() / (float) bitmapWidth;
            float scaleY = (float) getContainerViewHeight() / (float) bitmapHeight;
            float scale = scaleX > scaleY ? scaleY : scaleX;
            int width = (int) (bitmapWidth * scale);
            int height = (int) (bitmapHeight * scale);

            if (gifDrawable != null) {
                canvas.save();
                gifDrawable.setAlpha((int) (alpha * 255));
                gifDrawable.setBounds(-width / 2, -height / 2, width / 2, height / 2);
                gifDrawable.draw(canvas);
                canvas.restore();
            } else {
                centerImage.setAlpha(alpha);
                centerImage.setImageCoords(-width / 2, -height / 2, width, height);
                centerImage.draw(canvas);
            }
            canvas.restore();
        }
        canvas.save();
        canvas.translate(tranlateX, currentTranslationY / currentScale);
        radialProgressViews[0].setScale(1.0f - scaleDiff);
        radialProgressViews[0].setAlpha(alpha);
        radialProgressViews[0].onDraw(canvas);
        canvas.restore();

        if (sideImage == leftImage) {
            if (sideImage.getBitmap() != null) {
                canvas.save();
                canvas.translate(getContainerViewWidth() / 2, getContainerViewHeight() / 2);
                canvas.translate(-(canvas.getWidth() * (scale + 1) + PAGE_SPACING) / 2 + currentTranslationX, 0);
                int bitmapWidth = sideImage.getBitmapWidth();
                int bitmapHeight = sideImage.getBitmapHeight();

                float scaleX = (float) getContainerViewWidth() / (float) bitmapWidth;
                float scaleY = (float) getContainerViewHeight() / (float) bitmapHeight;
                float scale = scaleX > scaleY ? scaleY : scaleX;
                int width = (int) (bitmapWidth * scale);
                int height = (int) (bitmapHeight * scale);

                sideImage.setAlpha(1.0f);
                sideImage.setImageCoords(-width / 2, -height / 2, width, height);
                sideImage.draw(canvas);
                canvas.restore();
            }

            canvas.save();
            canvas.translate(currentTranslationX, currentTranslationY / currentScale);
            canvas.translate(-(canvas.getWidth() * (scale + 1) + PAGE_SPACING) / 2, -currentTranslationY / currentScale);
            radialProgressViews[2].setScale(1.0f);
            radialProgressViews[2].setAlpha(1.0f);
            radialProgressViews[2].onDraw(canvas);
            canvas.restore();
        }
    }

    @SuppressLint("DrawAllocation")
    private void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            scale = 1;
            translationX = 0;
            translationY = 0;
            updateMinMax(scale);

            if (checkImageView != null) {
                checkImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        checkImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) checkImageView.getLayoutParams();
                        WindowManager manager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Activity.WINDOW_SERVICE);
                        int rotation = manager.getDefaultDisplay().getRotation();
                        if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_90) {
                            layoutParams.topMargin = AndroidUtilities.dp(58);
                        } else {
                            layoutParams.topMargin = AndroidUtilities.dp(68);
                        }
                        checkImageView.setLayoutParams(layoutParams);
                        return false;
                    }
                });
            }
        }
    }

    private void onActionClick() {
//        if (currentMessageObject == null || currentFileNames[0] == null) {
//            return;
//        }
//        boolean loadFile = false;
//        if (currentMessageObject.messageOwner.attachPath != null && currentMessageObject.messageOwner.attachPath.length() != 0) {
//            File f = new File(currentMessageObject.messageOwner.attachPath);
//            if (f.exists()) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(f), "video/mp4");
//                parentActivity.startActivityForResult(intent, 500);
//            } else {
//                loadFile = true;
//            }
//        } else {
//            File cacheFile = FileLoader.getPathToMessage(currentMessageObject.messageOwner);
//            if (cacheFile.exists()) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(cacheFile), "video/mp4");
//                parentActivity.startActivityForResult(intent, 500);
//            } else {
//                loadFile = true;
//            }
//        }
//        if (loadFile) {
//            if (!FileLoader.getInstance().isLoadingFile(currentFileNames[0])) {
//                FileLoader.getInstance().loadFile(currentMessageObject.messageOwner.media.video, true);
//            } else {
//                FileLoader.getInstance().cancelLoadFile(currentMessageObject.messageOwner.media.video);
//            }
//        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (scale != 1) {
            scroller.abortAnimation();
            scroller.fling(Math.round(translationX), Math.round(translationY), Math.round(velocityX), Math.round(velocityY), (int) minX, (int) maxX, (int) minY, (int) maxY);
            containerView.postInvalidate();
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (discardTap) {
            return false;
        }
        if (canShowBottom) {
            if (radialProgressViews[0] != null && containerView != null) {
                int state = radialProgressViews[0].backgroundState;
                if (state > 0 && state <= 3) {
                    float x = e.getX();
                    float y = e.getY();
                    if (x >= (getContainerViewWidth() - AndroidUtilities.dp(64)) / 2.0f && x <= (getContainerViewWidth() + AndroidUtilities.dp(64)) / 2.0f &&
                            y >= (getContainerViewHeight() - AndroidUtilities.dp(64)) / 2.0f && y <= (getContainerViewHeight() + AndroidUtilities.dp(64)) / 2.0f) {
                        onActionClick();
                        checkProgress(0, true);
                        return true;
                    }
                }
            }
            toggleActionBar(!isActionBarVisible, true);
        } else if (sendPhotoType == 0) {
            checkImageView.performClick();
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!canZoom || scale == 1.0f && (translationY != 0 || translationX != 0)) {
            return false;
        }
        if (animationStartTime != 0 || animationInProgress != 0) {
            return false;
        }
        if (scale == 1.0f) {
            float atx = (e.getX() - getContainerViewWidth() / 2) - ((e.getX() - getContainerViewWidth() / 2) - translationX) * (3.0f / scale);
            float aty = (e.getY() - getContainerViewHeight() / 2) - ((e.getY() - getContainerViewHeight() / 2) - translationY) * (3.0f / scale);
            updateMinMax(3.0f);
            if (atx < minX) {
                atx = minX;
            } else if (atx > maxX) {
                atx = maxX;
            }
            if (aty < minY) {
                aty = minY;
            } else if (aty > maxY) {
                aty = maxY;
            }
            animateTo(3.0f, atx, aty, true);
        } else {
            animateTo(1.0f, 0, 0, true);
        }
        doubleTap = true;
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
